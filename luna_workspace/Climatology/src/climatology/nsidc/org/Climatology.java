package climatology.nsidc.org;

import helper_classes.*;

/* Climatology
 * 
 * Generate baseline climatology data files.
 * 
 * The user selects a time span and increment (monthly, seasonal, same month over multiple
 * years, etc), and the program reads the data, generating statistical files based on the
 * data.
 */

public class Climatology extends GeoObject {

	// Data types
	public enum DataType {
		SEA_ICE("seaice"), SSMI("ssmi"), AVHRR("avhrr");
		private final String name;

		private DataType(String s) {
			name = s;
		}

		public String toString() {
			return name;
		}
	}

	static DataType dataType = DataType.SSMI;

	static Timespan.Increment increment = Timespan.Increment.MULTIYEARJAN;

	// Start and end dates for the processing.
	// A data file for the start date *must* exist because that will be used
	// to generate the metadata.
	static int startYear = 2012;
	static int startMonth = 1;
	static int startDay = 30;

	static int finalYear = 2013;
	static int finalMonth = 12;
	static int finalDay = 30;

	static int lastStartYear = 0;
	static int lastStartMonth = 0;
	static int lastStartDay = 0;

	static int imageStartIndex = 0;
	static int imageEndIndex = 0;

	static boolean filterBadData = false; // Filter out bad data points using
											// minvalue and maxvalue?
	static final int minValue = -1000000000; // Minimum acceptable data value
	static final int maxValue = 1000000000; // Maximum acceptable data value

	// Files and paths for i/o
	
	//...WINDOWS
	static final String outputPath = "/Users/glgr9602/Desktop/condense/climatology/" +
			dataType.toString() + "/" + increment.toString() + "/";
	static final String dataPath = "/Users/glgr9602/Desktop/condense/data/" +
			dataType.toString() + "/daily/";
	
	// ...LINUX
	/*
	static final String outputPath = "/home/glgr9602/condense/climatology/" +
	/
			dataType.toString() + "/" + increment.toString() + "/";
	static final String dataPath = "/home/glgr9602/condense/data/" +
			dataType.toString() + "/daily/";
	static final String climatologyPrefix = "climate_";
	*/
	
	static boolean warningMessages = false; // Receive warning messages?
	static boolean debugMessages = false; // Receive debug messages?
	static boolean addYearToInputDirectory = true; // The input files may be
													// stored in subdirectories
													// by year

	// SSMI data selection
	static String polarization = "v"; // Horizontal (h) or vertical (v)
	static int frequency = 22; // Frequency of SSMI data

	// The image data across the specified time span [day][row][col]
	GriddedVector data[][][];

	int days = 0; // Days processed during one time increment

	boolean haveMetadata = false;
	Metadata metadata;

	// The dataset we're going to read.
	Dataset dataset;

	// Gridded image pixel locations.
	GriddedLocation locations[][];

	// The mean provides a reference for deciding whether to condense the
	// newest pixels; i.e., are the pixels varying greater than n*sd from
	// the mean? If so, keep the newest ones and update the reference image
	// with the new pixel values.
	double[][] mean = null; // Mean: [row][col]
	double[][] sd = null; // Standard deviation: [row][col]
	int population[][] = null;

	int rows = 316;
	int cols = 332;

	// Number of files successfully read, for sanity checks
	int fileCount = 0;

	/*-------------------------------------------------------------------------
	// MAIN PROGRAM
	//-----------------------------------------------------------------------*/

	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();

		// Control what messages we see.
		Tools.setDebug(debugMessages);
		Tools.setWarnings(warningMessages);

		Tools.statusMessage("Process " + dataType.toString() + " data from "
				+ startYear + "." + startMonth + "." + startDay + " to "
				+ finalYear + "." + finalMonth + "." + finalDay
				+ " in increments of " + increment.toString());

		new Climatology();

