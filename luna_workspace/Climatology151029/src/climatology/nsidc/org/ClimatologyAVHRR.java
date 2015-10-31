package climatology.nsidc.org;

import condense.*;

/* ClimatologyAVHRR
 * 
 * Generate baseline climatology data files.
 * 
 * The user selects a time span and increment (monthly, seasonal, same month over multiple
 * years, etc), and the program reads the data, generating statistical files based on the
 * data.
 */

public class ClimatologyAVHRR extends Climatology {

	Dataset.DataType dataType;

	Timespan.Increment increment;

	// Start and end dates for the processing.
	int startYear;
	int startMonth;
	int startDay;
	int startDOY;
	
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

	// SSMI and AVHRR data selection
	String suffix1; // SSMI frequency, or AVHRR channel suffix
	String suffix2; // SSMI polarization, or median time of the AVHRR data, 0200
					// or 1400 Z

	// The image data across the specified time increment [day][row][col]
    short data[][][];
    
	// The accumulated data across all time increments, for stats. [row][col]
	double sums[][] = null;
	double diffSquared[][] = null;
	int population[][] = null;

	// The statistics we're generating: mean and standard deviation.
	double[][] mean = null; // Mean: [row][col]
	double[][] sd = null; // Standard deviation: [row][col]

	// Northern hemisphere
	int rows = 1805;
	int cols = 1805;			

	// Number of files successfully read, for sanity checks
	int fileCount = 0;

	// Files and paths for i/o
	String outputPath;
	String dataPath;

	// Flags for the input path spec.
	boolean addYearToInputDirectory;
	boolean addDayToInputDirectory;
	
	boolean testing;
	
	/*
	 * Climatology
	 * 
	 * Read the data, make climatology baselines (mean and standard deviation).
	 */
	public ClimatologyAVHRR(Dataset.DataType type, int startY, int startM,
			int startD, int finalY, int finalM, int finalD,
			Timespan.Increment inc, String dataP, String outputP, String suff1,
			String suff2, boolean filter,
			boolean addYear, boolean addDay, boolean test) {

		dataType = type;

		startYear = startY;
		startMonth = startM;
		startDay = startD;
		Timestamp ts = new Timestamp(startYear, startMonth, startDay);
		startDOY = ts.dayOfYear();
		
		finalYear = finalY;
		finalMonth = finalM;
		finalDay = finalD;

		increment = inc;

		dataPath = dataP;
		outputPath = outputP;

		suffix1 = suff1;
		suffix2 = suff2;

		// Data range for AVHRR --
		// Special case: different range for albedo and visible channels
		if (suffix1.equalsIgnoreCase("albd") ||
			suffix1.equalsIgnoreCase("chn1") ||
			suffix1.equalsIgnoreCase("chn2")) {
			
			minValue = 1;
			maxValue = 1000;
		}
		else {
			minValue = 1900;
			maxValue = 3100;
		}

		filterBadData = filter;
		
		addYearToInputDirectory = addYear;
		addDayToInputDirectory = addDay;
		
		testing = test;
		
		// Southern hemisphere image size, rows and cols
		if (dataPath.indexOf("south") > 0) {
			rows = 1605;
			cols = 1605;
		}
		
		Tools.statusMessage("  Run: " + dataPath + "\n" + 
			"     suffix1 = " + suff1 + "  suffix2 = " + suff2 + "  increment = " + inc.toString() + "\n" +
			"     min = " + minValue + "  max = " + maxValue + "  rows = " + rows + "  cols = " + cols);
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

		// Create space for the data. Add 1 day for unexpected leap years.
		data = new short[increment.maxDays()+1][rows][cols];
		
		// Create the statistical arrays.
		sums = new double[rows][cols];
		diffSquared = new double[rows][cols];
		population = new int[rows][cols];

		// Two passes. First to calculate the mean, the second to
		// find the standard deviation (which requires the mean).
		for (int pass = 1; pass <= 2; pass++) {

			Tools.message("  PASS " + pass + " Time increment = " + increment.getName());

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

				// Before reading all the data for this timespan, null-out data
				// for each day. This is how we keep track of whether
				// there was data collected on any particular day.
				for (int i = 0; i < data.length; i++) data[i] = null;

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
				mean = Stats.meanNoBadData2d(sums, population, NODATA);

				// TODO Debugging
				try {
					DataFile sumfile = new DataFile();
					sumfile.create(outputPath+"sums-"+suffix1+"-"+increment.getName());
					sumfile.writeDoubles2d(sums);
					sumfile.close();
					DisplayImage("sums-"+suffix1+"-"+increment.getName(), sums);
				}
				catch (Exception e) {}

				// TODO Debugging
				try {
					DataFile popfile = new DataFile();
					popfile.create(outputPath+"population-"+suffix1+"-"+increment.getName());
					popfile.writeInts2d(population);
					popfile.close();
					double[][] pops = Tools.copyFromIntArray(population);
					DisplayImage("population-"+suffix1+"-"+increment.getName(), pops);
				}
				catch (Exception e) {}
				
			}
			if (pass == 2) {
				sd = Stats.standardDeviationNoBadData2d(diffSquared,
						population, (double) NODATA, 1.0);

				// TODO Debugging
				try {
					DataFile popfile = new DataFile();
					popfile.create(outputPath+"sd-"+suffix1+"-"+increment.getName());
					popfile.writeDoubles2d(sd);
					popfile.close();
					DisplayImage("sd-"+suffix1+"-"+increment.getName(), sd);
				}
				catch (Exception e) {}

				// TODO Debugging
				try {
					DataFile popfile = new DataFile();
					popfile.create(outputPath+"diffsquared-"+suffix1+"-"+increment.getName());
					popfile.writeDoubles2d(diffSquared);
					popfile.close();
					DisplayImage("diffsquared-"+suffix1+"-"+increment.getName(), diffSquared);
				}
				catch (Exception e) {}
				
			}
		}

