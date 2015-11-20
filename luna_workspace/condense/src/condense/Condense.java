package condense;

/* Main program to experiment with algorithms for condensed data sets.
 */

import java.util.ArrayList;
import java.util.Iterator;

public class Condense extends GeoObject {

	/*-------------------------------------------------------------------------
	// USE THE JAVA RUNTIME ARGUMENT TO SET THE CONFIGURATION PATH+FILE
	//-----------------------------------------------------------------------*/

	// File path+filename for the configuration file. Runtime input.
	static String configFilename;

	/*-------------------------------------------------------------------------
	// AVAILABLE TYPES OF DATA AND PROCESSING.
	//-----------------------------------------------------------------------*/

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

	public enum Algorithm {
		NO_CONDENSATION, ALGORITHM1
	}

	public enum DatabaseType {
		RAM, FILE, H2
	}

	/*-------------------------------------------------------------------------
	// DEFAULTS. USE THE CONFIGURATION FILE TO MODIFY THESE VALUES.
	//-----------------------------------------------------------------------*/

	static DataType dataType = DataType.SSMI;
	static Algorithm algorithm = Algorithm.NO_CONDENSATION;
	static DatabaseType databaseType = DatabaseType.H2;
	
	// Monthly or seasonal processing?
	static boolean seasonalFlag = false;
	
	// Start and end dates for the processing.
	static int startYear = 2013;
	static int startMonth = 1;
	static int startDay = 1;

	static int finalYear = 2013;
	static int finalMonth = 1;
	static int finalDay = 31;

	static int initialStartYear = 0;
	static int initialStartMonth = 0;
	static int initialStartDay = 0;

	static int imageStartIndex = 0;
	static int imageEndIndex = 0;

	// What level of anomaly threshold do we want for condensing the data?
	static double threshold = 2.0; // Standard deviations
	
	static short minValue = -10000;			// Minimum acceptable data value
	static short maxValue = 10000;			// Maximum acceptable data value
	static short anomalies = 2;				// Minimum number of adjacent anomalies

	// Files and paths for i/o
	static String outputPath = "";		// Where any output files are put.
	static String dataPath = "";		// Where the data is located
	static String locationsPath = "";	// Where the lat/lon files are located
	static String climatePath = "";		// Location of the climatology files
	static String databaseName;
	static String databasePath = "jdbc:h2:tcp://localhost/~/";

	// Stats for algorithmic processing
	static short[][] mean = null;	// Mean: [row][col]
	static short[][] sd = null; 	// Standard deviation: [row][col]
	static String meanFilename = "none";
	static String sdFilename = "none";
	
	// Flags
	static boolean createDatabase = true;
	static boolean warningMessages = false; // Receive warning messages?
	static boolean debugMessages = false; 	// Receive debug messages?
	
	// The input files may be stored in subdirectories by year and day
	static boolean addYearToInputDirectory = true; 	
	static boolean addDayToInputDirectory = false;	// Day-of-year 	
	
	// Create test images?
	static boolean generateImages = false; // Generate test images

	// SSMI data selection
	static String suffix1 = ""; 	// Frequency of SSMI data
	static String suffix2 = ""; 	// SSMI Polarizataion: (h) or (v)
	static String hemisphere = "south";
	
	// Flag for testing. Intended to shorten the run
	static boolean testing;
	
	// Keep track of data entries
	static int vectors = 0;
	static short timestamps = 0;

	/*-------------------------------------------------------------------------
	// INTERNAL GLOBAL DATA, NOT FOR USER TWEAKING
	//-----------------------------------------------------------------------*/

	// The dataset we're going to read.
	Dataset dataset;

	// Our 'database' of objects (in lieu of an actual database app).
	Database database;

	// No default size of the image files - program must determine.
	int rows = 0;
	int cols = 0;

	// Number of files successfully read, for status and sanity checks
	int fileCount = 0;

	/*-------------------------------------------------------------------------
	// MAIN PROGRAM
	//-----------------------------------------------------------------------*/

	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();