		long endTime = System.currentTimeMillis();
		endTime = (endTime - startTime) / 1000;
		Tools.statusMessage("Total time to process = " + endTime + " seconds");
	}

	/*
	 * Climatology
	 * 
	 * Read the data, make climatology baselines (mean and standard deviation).
	 */
	Climatology() {

		// Read the data files. The loop continues until all data files have
		// been read.
		while (readData()) {

			// Condense the data and add it to the database.
			makeBaseline();

		}

		// Warm fuzzy feedback.
		Tools.statusMessage("Total data files processed = " + fileCount);
	}

	
	/* 
	 * getLocations
	 * 
	 * Got the data locations? Metadata must be read first.
	 */
	protected void getLocations() {
		
		if (!haveMetadata) {
			Tools.errorMessage("Condense", "getLocations", "Metadata must be read before retrieving locations.",
					new Exception());
		}
		
		locations = new GriddedLocation[metadata.rows()][metadata.cols()];
		
		switch (dataType) {
    		case SEA_ICE:
    			// TODO
    		case SSMI:
    			// TODO
    			// temporary locations, for testing
    			for (int r = 0; r < metadata.rows(); r++) {
    				for (int c = 0; c < metadata.cols(); c++) {
    					locations[r][c] = new GriddedLocation(r, c, (double) Tools.randomInt(90), (double) Tools.randomInt(180));
    				}
    			}
    			break;
    		case AVHRR:
    			// todo
    			break;
		}
		
		return;
	}
	
	/*
	 * readData
	 * 
	 * Read the dataset files.
	 * 
	 * This method reads data files in 'gulps' of the specified time increment
	 * (i.e., week, month, or seasonal, etc.).
	 * 
	 * Returns false if the start day for processing is greater than the final
	 * day (i.e., the ultimate final day as the user specified for processing).
	 * 
	 * Doesn't care if a file is missing. Assumes the data isn't available and
	 * plows ahead.
	 */
	protected boolean readData() {

		// Keep track of when we started.
		lastStartYear = startYear;
		lastStartMonth = startMonth;
		lastStartDay = startDay;

		Timestamp startDate = new Timestamp(startYear, startMonth, startDay);
		Timestamp finalDate = new Timestamp(finalYear, finalMonth, finalDay);
		Timestamp.dateSeparator("");

		// Are we already done?
		if (startDate.days() > finalDate.days())
			return false;

		// Timespan is the total time to process in each incremental 'gulp'.
		Timespan timespan = new Timespan(startDate, finalDate, increment);

		// Have we exceeded the time stamp increment?
		if (startDate.days() > timespan.endTimestamp().days())
			return false;

		// Convert it to a number of whole days for this time increment. The
		// number of days may vary depending on the length of the increment
		// (month, year, etc) and the start date. This is maximum possible
		// days we'll process in any one gulp.
		int days = timespan.fullDays();

		// Have the time increment exceeded the final date? If so, bail out. 
		if (days == 0) return false;
		
		Tools.message("\n  Time span: "
				+ timespan.startTimestamp().dateString() + " to "
				+ timespan.endTimestamp().dateString());

		Tools.message("    Maximum possible days for this increment = " + days);

		String filename = "";

		// Have we opened the dataset?
		if (data == null)
			openDataset(days);

		// The initial date of the files we are reading.
		Timestamp date = timespan.startTimestamp();

		// Loop through the number of days, reading the data file for each day.
		// If a date doesn't exist, ignore it and move on.
		for (int d = 0; d < days; d++) {
			
			if (date.days() > timespan.endTimestamp().days())
				return false;

			try {

				switch (dataType) {
				case SEA_ICE:
					filename = DatasetSeaIce.getSeaIceFileName(dataPath,
							date.year(), date.month(), date.dayOfMonth(),
							addYearToInputDirectory);

					data[d] = (GriddedVector[][]) ((DatasetSeaIce) dataset)
							.readData(filename, locations, date.id());

					// Success
					fileCount++;

					break;

				case SSMI:
					filename = DatasetSSMI.getSSMIFileName(dataPath,
							date.year(), date.month(), date.dayOfMonth(),
							addYearToInputDirectory, frequency, polarization);

					// Read the data
					data[d] = (GriddedVector[][]) ((DatasetSSMI) dataset)
							.readData(filename, locations, date.id());

					// Success
					fileCount++;

					break;

				case AVHRR:
					/*TODO */

					break;
				}
			} catch (Exception e) {
			}

			// Remove bad data
			data[d] = GriddedVector.filterBadData(data[d], minValue, maxValue,
					NODATA);

			Tools.message(date.yearString() + "." + date.monthString() + "."
					+ date.dayOfMonthString() + "  File name: " + filename);

			// Keep track of the date we just processed.
			Timestamp oldDay = date;

			// Increment the day, skipping over days that are not part
			// of the increment selection.
			date = timespan.nextDay(date);
			
			// Are there more dates to process in this timespan?
			if (date == null) {
				// If this is a multiyear climatology, we've done it all. Quit.
				if (increment.isMultiyear()) {
					return false;
				}
				
				// Not a multiyear climatology. Month-by-Month, e.g.. Keep going.
				else {
					// This timespan is done, but there may be more timespans to process.
					// Pick up where we left off...
					date = oldDay;
					date.incrementOneDay();
					startYear = date.year();
					startMonth = date.month();
					startDay = date.dayOfMonth();
				}
			}
		}
		
		return true;
	}

	/*
	 * openDataset
	 * 
	 * Open a Dataset object, based on the selection of "datatype". The amount
	 * of space allocated for the data is determined by the maximum days value,
	 * which is the time increment used for reading sequential files.
	 */
	protected void openDataset(int maximumDays) {

		String filename = "";

		switch (dataType) {

		case SEA_ICE:
			filename = DatasetSeaIce.getSeaIceFileName(dataPath, startYear,
					startMonth, startDay, addYearToInputDirectory);
			dataset = new DatasetSeaIce(filename);

			getMetadata(filename);

			getLocations();
			
			break;

		case SSMI:
			filename = DatasetSSMI.getSSMIFileName(dataPath, startYear,
					startMonth, startDay, addYearToInputDirectory, frequency,
					polarization);

			dataset = new DatasetSSMI(filename);

			getMetadata(filename);

			getLocations();
			
			break;

		case AVHRR:
			/*TODO
			 * filename = DatasetAVHRR.getAVHRRFileName(dataPath, startYear,
			 * startMonth, startDay, addYearToInputDirectory);
			 * 
			 * dataset = new DatasetAVHRR(filename);
			 * 
			 * getMetadata( filename );
			 * 
			 * getLocations();
			 */

			break;
		}
		data = new GriddedVector[maximumDays][rows][cols];
	}

	/*
	 * getMetadata
	 * 
	 * Got the metadata? If not, go get it from the supplied file.
	 */
	protected void getMetadata(String filename) {

		if (haveMetadata)
			return;

		switch (dataType) {
		case SEA_ICE:
			metadata = dataset.readMetadata(filename);
			break;
		case SSMI:
			metadata = dataset.readMetadata(filename);
			break;
		case AVHRR:
			/* TODO
			 * metadata = dataset.readMetadata( filename ); */
			break;
		}

		rows = dataset.rows();
		cols = dataset.cols();

		haveMetadata = true;

		return;
	}

	/*
	 * makeBaseline
	 * 
	 * Create a climatology baseline using the most recent data time span.
	 */
	protected void makeBaseline() {

		Timestamp firstDate = new Timestamp(lastStartYear, lastStartMonth,
				lastStartDay);
		Timestamp lastDate = new Timestamp(lastStartYear, lastStartMonth,
				(lastStartDay + days) - 1);

		Tools.statusMessage("Climatology: " + firstDate.dateString() + " for "
				+ days + " days (until " + lastDate.dateString() + ")");

		accumulateStats();

		// Write the baseline data to output files.
		String filename = outputPath + climatologyPrefix + dataType.toString()
				+ "_" + increment.toString() + "_" + firstDate.dateString()
				+ ".bin";

		Tools.statusMessage("    output filename = " + filename);
	}

	/*
	 * accumulateStats
	 * 
	 * Find statistics for the data over the temporal increment.
	 */
	protected void accumulateStats() {

		// If we haven't read any files yet, no data to condense.
		if (!haveMetadata) {
			Tools.warningMessage("Climatology::accumulateStats: no metadata");
			return;
		}

		// Generate the reference image. This will be the mean value and
		// standard deviation for the time interval (week, month, season,
		// etc.). mean[0][][] will contain the mean, [1] will be
		// the standard deviation at each [row][col] location.

		if (mean == null)
			mean = new double[rows][cols];
		if (sd == null)
			sd = new double[rows][cols];
		if (population == null)
			population = new int[rows][cols];

		// Zero-out the reference image for this time span.
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				mean[r][c] = 0;
				sd[r][c] = 0;
				population[r][c] = 0;
			}
		}

		// First, accumulate the sum of all data values at each location.
		for (int d = 0; d < days; d++) {

			// Missing data for a day? Skip it.
			if (data[d] == null)
				continue;

			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {

					// Data at this location/date?
					if (data[d][r][c] != null) {
						if (data[d][r][c].data() != NODATA) {
							mean[r][c] += data[d][r][c].data();
							population[r][c]++;
						}
					}

				}
			}
		}

		// Next, find the mean value at each location.
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {

				// Avoid divide by zero if there isn't any data.
				if (population[r][c] > 0) {
					mean[r][c] /= population[r][c];
				} else {
					// No data? Indicate it.
					if (population[r][c] <= 0)
						mean[r][c] = NODATA;
				}
			}
		}

		// Find the standard deviation for the sample population.
		for (int d = 0; d < days; d++) {
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					if (data[d] != null)
						if (data[d][r][c] != null)
							sd[r][c] += Math.pow(data[d][r][c].data()
									- mean[r][c], 2);
				}
			}
		}

		// SD calculation continued...
		// Degrees of freedom. Minus one for unbiased standard deviation.
		double df = days - 1;

		// Odd case... If we're only processing one day, make sure df is not
		// zero.
		if (days <= 1)
			df = 1;

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				sd[r][c] = Math.sqrt(sd[r][c] / df);
			}
		}
	}
}
