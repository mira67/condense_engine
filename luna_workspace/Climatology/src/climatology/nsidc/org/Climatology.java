package climatology.nsidc.org;

import condense.*;

/* Climatology
 * 
 * Generate baseline climatology data files.
 * 
 * The user selects a time span and increment (monthly, seasonal, same month over multiple
 * years, etc), and the program reads the data, generating statistical files based on the
 * data.
 */

public class Climatology extends GeoObject {

	Dataset.DataType dataType;

	Timespan.Increment increment;

	// Start and end dates for the processing.
	// A data file for the start date *must* exist because that will be used
	// to generate the metadata.
	int startYear;
	int startMonth;
	int startDay;

	int finalYear;
	int finalMonth;
	int finalDay;

	// The entire timespan we'll process
	Timestamp startDate;
	Timestamp finalDate;
	Timespan totalTimespan;
	int totalDays = 0;

	// This is the date we are processing.
	Timestamp date;

	static boolean filterBadData; // Filter out bad data points

	static int minValue; // Minimum acceptable data value
	static int maxValue; // Maximum acceptable data value

	static final String climatologyPrefix = "climate-";

	static boolean warningMessages = true; // Receive warning messages?
	static boolean debugMessages = false; // Receive debug messages?
	static boolean addYearToInputDirectory = true; // The input files may be
													// stored in subdirectories
													// by year

	// SSMI and AVHRR data selection
	String suffix1; // SSMI frequency, or AVHRR channel suffix
	String suffix2; // SSMI polarization, or median time of the AVHRR data, 0200
					// or 1400 Z

	// The image data across the specified time increment [day][row][col]
	GriddedVector data[][][];

	// The accumulated data across all time increments. [row][col]
	double accumulator[][] = null;
	int population[][] = null;

	boolean haveMetadata = false;
	Metadata metadata;

	// The dataset we're going to read.
	Dataset dataset;

	// The mean provides a reference for deciding whether to condense the
	// newest pixels; i.e., are the pixels varying greater than n*sd from
	// the mean? If so, keep the newest ones and update the reference image
	// with the new pixel values.
	double[][] mean = null; // Mean: [row][col]
	double[][] sd = null; // Standard deviation: [row][col]

	int rows = 0;
	int cols = 0;

	// Number of files successfully read, for sanity checks
	int fileCount = 0;

	// Files and paths for i/o
	String outputPath;
	String dataPath;

	/*
	 * Climatology
	 * 
	 * Read the data, make climatology baselines (mean and standard deviation).
	 */
	public Climatology(Dataset.DataType type, int startY, int startM,
			int startD, int finalY, int finalM, int finalD,
			Timespan.Increment inc, String dataP, String outputP, String suff1,
			String suff2, double min, double max, boolean filter) {

		dataType = type;

		startYear = startY;
		startMonth = startM;
		startDay = startD;

		finalYear = finalY;
		finalMonth = finalM;
		finalDay = finalD;

		increment = inc;

		dataPath = dataP;
		outputPath = outputP;

		suffix1 = suff1;
		suffix2 = suff2;

		minValue = (int) Math.round(min);
		maxValue = (int) Math.round(max);

		filterBadData = filter;
	}

