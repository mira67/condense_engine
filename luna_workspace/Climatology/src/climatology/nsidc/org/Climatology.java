package climatology.nsidc.org;

import helper_classes.*;

import java.util.ArrayList;
import java.util.Iterator;

public class Climatology {

	// Data types
	public enum DataType 	{ SEA_ICE ("seaice"), SSMI ("ssmi"), AVHRR ("avhrr");
	    private final String name;       
	    private DataType(String s) {name = s;}
	    public String toString(){return name;}
	}

	static DataType dataType = DataType.SSMI;
	
	static Timespan.Increment increment = Timespan.Increment.MONTH;
	
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
	
	static boolean filterBadData = false;	// Filter out bad data points using minvalue and maxvalue?
	static int minValue = -1000000000;  // Minimum acceptable data value
	static int maxValue =  1000000000;  // Maximum acceptable data value
	
	// Files and paths for i/o
	static String outputPath = "/Users/glgr9602/Desktop/Condense/climatology/"+dataType.toString()+"/"+increment.toString()+"/";
	static String dataPath = "/Users/glgr9602/Desktop/Condense/data/"+dataType.toString()+"/";

	static boolean warningMessages = false;		// Receive warning messages?
    static boolean debugMessages = false;		// Receive debug messages?
    static boolean addYearToInputDirectory = true; // The input files may be stored in subdirectories by year
    
    // SSMI data selection
    static String polarization = "v"; 			// Horizontal (h) or vertical (v)
    static int frequency = 19;		 			// Frequency of SSMI data

    // The image data across the specified time span [day][row][col]
	GriddedVector data[][][];
	
	int days = 0;			// Days processed during one time increment
	
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
    double[][] mean = null;		// Mean: [row][col]
    double[][] sd = null;		// Standard deviation: [row][col]
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
		Tools.setDebug( debugMessages );
		Tools.setWarnings( warningMessages );

    	new Climatology();
    	
