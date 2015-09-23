package helper_classes;

/* Main program to experiment with algorithms for condensed data sets.
 */

///import java.sql.Date;
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
		NONE("none"), SEA_ICE("seaice"), SSMI("ssmi"), AVHRR("avhrr");
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
	static Timespan.Increment increment = Timespan.Increment.DAY;
	static Algorithm algorithm = Algorithm.NO_CONDENSATION;
	static DatabaseType databaseType = DatabaseType.H2;

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
	static double threshold = 1.0; // Standard deviations
	static boolean filterBadData = false; // Filter out bad data points using

	static int minValue = -1000000000; // Minimum acceptable data value
	static int maxValue = 1000000000; // Maximum acceptable data value

	// Files and paths for i/o
	static String outputPath = "/Users/mira67/Documents/IceData/nsidc_0001/";
	static String dataPath = "/Users/mira67/Documents/IceData/nsidc_0001/south/";
	static String databaseName;
	static String databasePath = "jdbc:h2:tcp://localhost/~/";
	static String surfaceFile = "";
	static String surfaceLats = "";
	static String surfaceLons = "";

	// Flags
	static boolean createDatabase = true;
	static boolean addDataToDatabase = true;
	static boolean readSurface = false; 	// Read the surface type data file?
	static boolean warningMessages = false; // Receive warning messages?
	static boolean debugMessages = false; 	// Receive debug messages?
	
	// The input files may be stored in subdirectories by year
	static boolean addYearToInputDirectory = true; 	
	
	// Create test images?
	static boolean generateImages = false; // Generate test images

	// SSMI data selection
	static String polarization = "v"; // Horizontal (h) or vertical (v)
	static int frequency = 19; // Frequency of SSMI data

	/*-------------------------------------------------------------------------
	// INTERNAL GLOBAL DATA, NOT FOR USER TWEAKING
	//-----------------------------------------------------------------------*/

	// The image data across the specified time span [day][row][col]
	GriddedVector data[][];

	int days = 0; // Days processed during one time increment

	boolean haveMetadata = false;

	Metadata metadata;

	// Surface type information.
	Metadata surfaceMetadata;
	SurfaceVector surfaceVectors[][];

	// The dataset we're going to read.
	Dataset dataset;

	// Our 'database' of objects (in lieu of an actual database app).
	Database database;

	// The mean provides a reference for deciding whether to condense the
	// newest pixels; i.e., are the pixels varying greater than n*sd from
	// the mean? If so, keep the newest ones and update the reference image
	// with the new pixel values.
	double[][] mean = null;	// Mean: [row][col]
	double[][] sd = null; 	// Standard deviation: [row][col]
	int population[][] = null;

	int rows = 316;
	int cols = 332;

	// Number of files successfully read, for sanity checks
	int fileCount = 0;

	// Gridded image pixel locations.
	GriddedLocation locations[][];

	/*-------------------------------------------------------------------------
	// MAIN PROGRAM
	//-----------------------------------------------------------------------*/

	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();

		// Control what messages we see.
		Tools.setDebug(debugMessages);
		Tools.setWarnings(warningMessages);

		// Check environment variables for default paths.
		if (System.getenv("outputpath") != null)
			outputPath = System.getenv("outputpath");
		if (System.getenv("datapath") != null)
			dataPath = System.getenv("datapath");

		// Read the configuration file. First, check to see if the the config
		// file has been specified with an environment variable. If not, look
		// for the file path (with name) as input on the command line, arg[0].
		String path = System.getenv("configfile");
		if (path == null) {
			configFilename = args[0];
		} else {
			configFilename = path;
		}

		try {
			if (!readConfigFile(configFilename)) {
				Tools.errorMessage("Condense", "main",
						"Could not read the cofiguration file: "
								+ configFilename, new Exception());
			}
		} catch (Exception e) {
			System.out.println(e);
			Tools.message("Error when reading configuration file. Did you specify a full path and name?");
			Tools.message("Example: \"java Condense C:/users/mydir/configfile.txt\"");
			Tools.message("or put the path in an enviroment variable called \"configfile\"");
			Tools.errorMessage("Condense", "main", "", new Exception());
		}

		// If a database name is not specified, create a default one.
		if (databaseName.isEmpty())
			databaseName = dataType.toString();

		new Condense();

		long endTime = System.currentTimeMillis();
		endTime = (endTime - startTime) / 1000;
		Tools.statusMessage("Total time to process = " + endTime + " seconds");
		Tools.statusMessage("End program");
	}

	/*
	 * Condense
	 * 
	 * Condense the data files into a database.
	 */
	Condense() {

			
		switch (databaseType) {
			case RAM:
				database = new DatabaseRamSchema("", dataType.toString());
				break;
			case FILE:
				database = new DatabaseFileSchema(outputPath, dataType.toString());
				break;
			case H2:
				database = new DatabaseH2(databasePath, databaseName);
				break;
		}

		if (createDatabase) {
			// Connect to the database.
			if (!database.connect()) {
				Tools.errorMessage("Condense", "Condense",
						"Could not connect to the database.", new Exception(
								"Giving up."));
			}

			// For development, clean out any tables and data first.
			if (addDataToDatabase) database.clean();

			// Read surface types and coast lines.
			if (readSurface) readSurface();

			// Start and end times.
			Timestamp startDate = new Timestamp(startYear, startMonth, startDay);
			Timestamp finalDate = new Timestamp(finalYear, finalMonth, finalDay);

			// Timespan is the total time we will process.
			Timespan timespan = new Timespan(startDate, finalDate, increment);

			// Quit if there are no days to process.
			if (timespan.days() == 0)
				return;

			Timestamp date = startDate;

			// Read the data files. Stop when we run out of dates.
			while (date != null) {

				readData(date);

				// If we found data, condense it and add it to the database.
				if (data != null) condenseData();

				// Increment the date.
				date = timespan.nextDay(date);
			}

			// Database info for debugging purposes.
			database.status();

			// All done. Close the database.
			database.disconnect();

			// Warm fuzzy feedback.
			Tools.statusMessage("Total data files processed = " + fileCount);
		}

		// We should now have a database full of condensed pixels.
		// Let's generate some images of them...
		if (generateImages)
			generateTestImages();
	}

	/*
	 * readSurface
	 * 
	 * Read the surface data.
	 * 
	 * Special case: the surface type could potentially be used in a
	 * condensation algorithm.
	 */
	protected void readSurface() {
		DatasetSurface datasetSurface = new DatasetSurface();
		surfaceMetadata = datasetSurface.readMetadata(surfaceFile);
		Tools.statusMessage("Condense::condense: reading surface pixels");
		surfaceVectors = datasetSurface.readData(surfaceFile, surfaceLats,
				surfaceLons);
	}

	/*
	 * readData
	 * 
	 * Read a dataset file. Since the different types of data will most likely
	 * have different formats and file names, this method must tailor itself to
	 * the type of data being read.
	 * 
	 * Doesn't care if a file is missing. Assumes the data isn't available.
	 */
	protected void readData(Timestamp date) {

		String filename = "";

		// Get metadata and locations
		if (!haveMetadata) {
			openDataset();
			Tools.message("==> Adding pixel data to the database");
		}

		try {

			data = null;
			switch (dataType) {
				case NONE:
					break;

				case SEA_ICE:
					filename = DatasetSeaIce.getFileName(dataPath,
							date.year(), date.month(), date.dayOfMonth(),
							addYearToInputDirectory);

					data = (GriddedVector[][]) ((DatasetSeaIce) dataset).readData(
							filename, locations, date.id);

					if (filename == null) break;
				
					// Success
					fileCount++;

					break;

				case SSMI:
					filename = DatasetSSMI.getFileName(dataPath, date.year(),
							date.month(), date.dayOfMonth(),
							addYearToInputDirectory, frequency, polarization);

					if (filename == null) break;
				
					// Read the data
					data = (GriddedVector[][]) ((DatasetSSMI) dataset).readData(
							filename, locations, date.id);

					// Success
					fileCount++;

					break;
			
				case AVHRR:
					// todo
					break;
			}
		} catch (Exception e) {
		}

		// Remove bad data
		// /data[d] = GriddedVector.filterBadData(data[d], minValue, maxValue,
		// NODATA);

		// Found data. Add the timestamp to the database.
		if (data != null && addDataToDatabase) {
			date.id = database.storeTimestamp(date);

			Tools.statusMessage(date.yearString() + "." + date.monthString() + "."
					+ date.dayOfMonthString() + "  File name: " + filename);

			// Update the vector data with the timestamp ID
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					data[r][c].timestampID = date.id;
				}
			}
		} else {
			Tools.statusMessage(date.yearString() + "." + date.monthString() + "."
					+ date.dayOfMonthString() + "  No file");
		}
		
	}

	/*
	 * openDataset
	 * 
	 * Get the Metadata and locations.
	 */
	protected void openDataset() {

		String filename = "";

		switch (dataType) {

		case SEA_ICE:
			filename = DatasetSeaIce.getFileName(dataPath, startYear,
					startMonth, startDay, addYearToInputDirectory);
			dataset = new DatasetSeaIce(filename);
			getMetadata(filename);
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
			// todo
			break;

		case NONE:
			break;

		}
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

		// We need to store the metadata in the database.
		Tools.statusMessage("==> Adding metadata to the database.");
		if (addDataToDatabase) database.storeMetadata(metadata);

		haveMetadata = true;

		return;
	}

	/*
	 * getLocations
	 * 
	 * Got the data locations? Metadata must be read first.
	 */
	protected void getLocations() {

		if (!haveMetadata) {
			Tools.errorMessage("Condense", "getLocations",
					"Metadata must be read before retrieving locations.",
					new Exception());
		}

		locations = new GriddedLocation[metadata.rows][metadata.cols];

		switch (dataType) {
		case NONE:
			return;
		case SEA_ICE:
			// TODO
		case SSMI:
			// TODO
			// temporary locations, for testing
			for (int r = 0; r < metadata.rows; r++) {
				for (int c = 0; c < metadata.cols; c++) {
					locations[r][c] = new GriddedLocation(r, c,
							(double) Tools.randomInt(90),
							(double) Tools.randomInt(180));
				}
			}
			break;
		case AVHRR:
			// todo
			break;
		}

		Tools.statusMessage("==> Adding locations to the database.");
		if (addDataToDatabase) database.storeLocationArray(locations);

		return;
	}

	/*
	 * condenseData
	 * 
	 * Condense the most recent data time span.
	 */
	protected void condenseData() {

		switch (algorithm) {

		case NO_CONDENSATION:
			noCondensation();
			break;

		case ALGORITHM1:
			break;
		}
	}

	/*
	 * noCondensation
	 * 
	 * Don't do any condensation. Add all pixels to the database.
	 */
	protected void noCondensation() {
		if (data != null && addDataToDatabase) database.storeVectorArray(data, locations);
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
		metadata = database.getMetadata();
		
		// Database status for debugging purposes.
		database.status();

		// Pixels at imageStartIndex will be used to make an image.
		if (imageStartIndex > metadata.timestamps) {
			Tools.errorMessage("Condense", "generateTestImages",
					"timestamp is out of range", new Exception());
		}

		// Get the list of timestamps in the database.
		ArrayList<Timestamp> timestamps = database.getTimestamps();

		/*
		// For debugging purposes: print out all the timestamps.
		Tools.statusMessage("------- Timestamps");
		Iterator<Timestamp> i = timestamps.iterator();
		while (i.hasNext()) {
			Timestamp t = (Timestamp) i.next();
			t.print();
			Tools.message("");
		}
		Tools.statusMessage("------- End Timestamps");
		*/
		
		Tools.statusMessage( "Create image at timestamp ID = " + imageStartIndex );

		// Get the timestamp for the requested start index. Subtract 1 because
		// ListArrays (timestamps) index from 0.
		Timestamp startTime = timestamps.get(imageStartIndex-1);

		// Get the pixels that have that ID in their timestamp
		ArrayList<GriddedVector> pixList = database.getVectorsAtTime( startTime.id());
		Tools.statusMessage("Pixels: " + pixList.size());

		// Make an integer array out of the pixel data
		int[][] sensorData = GriddedVector.createArrayFromVectorList(metadata.rows,
				metadata.cols, pixList);

		// Make a color table.
		ColorTable colors = new ColorTable();
		colors.prism();

		// Create the image from the pixels.
		Image myImage = new Image();
		RasterLayer layer = new RasterLayer(colors, sensorData);
		myImage.addLayer(layer);

		// If the surface database was read, superimpose it.
		/*
		 * if (readSurface) { ///surfaceDatabase.writeToTextFile(outputPath +
		 * "\\surface.txt"); ArrayList<GriddedVector> surfacePixList =
		 * surfaceDatabase.getVectorsInTimeRange(0, 0); GriddedVector[][]
		 * surfaceData = surfaceDatabase.createArrayFromSensorVectorList(
		 * surfacePixList );
		 * 
		 * RasterLayer layer2 = new RasterLayer( colors, surfaceData );
		 * myImage.addLayer(layer2);
		 * 
		 * // Surface data may be larger than sensor data -- expand the image.
		 * imageRows = datasetSurface.rows(); if (rows > imageRows) imageRows =
		 * rows;
		 * 
		 * imageCols = datasetSurface.cols(); if (cols > imageCols) imageCols =
		 * cols; }
		 */

		// Add a color bar.
		int height = 20;
		int width = 200;
		RasterColorBar bar = new RasterColorBar(0, 0, width, height, colors);
		RasterLayer colorBarLayer = new RasterLayer(bar.getPixels());
		colorBarLayer.name("Color Bar");
		myImage.addLayer(colorBarLayer);

		// Add test pattern?
		// /myImage.addTestPattern("testb");

		// /myImage.printLayerNames();

		// Display the image.
		myImage.display("Time (day): " + startTime.dateString() + "  " + algorithm + " " + threshold,
				metadata.rows, metadata.cols);

		// Grayscale image
		// change the color table?
		colors.grayScale();

		// Display the image.
		myImage.display("Time (day): " + startTime.dateString() + "  " + algorithm + " " + threshold,
				metadata.rows, metadata.cols);

		// Create an output file name for the image.
		String timeString = startTime.yearString() + startTime.monthString()
				+ startTime.dayOfMonthString();
		myImage.savePNG(
				outputPath + timeString + "+" + dataType + "+" + increment
						+ "_" + algorithm + "_" + Double.toString(threshold),
				metadata.rows, metadata.cols);

		// Diagnostics
		int total = rows * cols;
		int allPixels = database.metadata.timestamps * total;
		int storedPixels = database.numberOfVectors();

		Tools.statusMessage("");
		Tools.statusMessage("--------------------------------------------------------------");
		Tools.statusMessage("Data: " + dataType);
		Tools.statusMessage("Date range: " + initialStartYear + "."
				+ initialStartMonth + "." + initialStartDay + " - " + finalYear
				+ "." + finalMonth + "." + finalDay);
		Tools.statusMessage("Algorithm: " + algorithm);
		Tools.statusMessage("Time increment: " + increment);
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
	 * readConfigFile
	 * 
	 * Read parameters from a configuration file.
	 */
	static boolean readConfigFile(String filename) throws Exception {

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

				switch (variable) {
				case ";": // Comment line
				case "!": // Comment line
				case "#": // Comment line
				case "*": // Comment line
					break;
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
					if (value.equals("none"))
						dataType = DataType.NONE;
					if (value.equals("sea_ice"))
						dataType = DataType.SEA_ICE;
					if (value.equals("ssmi"))
						dataType = DataType.SSMI;
					Tools.statusMessage("Data Type = " + dataType);
					break;
				case "timeincrement":
					if (value.equals("week"))
						increment = Timespan.Increment.WEEK;
					if (value.equals("jan"))
						increment = Timespan.Increment.JAN;
					if (value.equals("feb"))
						increment = Timespan.Increment.FEB;
					if (value.equals("mar"))
						increment = Timespan.Increment.MAR;
					if (value.equals("apr"))
						increment = Timespan.Increment.APR;
					if (value.equals("may"))
						increment = Timespan.Increment.MAY;
					if (value.equals("jun"))
						increment = Timespan.Increment.JUN;
					if (value.equals("jul"))
						increment = Timespan.Increment.JUL;
					if (value.equals("aug"))
						increment = Timespan.Increment.AUG;
					if (value.equals("sep"))
						increment = Timespan.Increment.SEP;
					if (value.equals("oct"))
						increment = Timespan.Increment.OCT;
					if (value.equals("nov"))
						increment = Timespan.Increment.NOV;
					if (value.equals("dec"))
						increment = Timespan.Increment.DEC;
					if (value.equals("year"))
						increment = Timespan.Increment.YEAR;
					if (value.equals("djf"))
						increment = Timespan.Increment.DJF;
					if (value.equals("mam"))
						increment = Timespan.Increment.MAM;
					if (value.equals("jja"))
						increment = Timespan.Increment.JJA;
					if (value.equals("son"))
						increment = Timespan.Increment.SON;
					Tools.statusMessage("Time Increment = " + increment);
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
					minValue = Integer.valueOf(value);
					Tools.statusMessage("Low threshold = " + minValue);
					break;
				case "maxvalue":
					maxValue = Integer.valueOf(value);
					Tools.statusMessage("High threshold = " + maxValue);
					break;
				case "createdatabase":
					createDatabase = Boolean.valueOf(value);
					Tools.statusMessage("Create a database = " + createDatabase);
					break;
				case "adddatatodatabase":
					addDataToDatabase = Boolean.valueOf(value);
					Tools.statusMessage("Add data to the database = " + addDataToDatabase);
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
				case "surfacefile":
					surfaceFile = textValue;
					Tools.statusMessage("Surface Data File = " + surfaceFile);
					break;
				case "surfacelats":
					surfaceLats = textValue;
					Tools.statusMessage("Surface Latitudes File = "
							+ surfaceLats);
					break;
				case "surfacelons":
					surfaceLons = textValue;
					Tools.statusMessage("Surface Longitudes File = "
							+ surfaceLons);
					break;
				case "polarization":
					polarization = value;
					Tools.statusMessage("Polarization = " + polarization);
					break;
				case "frequency":
					frequency = Integer.valueOf(value);
					Tools.statusMessage("Frequency = " + frequency);
					break;
				case "readsurface":
					readSurface = Boolean.valueOf(value);
					Tools.statusMessage("Read Surface File = " + readSurface);
					break;
				case "filterbaddata":
					filterBadData = Boolean.valueOf(value);
					Tools.statusMessage("Filter bad data = " + filterBadData);
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