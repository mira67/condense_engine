package condense;

/* DatasetSSMI
 * 
 * File and data handling for SSMI data.
 */

import java.io.File;
import java.io.FilenameFilter;

public class DatasetSSMI extends Dataset {

	/*
	 * getFileName
	 * 
	 * Generate the file name for a daily SSMI file.
	 */
	public static String getFileName(String path, int year, int month,
			int day, boolean addYearToInputDirectory, String frequency,
			String polarization) {

		if (addYearToInputDirectory) {
			path = path + String.valueOf(year) + "/";
		}
		
		// Strings for building the file name.
		String yearString = String.valueOf(year);
		String monthString = String.valueOf(month);
		
		// Pad dates with zeros
		if (month < 10)
			monthString = "0" + String.valueOf(month);
		
		String dayString = String.valueOf(day);
		
		if (day < 10)
			dayString = "0" + String.valueOf(day);

		final String date = yearString + monthString + dayString;

		final String channel = frequency + polarization;

		// Search for this date in the directory of file names.
		File dir = new File(path);

		File[] matches = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.contains(date) && name.contains(channel);
			}
		});

		// Did we find a file with that date? If yes, use the first occurrence.
		if (matches != null) {
			if (matches.length != 0)
				return matches[0].getPath();
		}

		// Nothing. 
		return null;
	}

	/*
	 * getLocations
	 * 
	 * Read the locations for this gridded dataset.
	 */
	static public GriddedLocation[][] getLocations(String path, String hemisphere, String frequency) {

		int rows = 0;
		int cols = 0;
		
		// Determine the size of the image files...
		// Southern hemisphere
		if (hemisphere.equalsIgnoreCase("south")) {
			if (frequency.equalsIgnoreCase("85") ||
				frequency.equalsIgnoreCase("91")) {
				
				// 85 and 91 GHz
				rows = 664;
				cols = 632;				
			}
			else {
		 		// Southern hemisphere,	everything else (19, 22, 37 GHz)
				rows = 332;
				cols = 316;				
			}
		}
		
		// Northern hemisphere
		else {
			if (frequency.equalsIgnoreCase("85") ||
				frequency.equalsIgnoreCase("91")) {
				
					// 85 and 91 GHz
					rows = 896;
					cols = 608;
			}
			else {
		 		// Northern hemisphere,	everything else (19, 22, 37 GHz)
				rows = 448;
				cols = 304;	
			}
		}
		
		Tools.message("SSMI Locations:");
		Tools.message("    Hemisphere: " + hemisphere + ", Frequency: " + frequency + ", rows = " + rows + ", cols = " + cols);
		
		GriddedLocation[][] locs = new GriddedLocation[rows][cols];
		locs = GriddedLocation.initialize(locs);
		
		// Temporary file name hard-code until we have time for something better. :-(
		
		// 25km resolution grid cells
		String latsFile = path + "pss25lats_v3.dat";
		String lonsFile = path + "pss25lons_v3.dat";
		
		// 12.5km resolution grid cells
		if (rows > 332 ) {
			latsFile = path + "pss12lats_v3.dat";
			lonsFile = path + "pss12lons_v3.dat";
		}
		
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
			Tools.warningMessage("DatasetSSMI::readLocations: Failed to open file(s) in directory: ");
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
	 * Doesn't care if a file is missing. Assumes the data isn't available.
	 */
	static protected short[][] readData(Timestamp date, int rows, int cols,
			String path, boolean addYearToInputDirectory, boolean addDayToInputDirectory,
			String frequency, String polarization) {

		String filename = "";
		short[][] data = new short[rows][cols];
		
		try {
			data = null;
			
			// Find the file name
			filename = DatasetSSMI.getFileName(path, date.year(),
					date.month(), date.dayOfMonth(),
					addYearToInputDirectory, frequency, polarization);

			if (filename == null) {
				Tools.message(date.yearString() + "." + date.monthString() + "."
						+ date.dayOfMonthString() + "  No file");
				return data;
			}
		
			// Read the data

			DataFile file = new DataFile( filename );
			data = file.readShorts2D(rows, cols);
			
			file.close();
		}
		catch (Exception e) {
				// Didn't find the file, or it's bad. Return nothing.
				Tools.message(date.yearString() + "." + date.monthString() + "."
						+ date.dayOfMonthString() + "  DatasetSSMI::readData: Exception on file read: " + e);
		}
			
		Tools.message(date.yearString() + "." + date.monthString() + "."
					+ date.dayOfMonthString() + "  File name: " + filename);
			
		return data;
	}
}
