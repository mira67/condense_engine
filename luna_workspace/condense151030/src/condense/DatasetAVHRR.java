package condense;

/* DatasetAVHRR
 * 
 * File and data handling for AVHRR data.
 */

public class DatasetAVHRR extends Dataset {


	/*
	 * getFileName
	 * 
	 * Generate the file name for a daily AVHRR file.
	 * 
	 * DOY is "Day of Year", 0-365 or 366
	 */
	public static String getFileName(String path, int year,
			int DOY, boolean addYearToInputDirectory,
			boolean addDayToInputDirectory,
			String channel, String time) {

		// Strings for building the file name.
		String yearString = String.valueOf(year);
		String dayString = String.valueOf(DOY);
		
		if (DOY < 10) dayString = "00" + String.valueOf(DOY);
		if (DOY > 9 && DOY < 100) dayString = "0" + String.valueOf(DOY);

		// Construct the path where the data can be found.
		if (addYearToInputDirectory) {
			path = path + String.valueOf(year) + "/";
		}

		if (addDayToInputDirectory) {
			path = path + dayString + "/";
		}
		
		final String searchString = yearString + dayString + "_" + time + "_" + channel;
		String filename = Tools.findFile(path, searchString);
		
		if (filename == null) {
			Tools.warningMessage("Condense::DatasetAVHRR:getFileName -- Filename not found.");
			Tools.warningMessage("Path: " + path);
			Tools.warningMessage("Time: " + time + "  Channel: " + channel);
		}
		
		return filename;
	}

	/*
	 * getLocations
	 * 
	 * Read the locations for this gridded dataset. latsFile and lonsFile
	 * are the latitude and longitude files.
	 */
	static public GriddedLocation[][] getLocations(String path, String hemisphere) {
		
		int rows = 0;
		int cols = 0;

		// Northern hemisphere
		if (hemisphere.equalsIgnoreCase("north")) {
			rows = 1805;
			cols = 1805;			
		}
		else {
			// Southern hemisphere
			rows = 1605;
			cols = 1605;			
		}
		
		Tools.message("AVHRR Locations:");
		Tools.message("    Hemisphere: " + hemisphere + ", rows = " + rows + ", cols = " + cols);

		GriddedLocation[][] locs = new GriddedLocation[rows][cols];
		GriddedLocation.initialize(locs);
		
		//TODO
		String latsFile = null;
		String lonsFile = null;
		
		//if (latsFile == null) return locs;
		
		// Read the files
		try {
			
			// Open the files
			DataFile latitudes = new DataFile( latsFile );
			DataFile longitudes = new DataFile( lonsFile );

			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
							
					// Read the encoded data from the files
							
					int lat = latitudes.readInt();
					int lon = longitudes.readInt();
		
					// Reverse the byte order (for Windows).
					lat = Tools.reverseByteOrder(lat);
					lon = Tools.reverseByteOrder(lon);

					// Decode the data, convert to doubles.
					double latD = ((double) lat) / 100000.0;
					double lonD = ((double) lon) / 100000.0;
							
					// Create the surface object at this location
					locs[r][c] = new GriddedLocation(r,c,latD,lonD);
				}
			}

			latitudes.close();			
			longitudes.close();			
		}
		catch(Exception e) {
			Tools.warningMessage("DatasetAVHRR::readLocations: Failed to open file(s) in directory: ");
			Tools.warningMessage(path);
			Tools.warningMessage("Filenames: " + latsFile + " and/or " + lonsFile);
		}
		
		return locs;
	}

	/*
	 * readData
	 * 
	 * Read a dataset file. Since the different types of data will most likely
	 * have different formats and file names, this method must tailor itself to
	 * the type of data being read.
	 * 
	 * Doesn't care if a file is missing, assumes the data isn't available.
	 */
	static public short[][] readData(Timestamp date, int rows, int cols,
			String path, boolean addYearToInputDirectory, boolean addDayToInputDirectory,
			String channel, String time) {

		String filename = "";
		short[][] data = new short[rows][cols];
		
		// Read in the data file
		try {
			data = null;

			filename = DatasetAVHRR.getFileName(path, date.year(), date.dayOfYear(),
					addYearToInputDirectory, addDayToInputDirectory, channel, time);

			if (filename == null) {
				Tools.message(date.yearString() + "." + date.monthString() + "."
						+ date.dayOfMonthString() + "  No file");
				return data;
			}

			DataFile file = new DataFile( filename );
			data = file.readShorts2D(rows, cols);
			
			file.close();
		}
		catch (Exception e) {
			// Didn't find the file, or it's bad. Return nothing.
			Tools.message(date.yearString() + "." + date.monthString() + "."
					+ date.dayOfMonthString() + "  DatasetAVHRR::readData: Exception on file read: " + e);
		}
		
		Tools.message(date.yearString() + "." + date.monthString() + "."
					+ date.dayOfMonthString() + "  File name: " + filename);
		
		return data;
	}
}