		long endTime = System.currentTimeMillis();
        endTime = (endTime - startTime) / 1000;
		Tools.statusMessage("End. Total time to process = " + endTime + " seconds");
	}

	
	/* Climatology
	 * 
	 * Read the data, make climatology baselines (mean and standard deviation).
	 */
	Climatology() {

		// Read the data files. The loop continues until all data files have been read.
		while( readData() ) {
			
			// Condense the data and add it to the database.
			makeBaseline();
			
		}
		
		// Warm fuzzy feedback.
		Tools.statusMessage("Total data files processed = " + fileCount);
	}
	
	
	/* 
	 * readData
	 * 
	 * Read the dataset files.
	 * 
	 * This method reads data files in 'gulps' of the specified time increment
	 * (i.e., week, month, or seasonal, etc.).
	 * 
	 * Returns false if the start day for processing is greater than the final day
	 * (i.e., the ultimate final day as the user specified for processing).
	 * 
	 * Doesn't care if a file is missing. Assumes the data isn't available and plows ahead.
	 */
	protected boolean readData() {

		Timestamp startDate = new Timestamp( startYear, startMonth, startDay);
		Timestamp finalDate = new Timestamp( finalYear, finalMonth, finalDay);

		// Are we already done?
		if (startDate.days() > finalDate.days()) return false;
		
		// Timespan is the total time to process in each incremental 'gulp'.
		Timespan timespan = new Timespan(startDate, increment);
		
		// Convert it to a number of whole days for this time increment. The number of days
		// may vary depending on the length of the increment (month, year, etc) and
		// the start date. This is maximum possible days we'll process in any one gulp.
		int maximumDays = timespan.fullDays();
		
		// If the computed start or end date of the time span is outside of the requested
		// dates, use the requested date instead.
		if (timespan.startTimestamp().days() < startDate.days()) timespan.startTimestamp( startDate );
		if (timespan.endTimestamp().days() > finalDate.days()) timespan.endTimestamp( finalDate );

		// Total days we're processing.
		days = timespan.fullDays();

		Tools.debugMessage("maximum days = " + maximumDays);
		Tools.debugMessage("actual days = " + days);

		String filename = "";

		// Have we opened the dataset?
		if (data == null) openDataset( maximumDays );

		// The initial date of the files we are reading.
		Timestamp date = new Timestamp( startDate.year(), startDate.month(), startDate.dayOfMonth());
		
		// Loop through the number of days, reading the data file for each day.
		// If a date doesn't exist, ignore it and move on.
		for (int d = 0; d < days; d++) {
			
			try {
				
				switch (dataType) {
					case SEA_ICE:
						filename = DatasetSeaIce.getSeaIceFileName(dataPath, date.year(), date.month(),
	    										 date.dayOfMonth(), addYearToInputDirectory);
    					
    					data[d] = (GriddedVector[][]) ((DatasetSeaIce) dataset).readData( filename, locations, date.id() );

    					// Success
    					fileCount++;

	    			
    					break;
    	    			
  					case SSMI:
    						filename = DatasetSSMI.getSSMIFileName(dataPath,
    	    								date.year(), date.month(), date.dayOfMonth(),
    	    								addYearToInputDirectory, frequency, polarization);
    	    			
    						// Read the data
    						data[d] = (GriddedVector[][]) ((DatasetSSMI) dataset).readData( filename, locations, date.id());
    	    					
    						// Success
    						fileCount++;
       				
    	    			break;
    	    			
   					case AVHRR:
    						/*filename = DatasetSSMI.getSSMIFileName(dataPath,
    	    								date.year(), date.month(), date.dayOfMonth(),
    	    								addYearToInputDirectory, frequency, polarization);
    	    			
    						// Read the data
    						data[d] = (GriddedVector[][]) ((DatasetSSMI) dataset).readData( filename, locations, date.id());
    	    					
    						// Success
    						fileCount++;*/
       				
    	    			break;
				}
			} catch(Exception e) {return false;}
			
			// Remove bad data
			///data[d] = GriddedVector.filterBadData(data[d], minValue, maxValue, NODATA);
			
			Tools.statusMessage(date.yearString() + "." + date.monthString() +
					"." + date.dayOfMonthString() + "  File name: " + filename);
			

			// Next day.
			date.incrementOneDay();
		}

		Tools.debugMessage("End of loop. Next date: " + date.dateString() + "\n-----");
		
		// Update the starting date for the next time span.
		startYear = date.year();
		startMonth = date.month();
		startDay = date.dayOfMonth();
				
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
   				getMetadata( filename );
   				break;
			    
   			case SSMI:
   				filename = DatasetSSMI.getSSMIFileName(dataPath, startYear, startMonth,
   						startDay, addYearToInputDirectory, frequency,
   						polarization);

   				dataset = new DatasetSSMI(filename);
    				
   				getMetadata( filename );
    				
   				getLocations();
    				
   				break;			
			    
   			case AVHRR:
   				/*filename = DatasetAVHRR.getAVHRRFileName(dataPath, startYear, startMonth,
   						startDay, addYearToInputDirectory);

   				dataset = new DatasetAVHRR(filename);
    				
   				getMetadata( filename );
    				
   				getLocations();
    				
   				break;*/			
		}
		data = new GriddedVector[maximumDays][rows][cols];	
	}
	
	/* 
	 * getMetadata
	 * 
	 * Got the metadata? If not, go get it from the supplied file.
	 */
	protected void getMetadata( String filename ) {
		
		if (haveMetadata) return;
		
		switch (dataType) {
    		case SEA_ICE:
    			metadata = dataset.readMetadata( filename );
    			break;
    		case SSMI:
    			metadata = dataset.readMetadata( filename );
        		break;
    		case AVHRR:
    			/*metadata = dataset.readMetadata( filename );*/
        		break;
		}
		
		rows = dataset.rows();
		cols = dataset.cols();
		
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
			Tools.errorMessage("Condense", "getLocations", "Metadata must be read before retrieving locations.",
					new Exception());
		}
		
		locations = new GriddedLocation[metadata.rows()][metadata.cols()];
		
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
    					locations[r][c] = new GriddedLocation(r, c, (double) Tools.randomInt(90), (double) Tools.randomInt(180));
    				}
    			}
    			break;
		}
		
		Tools.statusMessage("==> Adding locations to the database.");
		database.storeLocationArray(locations);
		
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

			case TEMPORAL_THRESHOLD:
				temporalThresholdCondensation();
				break;

			case MINMAX:
				minmaxCondensation();
				break;
				
			case SPECIALSAMPLE:
				specialsampleCondenstation();
				break;
		}
    }
	
	/*
	 * noCondensation
	 * 
	 * Don't do any condensation. Add all pixels to the database.
	 */
	protected void noCondensation() {
		for (int d = 0; d < days; d++) {
			database.storeVectorArray( data[d], locations );
		}
	}
	
	/* special sampling
	 *
	 * pick key samples to store
	 */
	protected void specialsampleCondenstation() {
		
		accumulateStats();
		
		//threshold, percentage of data to preserve
		int thr = (int)Math.floor(days*0.1); //out of days (in this trial: days = 120)
		if (thr <= 2){
			thr = 3;
		}
		
		Tools.statusMessage("thr " + thr + " days" + days);
		Number [][] pixel_ts = new Number[days][2];
		Number[][] sampled_ts = new Number[thr][];
		///int locID = 0;
		
		// reform image based data into group of time series for downsampling
		for (int r = 0; r < rows; r++){
			for (int c = 0; c < cols; c++){
				for (int d = 0; d < days; d++){//d<2 to test algorithm 
					if (data[d] != null){
						if (data[d][r][c] != null) { 
							pixel_ts[d][1] = data[d][r][c].data();//create pixel time series
							pixel_ts[d][0] = d;//day stamp
						}
						else{
							pixel_ts[d][1] = data[d-1][r][c].data();
							data[d][r][c] = data[d-1][r][c];//quick fix for missing days data, if day-0 is missing?
							pixel_ts[d][0] = d;//day stamp
						}
					}
				
				}//days
				
				sampled_ts = Downsampling.largestTriangleThreeBuckets(pixel_ts, thr);//condense,can be simplified
				//debug
				Tools.debugMessage("sampled data length = " + sampled_ts.length);
				/*
				for (int downsampled_id = 0; downsampled_id < thr; downsampled_id++){
					database.store( data[sampled_ts[downsampled_id][0].intValue()][r][c].data,locID,cDateList[sampled_ts[downsampled_id][0].intValue()]);//only store sampled data
				}*/
				///locID++;
			}//cols
		}//rows
	}//special sampling method
	
	/* temporalThresholdCondensation
	 * 
	 * Condense the pixels based on a temporal threshold, datarods style.
	 */
	protected void temporalThresholdCondensation() {
		
		accumulateStats();

		// Add data to the database only if it exceeds the specified standard deviation threshold.
		for (int d = 0; d < days; d++) {	
			
			// Any data on this day? If not, skip it.
			if (data[d] == null) continue; 

			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					
					// Any data at this location?
					if (data[d][r][c] != null) { 
						
						// Ignore NaNs and other bad data
						if (Math.abs(data[d][r][c].data()) < Math.abs(NODATA) ) {
						
							// Does the data exceed the threshold (mean+sd*threshold)? If so,
							// store it in the database.
							if (data[d][r][c].data() > (mean[r][c] + sd[r][c]*threshold) ||
								data[d][r][c].data() < (mean[r][c] - sd[r][c]*threshold) ) {

								// Attach the location ID to this vector.
								data[d][r][c].location = locations[r][c];
								
								// Store the vector.
								database.storeVector( data[d][r][c] );		
							}
						}
					}
				}
			}
		}
	}

	
	/* 
	 * minmaxCondensation
	 * 
	 * Condense the pixels to the minimum and maximum values, if they exceed thresholds,
	 * over the course of the temporal increment (typically one week).
	 */
	protected void minmaxCondensation() {
		
		accumulateStats();

		/// r1, c1 for debugging purposes.
		int r1 = 68;
		int c1 = 128;

		// A place to store the condensed pixels, until we're ready
		// to write them to the database.
		GriddedVector min[][] = new GriddedVector[rows][cols];
		GriddedVector max[][] = new GriddedVector[rows][cols];
		
		// Add data to the database only if 1) it's either a minimum or maximum,
		// and 2) it exceeds the specified standard deviation threshold.
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				
				// Initialize the pixels, by default they have no data.
				///min[r][c] = new GriddedVector(r, c);
				min[r][c] = new GriddedVector(locations[r][c]);
				///max[r][c] = new GriddedVector(r, c);
				max[r][c] = new GriddedVector(locations[r][c]);
				
				// Cycle through the days, looking for a min or max value that exceeds
				// the threshold.
				for (int d = 0; d < days; d++) {	

					// Any data on this day? If not, skip it.
					if (data[d] == null) continue; 
						
					// Any data at this location?
					if (data[d][r][c] != null) { 
						
						// Ignore NaNs and other bad data
						if (Math.abs(data[d][r][c].data()) < Math.abs(NODATA) ) {
							
							if (r == r1 && c == c1) Tools.debugMessage(
								"     ---> mean = " + mean[r1][c1] + " sd = " + sd[r1][c1] +
								"   data = " + data[d][r][c].data());

							// Does the data exceed the threshold (mean+sd*threshold)? If so,
							// perhaps store it in the database -- if it's a max value.
							if (data[d][r][c].data() > (mean[r][c] + sd[r][c]*threshold) ) {

								// A new maximum value? Save it.
								if (data[d][r][c].data() > max[r][c].data() ||
									max[r][c].data() == NODATA) max[r][c] = data[d][r][c];
								
							}
							
							// Same thing, for minimum values.
							if (data[d][r][c].data() < (mean[r][c] - sd[r][c]*threshold) ) {

								// A new minimum value? Save it.
								if (data[d][r][c].data() < min[r][c].data() ||
									min[r][c].data() == NODATA) min[r][c] = data[d][r][c];

							}
						}  // data != nodata
					} // data != null
				}  // Day

				if (min[r][c].data() != NODATA) database.storeVector( min[r][c] );		
				if (max[r][c].data() != NODATA) database.storeVector( max[r][c] );
												
			} // Column
		} // Row
	}

	
	/* accumulateStats
	 * 
	 * Find statistics for the data over the temporal increment.
	 */
	protected void accumulateStats() {
		
		// If we haven't read any files yet, no data to condense.
		if (!haveMetadata) return;
		
		// Generate the reference image. This will be the mean value and
		// standard deviation for the time interval (week, month, season,
		// etc.). mean[0][][] will contain the mean, [1] will be 
		// the standard deviation at each [row][col] location.
		
		if (mean == null) mean = new double[rows][cols];
		if (sd == null) sd = new double[rows][cols];
		if (population == null) population = new int[rows][cols];

		
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
			if (data[d] == null) continue;
			
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
				if (population[r][c] > 0 ) {
					mean[r][c] /= population[r][c];
				} else {
					// No data? Indicate it.
					if (population[r][c] <= 0 ) mean[r][c] = NODATA;
				}
			}
		}

		// Find the standard deviation for the sample population.
		for (int d = 0; d < days; d++) {
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					if (data[d] != null)
						if (data[d][r][c] != null)
							sd[r][c] += Math.pow(data[d][r][c].data() - mean[r][c],2);
				}
			}
		}

		// SD calculation continued...
		// Degrees of freedom. Minus one for unbiased standard deviation.
		double df = days - 1;
		
		// Odd case... If we're only processing one day, make sure df
		// is not zero.
		if (days <= 1) df = 1;
		
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				sd[r][c] = Math.sqrt(sd[r][c] / df); 
			}
		}
	}

	
	/* generateTestImages
	 * 
	 * Create images and data from the contents of the database. For testing
	 * purposes only.
	 */
	void generateTestImages() {

		Tools.statusMessage("\nGENERATE TEST IMAGES");

		database.connectReadOnly();

		// Database status for debugging purposes.
		database.status();
		
		// Get the metadata
		metadata = database.getMetadata();
		
		// Pixels between imageStartIndex and imageEndIndex will
		// be used to make an image.
		if (imageStartIndex > metadata.timestamps ||
			imageEndIndex > metadata.timestamps ) {

			Tools.errorMessage("Condense", "generateTestImages", "timestamps are out of range",
					new Exception());
		}
		
		// Get the list of timestamps in the database.
		ArrayList<Timestamp> timestamps = database.getTimestamps();
		
		// For debugging purposes: print out all the timestamps.
		Tools.statusMessage("------- Timestamps");
		Iterator<Timestamp> i = timestamps.iterator();
		while(i.hasNext()) {
			Timestamp t = (Timestamp) i.next();
			t.print();
			Tools.message("");
		}
		Tools.statusMessage("------- End Timestamps");
		
		// The time range of images we want to display...
		// These are NOT the database indexes but rather the nth timestamps
		// in the arraylist of timestamps.
        Timestamp startTime = timestamps.get( imageStartIndex-1 );
        Timestamp endTime = timestamps.get( imageEndIndex-1 );

        Tools.statusMessage("");
		Tools.statusMessage("Create image....  (time index = " + imageStartIndex + " to " + imageEndIndex + ")");

        // Get the pixels for the image
		ArrayList<GriddedVector> pixList = database.getVectors(imageStartIndex, imageEndIndex);
		Tools.statusMessage("Pixels: " + pixList.size());
		
		// For debugging -- print out the contents of the first 10 vectors
		for (int j = 0; j < 10; j++) {
			pixList.get(j).print();
		}
		
		// Make an integer array out of the pixel data
		int[][] sensorData = createArrayFromVectorList( metadata.rows, metadata.cols, pixList );

		// Make a color table.
		ColorTable colors = new ColorTable();
		colors.prism();
		
		// Create the image from the pixels.
		Image myImage = new Image();
    	RasterLayer layer = new RasterLayer( colors, sensorData );
    	myImage.addLayer(layer);

    	// If the surface database was read, superimpose it.
		/*if (readSurface) {
			///surfaceDatabase.writeToTextFile(outputPath + "\\surface.txt");
			ArrayList<GriddedVector> surfacePixList = surfaceDatabase.getVectorsInTimeRange(0, 0);
			GriddedVector[][] surfaceData = surfaceDatabase.createArrayFromSensorVectorList( surfacePixList );

			RasterLayer layer2 = new RasterLayer( colors, surfaceData );
			myImage.addLayer(layer2);
			
			// Surface data may be larger than sensor data -- expand the image. 
			imageRows = datasetSurface.rows();
			if (rows > imageRows) imageRows = rows;
			
			imageCols = datasetSurface.cols();
			if (cols > imageCols) imageCols = cols;
		}*/

		// Add a color bar.
		int height = 20;
		int width = 200;
		RasterColorBar bar = new RasterColorBar(0, 0, width, height, colors);
		RasterLayer colorBarLayer = new RasterLayer( bar.getPixels() );
		colorBarLayer.name("Color Bar");
		myImage.addLayer(colorBarLayer);
		
		// Add test pattern?
		///myImage.addTestPattern("testb");
		
	    ///myImage.printLayerNames();

	    // Display the image.
   		myImage.display( "Time (day): " + startTime.dateString() + " - " + endTime.dateString() +
   				"  " + algorithm + " " + threshold, metadata.rows, metadata.cols );

   		//Grayscale image
   		// change the color table?
   		colors.grayScale();
   		
	    // Display the image.
   		myImage.display( "Time (day): " + startTime.dateString() + " - " + endTime.dateString() +
   				"  " + algorithm + " " + threshold, metadata.rows, metadata.cols );

   		// Create an output file name for the image.
	    String timeString = startTime.yearString() +
		    	startTime.monthString() + startTime.dayOfMonthString();
	    myImage.savePNG( outputPath + timeString + "+" + dataType + "+" + increment + "_" +
		    			 algorithm + "_" + Double.toString(threshold), metadata.rows, metadata.cols );
        
   		
	    // Diagnostics
	    int total = rows*cols;
	    int allPixels = database.metadata.timestamps * total;
	    int storedPixels = database.numberOfVectors();
	    
   		Tools.statusMessage("");
   		Tools.statusMessage("--------------------------------------------------------------");
   		Tools.statusMessage("Data: " + dataType);
   		Tools.statusMessage("Date range: " + initialStartYear + "." + initialStartMonth + "." +
   							initialStartDay + " - " + finalYear + "." + finalMonth + "." + finalDay);
   		Tools.statusMessage("Algorithm: " + algorithm);
   		Tools.statusMessage("Time increment: " + increment);
   		Tools.statusMessage("Threshold: " + threshold);
   		//Tools.statusMessage("Reference image: " + refImage);
   		Tools.statusMessage("Time indicies: " + database.numberOfTimestamps());	
   		Tools.statusMessage("Total pixels in one image:  " + total);     
   		Tools.statusMessage("Total pixels in all images: " + allPixels);
        Tools.statusMessage("Database size (pixels):     " + storedPixels );
        if (allPixels > 0)
        	Tools.statusMessage("Percent of pixels stored:   " + 100f*(float)storedPixels/(float)allPixels );
   		Tools.statusMessage("--------------------------------------------------------------");        
   		Tools.statusMessage(" ");

        database.disconnect();
	}
	
	/*
	 * createArrayFromVectorList
	 * 
	 * Given an array-list of vectors, create an integer array containing only the vectors'
	 * scalar data values at each row/col location.
	 */
	public static int[][] createArrayFromVectorList(int rows, int cols, ArrayList<GriddedVector> list) {
		int[][] array = new int[rows][cols];
		
		Iterator<GriddedVector> i = list.iterator();
		while(i.hasNext()) {
			GriddedVector v = i.next();
			
			if (v.data() != NODATA) {
				if (v.row() < rows && v.col() < cols) {
					array[v.row()][v.col()] = v.data();		
				}
			}
		}
		
		return array;
	}
	
	/* findLocation
	 * 
	 * Search for an index in an array of gridded locations. Warning: returns
	 * null if it doesn't find it.
	 */
	public static GriddedLocation findLocation( GriddedLocation[][] locs, int index ) {
		for (int r = 0; r < locs.length; r++) {
			for (int c = 0; c < locs[0].length; c++) {
				if (locs[r][c].id == index) return locs[r][c];
			}
		}
		
		return null;
	}
	
	/* readConfigFile
	 * 
	 * Read parameters from a configuration file.
	 */
	static boolean readConfigFile( String filename ) throws Exception {
		
		Tools.statusMessage("--------------------------------------------------------------");
		
		try {
			DataFile file = new DataFile( filename );

			Tools.statusMessage("Configuration file: " + filename);
			
			// All the lines in the file.
			ArrayList<String> lines = file.readStrings();
			
			Iterator<String> lineIter = lines.iterator();
			
			// Process the lines in the configuration file.
			while(lineIter.hasNext()) {
				
				// Read the line
				String input = lineIter.next();
				
				// Before cleaning up the string, save any literal text values.
				String textValue = Tools.parseString( input, 1, "=" );
				textValue = textValue.trim();		// Remove any white space at the beginning and end
				
				// Clean up the string: remove white spaces, and make all lower case
				String line = Tools.removeCharacters(input, ' ');
				line = line.toLowerCase();
				
				Tools.debugMessage("  line: " + line);
				
				// Parse out the variable and parameter value
				String variable = Tools.parseString( line, 0, "=" );
				String value = Tools.parseString( line, 1, "=" );
				
				// Handle blank lines.
				if (variable.length() == 0) continue;
				
				switch(variable) {
					case ";":		// Comment line
					case "!":		// Comment line
					case "#":		// Comment line
					case "*":		// Comment line
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
						if (value.equals("none")) dataType = DataType.NONE;
						if (value.equals("sea_ice")) dataType = DataType.SEA_ICE;
						if (value.equals("ssmi")) dataType = DataType.SSMI;
						Tools.statusMessage("Data Type = " + dataType);
						break;
					case "timeincrement":
						if (value.equals("week")) increment = Timespan.Increment.WEEK;
						if (value.equals("month")) increment = Timespan.Increment.MONTH;
						if (value.equals("year")) increment = Timespan.Increment.YEAR;
						if (value.equals("seasonal")) increment = Timespan.Increment.SEASONAL;	
						Tools.statusMessage("Time Increment = " + increment);
						break;
					case "algorithm":
						if (value.equals("none")) algorithm = Algorithm.NO_CONDENSATION;
						if (value.equals("no_condensation")) algorithm = Algorithm.NO_CONDENSATION;
						if (value.equals("temporal")) algorithm = Algorithm.TEMPORAL_THRESHOLD;
						if (value.equals("minmax")) algorithm = Algorithm.MINMAX;
						if (value.equals("blankalg")) algorithm = Algorithm.SPECIALSAMPLE;
						Tools.statusMessage("Algorithm = " + algorithm);
						break;
					case "threshold":
						threshold = Double.valueOf(value);
						Tools.statusMessage("Statistical threshold (sigma) = " + threshold);
						break;
					case "minvalue":
						minValue = Integer.valueOf(value);
						Tools.statusMessage("Low threshold = " + minValue);
						break;
					case "maxvalue":
						maxValue = Integer.valueOf(value);
						Tools.statusMessage("High threshold = " + maxValue);
						break;
					case "debug":
						debugMessages = Boolean.valueOf(value);
						Tools.statusMessage("Debug = " + debugMessages);
						Tools.setDebug( debugMessages );
						break;
					case "warnings":
						warningMessages = Boolean.valueOf(value);
						Tools.statusMessage("Warnings = " + warningMessages);
						Tools.setWarnings( warningMessages );
						break;
					case "addyear":
						addYearToInputDirectory = Boolean.valueOf(value);
						Tools.statusMessage("Add the year to the input directory = " + addYearToInputDirectory);
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
						Tools.statusMessage("Surface Latitudes File = " + surfaceLats);
						break;
					case "surfacelons":
						surfaceLons = textValue;
						Tools.statusMessage("Surface Longitudes File = " + surfaceLons);
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
						Tools.statusMessage("Image start index = " + imageStartIndex);
						break;
					case "imageend":
						imageEndIndex = Integer.valueOf(value);
						Tools.statusMessage("Image end index = " + imageEndIndex);
						break;
					case "database":
					case "databasetype":
						if (value.equals("ram")) databaseType = DatabaseType.RAM;
						if (value.equals("file")) databaseType = DatabaseType.FILE;
						if (value.equals("h2")) databaseType = DatabaseType.H2;
						Tools.statusMessage("Database type: " + databaseType);
						break;
					default:
						Tools.warningMessage("Configuration file line not understood: " + input);
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

}