	/*
	 * run
	 * 
	 * Generate the climatology.
	 */
	public boolean run() {

		// What's the total time span we're going to process?
		Timestamp.dateSeparator(".");
		startDate = new Timestamp(startYear, startMonth, startDay);
		finalDate = new Timestamp(finalYear, finalMonth, finalDay);
		totalTimespan = new Timespan(startDate, finalDate,
				Timespan.Increment.NONE);

		// Have we opened the dataset?
		if (data == null) {
			// If it fails to open, return false.
			if (!openDataset(increment.maxDays()))
				return false;
		}
		// Two passes. First to calculate the mean, the second to
		// find the standard deviation (which requires the mean).
		for (int pass = 1; pass < 3; pass++) {

			Tools.message("  PASS " + pass);

			// What is our first date to process? Note that, depending on the
			// choice of increment, the first processing day may not be on the
			// first day specified (date != startDate). Ask the timespan for the
			// logical first date to process.
			date = totalTimespan.startTimestamp();

			// Read the data files. Iterate by year.
			for (int y = startDate.year(); y <= finalDate.year(); y++) {

				Timespan timespan = new Timespan(date, finalDate, increment);

				// Timespan may have adjusted itself to comply with the
				// increment. If it exceeded the end date, break.
				if (timespan.startTimestamp().days() > finalDate.days())
					break;

				Tools.message("    Increment time span: "
						+ timespan.startTimestamp().dateString() + " to "
						+ timespan.endTimestamp().dateString() + "  days = "
						+ timespan.days());

				readData(timespan);

				// Store the data for later statistical calculations.
				accumulateData(data, (pass == 2));

				// Advance the date to next year.
				date = new Timestamp(timespan.startTimestamp().year() + 1, 1, 1);
			}

			// Calculated the statistics. First time through, the mean; second
			// time through, standard deviation.
			Tools.message("  CALCULATING STATS");

			if (pass == 1) {
				mean = Stats.meanNoBadData2d(accumulator, population, NODATA);

				// Zero-out the data accumulator
				accumulator = null;
			}

			if (pass == 2)
				sd = Stats.standardDeviationNoBadData2d(accumulator,
						population, NODATA, 1);
		}

		// Write out the stats to files
		makeStatsFiles(totalTimespan);

		// Warm fuzzy feedback.
		Tools.statusMessage("  Total data files processed = " + fileCount);

		return true;
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

		// Keep track of how many days, in total, we've processed.
		totalDays += days;

		// Starting date for processing
		date = timespan.startTimestamp();

		// Loop through the days, reading the data file for each day.
		for (int d = 0; d < days; d++) {

			data[d] = null;

			// Make sure we have a starting date.
			if (date == null) {
				Tools.warningMessage("Unexpected NULL date in Climatology::readData");
				return;
			}

			// Read the data from a file. If a file doesn't exist, just move on.
			try {
				switch (dataType) {
				case SEA_ICE:
					filename = DatasetSeaIce.getFileName(dataPath, date.year(),
							date.month(), date.dayOfMonth(),
							addYearToInputDirectory);

					data[d] = (GriddedVector[][]) ((DatasetSeaIce) dataset)
							.readData(filename,
									new GriddedLocation[rows][cols], date.id());

					break;

				case SSMI:
					filename = DatasetSSMI.getFileName(dataPath, date.year(),
							date.month(), date.dayOfMonth(),
							addYearToInputDirectory, suffix1, suffix2);

					// Read the data
					data[d] = (GriddedVector[][]) ((DatasetSSMI) dataset)
							.readData(filename,
									new GriddedLocation[rows][cols], date.id());
					break;

				case AVHRR:
					filename = DatasetAVHRR.getFileName(dataPath, date.year(), date.dayOfYear(),
							addYearToInputDirectory, true, suffix1, suffix2);

					// Read the data
					data[d] = (GriddedVector[][]) ((DatasetAVHRR) dataset)
							.readData(filename,
									new GriddedLocation[rows][cols], date.id());
					break;
				}
			} catch (Exception e) {
				// No file. Do nothing.
			}
			

			// Did we get data?
			if (data[d] != null) {

				// Success
				fileCount++;
				
				// Get rid of any unrealistic data points.
				if (filterBadData) {
					data[d] = GriddedVector.filterBadData(data[d],
							minValue, maxValue, NODATA);
				}
			}

			Tools.message("    " + date.yearString() + "." + date.monthString()
					+ "." + date.dayOfMonthString() + "  File name: "
					+ filename);

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
	protected boolean openDataset(int maximumDays) {

		String filename = "";

		switch (dataType) {

		case SEA_ICE:
			filename = DatasetSeaIce.getFileName(dataPath, startYear,
					startMonth, startDay, addYearToInputDirectory);
			dataset = new DatasetSeaIce(filename);

			break;

		case SSMI:
			filename = DatasetSSMI.getFileName(dataPath, startYear, startMonth,
					startDay, addYearToInputDirectory, suffix1, suffix2);

			dataset = new DatasetSSMI(filename, "");

			break;

		case AVHRR:
			filename = DatasetAVHRR.getFileName(dataPath, startYear, startDay,
					addYearToInputDirectory, true, suffix1, suffix2);

			dataset = new DatasetAVHRR(filename, "");

			break;
		}

		// Get the metadata
		if (!getMetadata(filename))
			return false;

		// Make the data array. Plus one for yearly-averaging a leap-year. Unlikely, but possible.
		data = new GriddedVector[maximumDays+1][rows][cols];

		return true;
	}

	/*
	 * getMetadata
	 * 
	 * Got the metadata? If not, go get it from the supplied file.
	 */
	protected boolean getMetadata(String filename) {

		if (haveMetadata)
			return true;

		metadata = dataset.readMetadata(filename);

		if (metadata == null)
			return false;

		rows = dataset.rows();
		cols = dataset.cols();

		haveMetadata = true;

		return true;
	}

	/*
	 * accumulateData
	 * 
	 * Store the data. sdFlag says just accumulate the sums of the data (false)
	 * or squares of the difference of the data compared to the mean (true).
	 */
	protected void accumulateData(GriddedVector[][][] input, boolean sdFlag) {

		// Number of days in the data array
		int days = input.length;

		// First time through? Initialize things.
		if (accumulator == null) {

			accumulator = new double[rows][cols];
			population = new int[rows][cols];

			// Zero-out the array.

			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					accumulator[r][c] = 0;
					population[r][c] = 0;
				}
			}
		}

		// Cycle through all days
		for (int d = 0; d < days; d++) {

			// Missing data for a day? Skip it.
			if (input[d] == null)
				continue;

			// Go through all locations on this day
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {

					// Is there data at this location/date?
					if (input[d][r][c] != null) {
						if (input[d][r][c].data() != NODATA) {

							// For accumulating data sums:
							if (!sdFlag) {
								accumulator[r][c] += input[d][r][c].data();
								population[r][c]++;
							}

							// For accumulating data differences squared (useful
							// for standard
							// deviation calculation):
							if (sdFlag) {
								accumulator[r][c] += Math.pow(
										input[d][r][c].data() - mean[r][c], 2);
								population[r][c]++;
							}
						}
					}
				}
			}
		}
	}

	/*
	 * makeStatsFiles
	 * 
	 * Create a climatology baseline using the most recent data time span.
	 */
	protected void makeStatsFiles(Timespan t) {

		// Use the dates to make file names
		Timestamp firstDate = t.startTimestamp();
		Timestamp lastDate = t.endTimestamp();

		Tools.statusMessage("  Climatology: " + firstDate.dateString() + " to "
				+ lastDate.dateString() + " in increments of "
				+ increment.toString() + " (total days = " + totalDays + ")");

		String filename = "no name";
		Timestamp.dateSeparator("");

		String incName = increment.toString().toLowerCase();

		// Write the baseline data to output files
		try {
			// Mean baseline climatology file
			filename = outputPath + climatologyPrefix + dataType.toString()
					+ suffix1 + suffix2 + "-mean-" + incName + "-"
					+ firstDate.yearString() + "-" + lastDate.yearString()
					+ ".bin";

			Tools.statusMessage("    mean output filename = " + filename);

			DataFile file = new DataFile();
			file.create(filename);
			file.writeDouble2d(mean);
			file.close();

			// Standard deviation climatology file
			filename = outputPath + climatologyPrefix + dataType.toString()
					+ suffix1 + suffix2 + "-sd-" + incName + "-"
					+ firstDate.yearString() + "-" + lastDate.yearString()
					+ ".bin";

			Tools.statusMessage("    sd output filename = " + filename);

			file = new DataFile();
			file.create(filename);
			file.writeDouble2d(sd);
			file.close();
		} catch (Exception e) {
			Tools.warningMessage("Could not open output baseline data file: "
					+ filename);
		}

		Timestamp.dateSeparator(".");

		// Warm-fuzzy check...
		int r = 50;
		int c = 50;
		Tools.message("Rows,cols = " + rows + "," + cols);
		Tools.message("Population at " + r + "," + c + " = " + population[r][c]);
		Tools.message("Mean at " + r + "," + c + " = " + mean[r][c]);
		Tools.message("Standard deviation at " + r + "," + c + " = " + sd[r][c]);
	}
}