		// Write out the stats to files
		makeStatsFiles(totalTimespan);

		// Warm fuzzy feedback.
		Tools.statusMessage("  Total data files processed = " + fileCount/2);

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
		
		String filename = "";
		DataFile file;
		
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
				filename = DatasetAVHRR.getFileName(dataPath, date.year(), date.dayOfYear(),
						addYearToInputDirectory, true, suffix1, suffix2);

				file = new DataFile(filename);
				
				// Read the data
				data[d] = file.readShorts2D(rows, cols);
				
				file.close();
				
			}
			catch (Exception e) {
				// No file. Do nothing.
			}
			
			// Did we get data?
			if (data[d] != null) {

				// Success
				fileCount++;
				
				// Get rid of any unrealistic data points.
				if (filterBadData) {
					data[d] = Tools.discardBadData(data[d], minValue, maxValue);
				}
			}

			if (filename == null) {
				Tools.message("    " + date.yearString() + "." + date.monthString()
						+ "." + date.dayOfMonthString() + "  File name: "
						+ filename + "    (" + suffix1 + ")");
			}
			else {
				Tools.message("    " + date.yearString() + "." + date.monthString()
						+ "." + date.dayOfMonthString() + "  File name: "
						+ filename);				
			}

			// Next day.
			date = timespan.nextDay(date);
		}
	}
	
	/*
	 * accumulateData
	 * 
	 * Store the data. sdFlag says just accumulate the sums of the data (false)
	 * or squares of the difference of the data compared to the mean (true).
	 */
	protected void accumulateData(short[][][] input, boolean sdFlag) {

		int days = input.length;
		
		// Cycle through all days
		for (int d = 0; d < days; d++) {

			// Missing data for a day? Skip it.
			if (input[d] == null) continue;	

			// Go through all locations on this day
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {

					// Is there data at this location/date?
					if (input[d][r][c] != NODATA) {

						// For accumulating the sums, used for the mean:
						if (!sdFlag) {
							sums[r][c] += input[d][r][c];
							population[r][c]++;
						}

						// Differences squared: (value - mean)^2
						// for standard deviation calculation
						if (sdFlag) {
							diffSquared[r][c] += Math.pow(
									input[d][r][c] - mean[r][c], 2);
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

		Tools.statusMessage("  Climatology::makeStatsFiles: " + firstDate.dateString() + " to "
				+ lastDate.dateString() + " in increments of "
				+ increment.toString() + " (total days = " + totalDays/2 + ")");

		// Warm-fuzzy QA feedback, arbitrary location...
		int r = 52;
		int c = 52;
		Tools.message("Rows,cols = " + rows + "," + cols);
		Tools.message("Population at " + r + "," + c + " = " + population[r][c]);
		Tools.message("Mean at " + r + "," + c + " = " + mean[r][c]);
		Tools.message("Standard deviation at " + r + "," + c + " = " + sd[r][c]);

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
			short[][] shortMean = Tools.doubleArrayToShort(mean);
			file.writeShorts2d(shortMean);
			file.close();

			// Standard deviation climatology file
			filename = outputPath + climatologyPrefix + dataType.toString()
					+ suffix1 + suffix2 + "-sd-" + incName + "-"
					+ firstDate.yearString() + "-" + lastDate.yearString()
					+ ".bin";

			Tools.statusMessage("    sd output filename = " + filename);

			DataFile fileSD = new DataFile();
			fileSD.create(filename);
			short[][] shortSD = Tools.doubleArrayToShort(sd);
			fileSD.writeShorts2d(shortSD);
			fileSD.close();
			
		} catch (Exception e) {
			Tools.warningMessage("Could not open output baseline data file: "
					+ filename);
		}

		Timestamp.dateSeparator(".");

	}
	
static public void DisplayImage( String filename, double[][] array) {

	int rows = array.length;
	int cols = array[0].length;
	
// Create an image obect
Image image = new Image();

// Create the color table
ColorTable ct = new ColorTable();
ct.grayScale();

// Convert the data to integers and scale it for display
int[][] intArray = Tools.doubleArrayToInteger(array);
intArray = Tools.scaleIntArray2D(intArray, 0, 255);

// Create a raster layer for the image, with the data
RasterLayer layer = new RasterLayer( ct, intArray );
image.addLayer(layer);

// Display the image
image.display(filename, rows, cols);

// Save a png version
image.savePNG(filename, rows, cols);
}


}