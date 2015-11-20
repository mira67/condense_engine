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

		// Size of the image files.
		int rows = rows( hemisphere, frequency );
		int cols = cols( hemisphere, frequency );
		
		Tools.message("SSMI Locations:");
		Tools.message("    Hemisphere: " + hemisphere + ", Frequency: " + frequency + ", rows = " + rows + ", cols = " + cols);
		
		GriddedLocation[][] locs = new GriddedLocation[rows][cols];
		locs = GriddedLocation.initialize(locs);

		// 25km resolution grid cells, southern hemisphere.
		String latsFile = path + "pss25lats_v3.dat";
		String lonsFile = path + "pss25lons_v3.dat";

		// Read data files
		// Temporary file name hard-code until we have time for something better. :-(
		if (hemisphere.equalsIgnoreCase("south") && rows > 332 ) {
			// 12.5km resolution grid cells
			latsFile = path + "pss12lats_v3.dat";
			lonsFile = path + "pss12lons_v3.dat";
		}
		
		// Northern hemisphere
		else {
			// 25km resolution grid cells
			latsFile = path + "psn25lats_v3.dat";
			lonsFile = path + "psn25lons_v3.dat";
						
			// 12.5km resolution grid cells
			if (rows > 448 ) {
				latsFile = path + "psn12lats_v3.dat";
				lonsFile = path + "psn12lons_v3.dat";
			}
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
		
					// Swap the byte order (for Windows).
					lat = Tools.swap(lat);
					lon = Tools.swap(lon);

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
	 * rows
	 * 
	 * Return the number of rows for the SSMI data.
	 */
	public static int rows(String hemisphere, String frequency) {
		if (hemisphere.equalsIgnoreCase("south")) {
			// 85 and 91 GHz
			if (frequency.equalsIgnoreCase("85") ||
				frequency.equalsIgnoreCase("91")) return 664;
			
	 		// Southern hemisphere,	everything else (19, 22, 37 GHz)
			return 332;
		}
		// Northern hemisphere
		else {
			// 85 and 91 GHz
			if (frequency.equalsIgnoreCase("85") ||
					frequency.equalsIgnoreCase("91")) return 896;
		}
		
		// Northern hemisphere, all other frequencies
		return 448;
	}

	/*
	 * cols
	 * 
	 * Return the number of columns for the SSMI data.
	 */
	public static int cols(String hemisphere, String frequency) {
		if (hemisphere.equalsIgnoreCase("south")) {
			// 85 and 91 GHz
			if (frequency.equalsIgnoreCase("85") ||
				frequency.equalsIgnoreCase("91")) return 632;
	 		// Southern hemisphere,	everything else (19, 22, 37 GHz)
			else return 316;				

		}
		// Northern hemisphere
		else {
			// 85 and 91 GHz
			if (frequency.equalsIgnoreCase("85") ||
					frequency.equalsIgnoreCase("91")) return 608;
		}
		
		// Northern hemisphere, all other frequencies
		return 304;
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
			data = file.read2ByteInts2D(rows, cols);
			
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