		// Control what messages we see.
		Tools.setDebug(debugMessages);
		Tools.setWarnings(warningMessages);

		// Read the configuration file.
		configFilename = args[0];
		
		try {
			if (!readConfigFile(configFilename)) {
				Tools.errorMessage("Condense", "main",
						"Could not read the cofiguration file: "
								+ configFilename, new Exception());
			}
		} catch (Exception e) {
			System.out.println(e);
			Tools.message("Error when reading configuration file: " + configFilename);
			Tools.errorMessage("Condense", "main", "", new Exception());
		}

		new Condense();

		long endTime = System.currentTimeMillis();
		endTime = (endTime - startTime) / 1000;
		Tools.statusMessage("Total time to process = " + endTime + " seconds");
		double totalHours = endTime /3600.;
		Tools.statusMessage("Total time to process = " + totalHours + " hours");
		Tools.statusMessage("End program");
	}

	/*
	 * Condense
	 * 
	 * Condense the data files into a database.
	 */
	Condense() {
		
		// Connect to the database 
		database = connectToDatabase( databaseType, databaseName,
				createDatabase );

		// Successful database connection? 
		if (database != null) {
			
			// Start and end times for processing.
			Timestamp startDate = new Timestamp(startYear, startMonth, startDay);
			Timestamp finalDate = new Timestamp(finalYear, finalMonth, finalDay);

			// Timespan is the total time we will process.
			Timespan timespan = new Timespan(startDate, finalDate, Timespan.Increment.NONE);
			Tools.message("Total days to process: " + timespan.days());
				
			// Quit if there are no days to process.
			if (timespan.days() == 0)
					return;

			Tools.message("Reading locations");
			GriddedLocation locations[][];
			
			// Get the gridded locations
			locations = getLocations( dataType, locationsPath, hemisphere, suffix1 );						

			// Set the number of rows and columns for the images, based on location array size.
			rows = locations.length;
			cols = locations[0].length;

			// Store the locations
			Tools.message("Storing locations");
			database.storeLocationArray(locations);
			
			// Date is our iterator.
			Timestamp date = startDate;
			
			// An array to hold the daily data
			short[][] data = new short[rows][cols];
				
			Tools.message("Reading data");

			// Read the data files. Stop when we run out of dates.
			while (date != null) {

				// Processing feedback
				fileCount++;
				Tools.messageNoRTLF(fileCount + "/" + timespan.days() + "  ");
				
				switch (dataType) {
				case AVHRR:
					data = DatasetAVHRR.readData(date, hemisphere,
							dataPath, addYearToInputDirectory, addDayToInputDirectory,
							suffix1, suffix2);
					break;
				case SSMI:
					data = DatasetSSMI.readData(date, rows, cols,
							dataPath, addYearToInputDirectory, addDayToInputDirectory,
							suffix1, suffix2);
					break;
				case SEA_ICE:
					break;
				}
				
				// Successfully found data? Condense it and add it to the database.
				if (data != null) {

					// Add the timestamp to the database.
					date.id = database.storeTimestamp(date);	
					timestamps++;
					
					// Condense the data we found
					data = condenseData(date, data, minValue, maxValue);

					// Add the data to the database
					addDataToDatabase( database, data, locations, date.id );	
				}
				
				// Increment the date.
				date = timespan.nextDay(date);			}
		
			// Store the metadata
			Metadata metadata = new Metadata(rows, cols, timestamps, rows*cols, vectors);
			database.storeMetadata( metadata );
			
			// Database info for debugging purposes.
			database.status();

			// All done. Close the database.
			database.disconnect();

			// Warm fuzzy feedback.
			Tools.statusMessage("Total data files attempted = " + fileCount);
			Tools.statusMessage("Total data files processed = " + timestamps);

			// We should now have a wonderful database full of condensed pixels.
			// Let's generate some test images of them...
			if (generateImages)	generateTestImages();
		}
	}
	

	/*
	 * connectToDatabase
	 * 
	 * Attach to an existing database, or create one if it doesn't exist.
	 */
	static public Database connectToDatabase( DatabaseType type, String name,
			boolean create) {

		Database db = null;
		
		// If a database name is not specified, create a default one.
		if (name.isEmpty()) {
			name = dataType.toString() + suffix1 + suffix2 + "." + startYear + finalYear;
		}
				
		switch (type) {
			case RAM:
				db = new DatabaseRamSchema("", dataType.toString());
				break;
			case FILE:
				db = new DatabaseFileSchema(outputPath, dataType.toString());
				break;
			case H2:
				db = new DatabaseH2(databasePath, databaseName);
				break;
		}

		// Create if it doesn't exist?
		if (create) {
			// Connect to the database.
			if (!db.connect()) {
				Tools.errorMessage("Condense", "Condense",
						"Could not connect to the database.", new Exception(
								"Giving up."));
			}
		}	
		
		db.clean();
		
		return db;
	}
	
	
	/*
	 * getLocations
	 * 
	 * Read the locations from the files at "path", return an array of gridded locations.
	 * The "suffix" variable is for additional sensor variables, such as SSMI frequency;
	 * suffix is ignored if not needed (e.g., for AVHRR data).
	 */
	static protected GriddedLocation[][] getLocations(
			DataType type, String path, String hemisphere, String suffix) {

		GriddedLocation[][] locs = null;
		
		switch (type) {
		case SEA_ICE:
			// TODO
			break;
		case SSMI:
			locs = DatasetSSMI.getLocations(path, hemisphere, suffix);
			break;
		case AVHRR:
			locs = DatasetAVHRR.getLocations(path, hemisphere);
			break;
		default:
			break;
		}

		return locs;
	}

	/*
	 * condenseData
	 * 
	 * Condense the most recent data time span.
	 */
	static protected short[][] condenseData( Timestamp day, short data[][],
			short min, short max) {

		if (data == null) return data;
		
		int rows = data.length;
		int cols = data[0].length;
		
		switch (algorithm) {

		case NO_CONDENSATION:
			return noCondensation( data );

		case ALGORITHM1:
			// Read the mean and standard deviation climatology files, if
			// we haven't already. Find the file names...
			
			String increment = null;
			String[] months = {"jan", "feb", "mar", "apr", "may",
					"jun", "jul", "aug", "sep", "oct", "nov", "dec"};
			
			// Increment by month only.
			// TODO: by season, too?
			increment = months[day.month()-1];
			
			// Look for the climatology files
			String newMeanFilename = Tools.findFile(climatePath, suffix1+suffix2+"-mean-"+increment);
			String newSdFilename = Tools.findFile(climatePath, suffix1+suffix2+"-sd-"+increment);

			// Did we not find them?
			if (newMeanFilename == null || newSdFilename == null) {
				Tools.errorMessage("Condense", "CondenseData",
						"Could not find stats files: " + newMeanFilename+
						" or " + newSdFilename, new Exception());
			}

			// Read in the statistical data from these files -- if we haven't already done it.
			if (meanFilename.equals(newMeanFilename) != true) {
				meanFilename = newMeanFilename;
				sdFilename = newSdFilename;

				Tools.message("  Reading climatology files:  (" + 
						suffix1 + suffix2 + ", " + increment + ")" );
				Tools.message("    " + meanFilename);
				Tools.message("    " + sdFilename);
				
				try {
					DataFile file = new DataFile(meanFilename);
					mean = file.readShorts2D( rows, cols);
					file.close();
					
					file = new DataFile(sdFilename);
					sd = file.readShorts2D( rows, cols);
					file.close();						
				}
				catch(Exception e) {
					Tools.errorMessage("Condense", "condenseData", "Could not read stats files", e);
				}
			}
			
			data = Algorithms.algorithm1( data, mean, sd, threshold, anomalies, min, max);
		}

		return data;
	}

	/*
	 * noCondensation
	 * 
	 * Don't do any condensation. Add all pixels to the database.
	 */
	static public short[][] noCondensation( short[][] data ) {
		return data;
	}

	/*
	 * generateTestImages
	 * 
	 * Create images and data from the contents of the database. For testing
	 * purposes only.
	 */
	void generateTestImages() {

		Tools.statusMessage("\nGENERATE TEST IMAGES");

		database.connectReadOnly();

		// Get the metadata
		Metadata metadata = database.getMetadata();
		
		// Database status for debugging purposes.
		database.status();

		// Pixels at imageStartIndex will be used to make an image.
		if (imageStartIndex > metadata.timestamps) {
			Tools.errorMessage("Condense", "generateTestImages",
					"timestamp is out of range", new Exception());
		}

		// Get the list of timestamps in the database.
		ArrayList<Timestamp> timestamps = database.getTimestamps();

		Tools.statusMessage( "Create image at timestamp ID = " + imageStartIndex );

		// Get the timestamp for the requested start index. Subtract 1 because
		// ListArrays (timestamps) index from 0.
		Timestamp startTime = timestamps.get(imageStartIndex-1);

		// Get the pixels that have that ID in their timestamp
		ArrayList<GriddedVector> pixList = database.getVectorsAtTime( startTime.id());
		Tools.statusMessage("Pixels: " + pixList.size());

		// Make an integer array out of the pixel data
		short[][] sensorData = GriddedVector.createArrayFromVectorList(metadata.rows,
				metadata.cols, pixList);

		// Scale the data so that it goes from 0 - 255
		sensorData = Tools.scaleShortArray2D(sensorData, 0, 255);
		
		// Make a color table.
		ColorTable colors = new ColorTable();
		colors.prism();

		// Create the image from the pixels.
		Image myImage = new Image();
		RasterLayer layer = new RasterLayer(colors, sensorData);
		myImage.addLayer(layer);

		// Add a color bar.
		int height = 20;
		int width = 200;
		RasterColorBar bar = new RasterColorBar(0, 0, width, height, colors);
		RasterLayer colorBarLayer = new RasterLayer(bar.getPixels());
		colorBarLayer.name("Color Bar");
		myImage.addLayer(colorBarLayer);

		// Display the image.
		myImage.display("Time (day): " + startTime.dateString() + "  " + algorithm + " " + threshold,
				metadata.rows, metadata.cols);

		// Grayscale image
		colors.grayScale();

		// Display the image.
		myImage.display("Time (day): " + startTime.dateString() + "  " + algorithm + " " + threshold,
				metadata.rows, metadata.cols);

		// Create an output file name for the image.
		String timeString = startTime.yearString() + startTime.monthString()
				+ startTime.dayOfMonthString();
		myImage.savePNG(
				outputPath + timeString + "+" + dataType + "+" +
				"_" + algorithm + "_" + Double.toString(threshold),
				metadata.rows, metadata.cols);

		// Diagnostics
		long total = rows * cols;
		long allPixels = database.metadata.timestamps * total;
		long storedPixels = database.numberOfVectors();

		Tools.statusMessage("");
		Tools.statusMessage("--------------------------------------------------------------");
		Tools.statusMessage("Data: " + dataType);
		Tools.statusMessage("Date range: " + initialStartYear + "."
				+ initialStartMonth + "." + initialStartDay + " - " + finalYear
				+ "." + finalMonth + "." + finalDay);
		Tools.statusMessage("Algorithm: " + algorithm);
		Tools.statusMessage("Threshold: " + threshold);
		// Tools.statusMessage("Reference image: " + refImage);
		Tools.statusMessage("Time indicies: " + database.numberOfTimestamps());
		Tools.statusMessage("Total pixels in one image:  " + total);
		Tools.statusMessage("Total pixels in all images: " + allPixels);
		Tools.statusMessage("Database size (pixels):     " + storedPixels);
		if (allPixels > 0)
			Tools.statusMessage("Percent of pixels stored:   " + 100f
					* (float) storedPixels / (float) allPixels);
		Tools.statusMessage("--------------------------------------------------------------");
		Tools.statusMessage(" ");

		database.disconnect();
	}


	/*
	 * addDataToDatabase
	 * 
	 * Store short integer data in the database.
	 */
	static public void addDataToDatabase( Database db, short[][] data,
			GriddedLocation[][] locations, short timestampID) {
		
		for (int r = 0; r < data.length; r++) {
			for (int c = 0; c < data[0].length; c++) {
				
				if (data[r][c] != NODATA) { 
					db.storeVector(data[r][c], locations[r][c].id, timestampID);
					vectors++;
				}
			}
		}
	}
	
	/*
	 * readConfigFile
	 * 
	 * Read parameters from a configuration file.
	 */
	static protected boolean readConfigFile(String filename) throws Exception {

		Tools.statusMessage("--------------------------------------------------------------");

		try {
			DataFile file = new DataFile(filename);

			Tools.statusMessage("Configuration file: " + filename);

			// All the lines in the file.
			ArrayList<String> lines = file.readStrings();

			Iterator<String> lineIter = lines.iterator();

			// Process the lines in the configuration file.
			while (lineIter.hasNext()) {

				// Read the line
				String input = lineIter.next();

				// Before cleaning up the string, save any literal text values.
				String textValue = Tools.parseString(input, 1, "=");
				textValue = textValue.trim(); // Remove any white space at the
												// beginning and end

				// Clean up the string: remove white spaces, and make all lower
				// case
				String line = Tools.removeCharacters(input, ' ');
				line = line.toLowerCase();

				Tools.debugMessage("  line: " + line);

				// Parse out the variable and parameter value
				String variable = Tools.parseString(line, 0, "=");
				String value = Tools.parseString(line, 1, "=");

				// Handle blank lines.
				if (variable.length() == 0)
					continue;

				// Comment lines
				if (variable.indexOf("*") == 0 ||
						variable.indexOf("#") == 0 ||
						variable.indexOf("!") == 0 ||
						variable.indexOf(";") == 0) continue;
				
				// Process the variable
				switch (variable) {
				case "startyear":
					startYear = Integer.valueOf(value);
					Tools.statusMessage("Start Year = " + startYear);
					// StartYear will vary as the program runs. Store the first
					// start date separately.
					initialStartYear = startYear;
					break;
				case "startmonth":
					startMonth = Integer.valueOf(value);
					Tools.statusMessage("Start Month = " + startMonth);
					initialStartMonth = startMonth;
					break;
				case "startday":
					startDay = Integer.valueOf(value);
					Tools.statusMessage("Start Day = " + startDay);
					initialStartDay = startDay;
					break;
				case "finalyear":
					finalYear = Integer.valueOf(value);
					Tools.statusMessage("Final Year = " + finalYear);
					break;
				case "finalmonth":
					finalMonth = Integer.valueOf(value);
					Tools.statusMessage("Final Month = " + finalMonth);
					break;
				case "finalday":
					finalDay = Integer.valueOf(value);
					Tools.statusMessage("Final Day = " + finalDay);
					break;
				case "datatype":
					if (value.equals("sea_ice"))
						dataType = DataType.SEA_ICE;
					if (value.equals("ssmi"))
						dataType = DataType.SSMI;
					if (value.equals("avhrr"))
						dataType = DataType.AVHRR;
					Tools.statusMessage("Data Type = " + dataType);
					break;
				case "algorithm":
					if (value.equals("none"))
						algorithm = Algorithm.NO_CONDENSATION;
					if (value.equals("no_condensation"))
						algorithm = Algorithm.NO_CONDENSATION;
					if (value.equals("algorithm1"))
						algorithm = Algorithm.ALGORITHM1;
					Tools.statusMessage("Algorithm = " + algorithm);
					break;
				case "threshold":
					threshold = Double.valueOf(value);
					Tools.statusMessage("Statistical threshold (sigma) = "
							+ threshold);
					break;
				case "minvalue":
					minValue = Short.valueOf(value);
					Tools.statusMessage("Low threshold = " + minValue);
					break;
				case "anomalies":
					anomalies = Short.valueOf(value);
					Tools.statusMessage("Minumum number of adjacent anomalies = " + anomalies);
					break;
				case "maxvalue":
					maxValue = Short.valueOf(value);
					Tools.statusMessage("High threshold = " + maxValue);
					break;
				case "createdatabase":
					createDatabase = Boolean.valueOf(value);
					Tools.statusMessage("Create a database = " + createDatabase);
					break;
				case "debug":
					debugMessages = Boolean.valueOf(value);
					Tools.statusMessage("Debug = " + debugMessages);
					Tools.setDebug(debugMessages);
					break;
				case "warnings":
					warningMessages = Boolean.valueOf(value);
					Tools.statusMessage("Warnings = " + warningMessages);
					Tools.setWarnings(warningMessages);
					break;
				case "addyear":
					addYearToInputDirectory = Boolean.valueOf(value);
					Tools.statusMessage("Add the year to the input directory = "
							+ addYearToInputDirectory);
					break;
				case "adddoy":   // Add the day-of-year to the data path
					addDayToInputDirectory = Boolean.valueOf(value);
					Tools.statusMessage("Add the day-of-year to the input directory = "
							+ addDayToInputDirectory);
					break;
				case "datapath":
					dataPath = textValue;
					Tools.statusMessage("Data Path = " + dataPath);
					break;
				case "outputpath":
					outputPath = textValue;
					Tools.statusMessage("Output Path = " + outputPath);
					break;
				case "databasename":
					databaseName = textValue;
					Tools.statusMessage("Database Name = " + databaseName);
					break;
				case "databasepath":
					databasePath = textValue;
					Tools.statusMessage("Database Path = " + databasePath);
					break;
				case "locations":
				case "locationspath":
					locationsPath = textValue;
					Tools.statusMessage("Locations Path = " + locationsPath);
					break;
				case "climate":
				case "climatepath":
				case "statspath":
					climatePath = textValue;
					Tools.statusMessage("Climatology (stats) Path = " + climatePath);
					break;
				case "polarization":
					suffix2 = value;
					Tools.statusMessage("Polarization = " + suffix2);
					break;
				case "frequency":
					suffix1 = value;
					Tools.statusMessage("Frequency = " + suffix1);
					break;
				case "hemisphere":
					hemisphere = value;
					Tools.statusMessage("Hemisphere = " + hemisphere);
					break;
				case "channel":		// AVHRR channel: chn1, chn2, etc. The file name suffix.
					suffix1 = value;
					Tools.statusMessage("Channel = " + suffix1);
					break;
				case "time":			// AVHRR time: 0200 or 1400, also a file name suffix.
					suffix2 = value;
					Tools.statusMessage("Time = " + suffix2);
					break;
				case "seasonal":
					seasonalFlag = Boolean.valueOf(value);
					Tools.statusMessage("Process by season = " + seasonalFlag);
					break;
				case "generateimages":
					generateImages = Boolean.valueOf(value);
					Tools.statusMessage("Generate Images = " + generateImages);
					break;
				case "imagestart":
					imageStartIndex = Integer.valueOf(value);
					Tools.statusMessage("Image start index = "
							+ imageStartIndex);
					break;
				case "imageend":
					imageEndIndex = Integer.valueOf(value);
					Tools.statusMessage("Image end index = " + imageEndIndex);
					break;
				case "database":
				case "databasetype":
					if (value.equals("ram"))
						databaseType = DatabaseType.RAM;
					if (value.equals("file"))
						databaseType = DatabaseType.FILE;
					if (value.equals("h2"))
						databaseType = DatabaseType.H2;
					Tools.statusMessage("Database type: " + databaseType);
					break;					
				case "testing":
					testing = Boolean.valueOf(value);
					Tools.statusMessage("Testing = " + testing);
					break;
				default:
					Tools.warningMessage("Configuration file line not understood: "
							+ input);
					break;

				}

				file.close();
			}
		} catch (Exception e) {
			throw e;
		}

		Tools.statusMessage("--------------------------------------------------------------");

		return true;
	}
}
