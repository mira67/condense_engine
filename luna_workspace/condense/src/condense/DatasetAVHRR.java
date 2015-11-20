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
	 * rows
	 * 
	 * Return the number of rows for this hemisphere's imagery.
	 */
	public static int rows( String hemisphere ) {
		if (hemisphere.equalsIgnoreCase("south"))
			return 1605;
		else return 1805;

	}

	/* 
	 * cols
	 * 
	 * Return the number of cols for this hemisphere's imagery.
	 */
	public static int cols( String hemisphere ) {
		if (hemisphere.equalsIgnoreCase("south")) return 1605;
		else return 1805;
	}
	
	/*
	 * 
	 * getLocations
	 * 
	 * Read the locations for this gridded dataset. latsFile and lonsFile
	 * are the latitude and longitude files.
	 */
	static public GriddedLocation[][] getLocations(String path, String hemisphere) {
		
		int rows = rows(hemisphere);
		int cols = cols(hemisphere);
		
		// Don't hard-code? Temporary
		String latsFilename = path + "app_s001_lat2";	// Southern hemisphere, 5km (1605x1605)
		String lonsFilename = path + "app_s001_lon2";	// Southern hemisphere, 5km (1605x1605)

		// Northern hemisphere
		if (hemisphere.equalsIgnoreCase("north")) {
			latsFilename = path + "app_n001_lat2";	// Northern hemisphere, 5km (1805x1805)
			lonsFilename = path + "app_n001_lon2";
		}
		
		Tools.message("AVHRR Locations:");
		Tools.message("    Hemisphere: " + hemisphere + ", rows = " + rows + ", cols = " + cols);

		GriddedLocation[][] locs = new GriddedLocation[rows][cols];
		
		// Read the files
		try {
			
			// Open the files
			DataFile latitudes = new DataFile( latsFilename );
			DataFile longitudes = new DataFile( lonsFilename );

			// Create the locations array
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
	
					// The files are 4x the resolution. We only need every 4th location.
					short lat = latitudes.readShort();
					latitudes.readShort();
					latitudes.readShort();
					latitudes.readShort();

					short lon = longitudes.readShort();
					longitudes.readShort();
					longitudes.readShort();
					longitudes.readShort();
					
					double latD = ((double) lat) / 100.;
					double lonD = ((double) lon) / 100.;
					
					// Create the surface object at this location
					locs[r][c] = new GriddedLocation(r, c, latD, lonD);
				}
			}

			latitudes.close();			
			longitudes.close();
		}
		catch(Exception e) {
			Tools.warningMessage("DatasetAVHRR::readLocations: Failed to open file(s) in directory: ");
			Tools.warningMessage(path);
			Tools.warningMessage("Filenames: " + latsFilename + " and/or " + lonsFilename);
		}
		
		return locs;
	}

	/*
	 * readData
	 * 
	 * Read an AVHRR dataset file.
	 * 
	 * Doesn't care if a file is missing, assumes the data isn't available.
	 */
	static public short[][] readData(Timestamp date, String hemisphere,
			String path, boolean addYearToInputDirectory, boolean addDayToInputDirectory,
			String channel, String time) {

		int rows = rows(hemisphere);
		int cols = cols(hemisphere);
		
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
