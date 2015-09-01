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

	static Timespan.Increment increment = Timespan.Increment.MAM;

	// Start and end dates for the processing.
	// A data file for the start date *must* exist because that will be used
	// to generate the metadata.
	static int startYear = 2012;
	static int startMonth = 1;
	static int startDay = 30;

	static int finalYear = 2013;
	static int finalMonth = 10;
	static int finalDay = 30;

	static int lastStartYear = 0;
	static int lastStartMonth = 0;
	static int lastStartDay = 0;

	// The entire timespan we'll process
	Timestamp startDate;
	Timestamp finalDate;
	Timespan totalTimespan;
	
	// This is the date we are processing.
	Timestamp date;
	
	static boolean filterBadData = false; 	// Filter out bad data points
	
	static final int minValue = -1000000000;	// Minimum acceptable data value
	static final int maxValue = 1000000000;		// Maximum acceptable data value

	// Files and paths for i/o
	
	//...WINDOWS
	static final String outputPath = "/Users/glgr9602/Desktop/condense/climatology/" +
			dataType.toString() + "/";
	static final String dataPath = "/Users/glgr9602/Desktop/condense/data/" +
			dataType.toString() + "/daily/";
	
	// ...LINUX
	/*
	static final String outputPath = "/home/glgr9602/condense/climatology/" +
	/
			dataType.toString() + "/" + increment.toString() + "/";
	static final String dataPath = "/home/glgr9602/condense/data/" +
			dataType.toString() + "/daily/";
	*/

	static final String climatologyPrefix = "climate-";
	
	static boolean warningMessages = true; // Receive warning messages?
	static boolean debugMessages = false; // Receive debug messages?
	static boolean addYearToInputDirectory = true; // The input files may be
													// stored in subdirectories
													// by year

	// SSMI data selection
	static String polarization = "v"; // Horizontal (h) or vertical (v)
	static int frequency = 22; // Frequency of SSMI data

	// The image data across the specified time span [day][row][col]
	GriddedVector data[][][];

	boolean haveMetadata = false;
	Metadata metadata;

	// The dataset we're going to read.
	Dataset dataset;

	// Gridded pixel locations.
	GriddedLocation locations[][];

	// The mean provides a reference for deciding whether to condense the
	// newest pixels; i.e., are the pixels varying greater than n*sd from
	// the mean? If so, keep the newest ones and update the reference image
	// with the new pixel values.
	double[][] mean = null;	// Mean: [row][col]
	double[][] sd = null;	// Standard deviation: [row][col]
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

		// What's the total time span we're going to process?
		Timestamp.dateSeparator(".");
		startDate = new Timestamp(startYear, startMonth, startDay);
		finalDate = new Timestamp(finalYear, finalMonth, finalDay);
		totalTimespan = new Timespan(startDate, finalDate, Timespan.Increment.NONE);
		
		// What is our first date to process? Note that, depending on the
		// choice of increment, the first processing day may not be on the 
		// first day specified (date != startDate). Ask the timespan for the
		// logical first date to process.
		date = totalTimespan.startTimestamp();

		Tools.message("  Specified time span: "
				+ startDate.dateString() + " to "
				+ finalDate.dateString());

		// Have we opened the dataset?
		if (data == null)
			openDataset(increment.maxDays());
		
		// Read the data files. Iterate by year.
		for (int y = startDate.year(); y <= finalDate.year(); y++) {
			
			Timespan timespan = new Timespan(date, finalDate, increment);

			// Timespan may have adjusted itself to comply with the increment.
			// If it exceeded the end date, break.
			if (timespan.startTimestamp().days() > finalDate.days()) break;
			
			Tools.message("    Increment time span: " +
					timespan.startTimestamp().dateString() + " to " +
					timespan.endTimestamp().dateString() + "  days = " +
					timespan.days());
			
			readData( timespan );
			
			// Acts as both an accumulator and the final stats calculator.
			calculateStats();

			// Advance the date to next year.
			date = new Timestamp(timespan.startTimestamp().year()+1, 1, 1);
		}

		// Write the stats to files
		makeStatsFiles(totalTimespan);
		
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
	 * This method reads data files for the entire specified timespan.
	 * 
	 * Doesn't care if a file is missing. Assumes the data isn't available and
	 * plows ahead.
	 */
	protected void readData(Timespan timespan) {

		// Have we opened the dataset?
		if (data == null) {
			Tools.errorMessage("Climatology", "readData", "dataset not open",
					null);
		}

		String filename = "";

		// The number of days we're going to process in this increment.
		int days = timespan.days();

		// Starting date for processing
		date = timespan.startTimestamp();

		// Loop through the days, reading the data file for each day.
		for (int d = 0; d < days; d++) {

			if (date == null) {
				Tools.warningMessage("Unexpected NULL date in Climatology::readData");
				return;
			}
			
			// If a file doesn't exist, ignore it and move on.
			try {
				switch (dataType) {
				case SEA_ICE:
					filename = DatasetSeaIce.getFileName(dataPath, date.year(),
							date.month(), date.dayOfMonth(),
							addYearToInputDirectory);

					data[d] = (GriddedVector[][]) ((DatasetSeaIce) dataset)
							.readData(filename, locations, date.id());

					// Success
					fileCount++;

					break;

				case SSMI:
					filename = DatasetSSMI.getFileName(dataPath, date.year(),
							date.month(), date.dayOfMonth(),
							addYearToInputDirectory, frequency, polarization);

					// Read the data
					data[d] = (GriddedVector[][]) ((DatasetSSMI) dataset)
							.readData(filename, locations, date.id());

					// Success
					fileCount++;

					break;

				case AVHRR:
					// TODO
					break;
				}
			} catch (Exception e) {
				// No file. Do nothing.
			}

			Tools.message(date.yearString() + "." + date.monthString() + "."
					+ date.dayOfMonthString() + "  File name: " + filename);

			// Next day.
			date = timespan.nextDay(date);
		}
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
			filename = DatasetSeaIce.getFileName(dataPath, startYear,
					startMonth, startDay, addYearToInputDirectory);
			dataset = new DatasetSeaIce(filename);

			getMetadata(filename);

			getLocations();
			
			break;

		case SSMI:
			filename = DatasetSSMI.getFileName(dataPath, startYear,
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
		
		// Make the data array. Add 1 for possible leap year when
		// processing multiple years.
		data = new GriddedVector[maximumDays+1][rows][cols];
	}

	/*
	 * getMetadata
	 * 
	 * Got the metadata? If not, go get it from the supplied file.
	 */
	protected void getMetadata(String filename) {

		if (haveMetadata)
			return;

		metadata = dataset.readMetadata(filename);

		rows = dataset.rows();
		cols = dataset.cols();

		haveMetadata = true;

		return;
	}

	/*
	 * makeStatsFiles
	 * 
	 * Create a climatology baseline using the most recent data time span.
	 */
	protected void makeStatsFiles(Timespan t) {

		Timestamp firstDate = t.startTimestamp();
		Timestamp lastDate = t.endTimestamp();
		int days = t.days();
		
		Tools.statusMessage("Climatology: " + firstDate.dateString() + " for "
				+ days + " days (until " + lastDate.dateString() + ")");

		String filename = "no name";
		Timestamp.dateSeparator("");

		// Write the baseline data to output files.
		try {
			// Mean baseline climatology file
			filename = outputPath + climatologyPrefix + dataType.toString()
					+ "-" + increment.toString() + "-mean-" + firstDate.dateString()
					+ ".bin";

			Tools.statusMessage("    mean output filename = " + filename);
			
			DataFile file = new DataFile();
			file.create(filename);
			file.writeDouble2d(mean);
			file.close();
			
			// Standard deviation climatology file
			filename = outputPath + climatologyPrefix + dataType.toString()
					+ "-" + increment.toString() + "-sd-" + firstDate.dateString()
					+ ".bin";

			Tools.statusMessage("    sd output filename = " + filename);
			
			file = new DataFile();
			file.create( filename );
			file.writeDouble2d(sd);
			file.close();
		}
		catch(Exception e) {
			Tools.warningMessage("Could not open output baseline data file: " +
					filename);
		}

		Timestamp.dateSeparator(".");
	}
	
	/* calculateStats
	 * 
	 * Find statistics for the data. Also acts as an accumulator.
	 */
	protected void calculateStats() {
		
		// If we haven't read any files yet, no data to condense.
		if (!haveMetadata) {
			Tools.warningMessage("Climatology::calculateStats: no metadata");
			return;
		}

		// Number of days in the data array
		int days = data.length;
		
		// First time through? Initialize things.
		if (mean == null) {
			mean = new double[rows][cols];
			sd = new double[rows][cols];
			population = new int[rows][cols];

			// Zero-out the stats arrays.
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					mean[r][c] = 0;
					sd[r][c] = 0;
					population[r][c] = 0;
				}
			}
		}

		// Accumulate the sum of all data values at each location.
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
