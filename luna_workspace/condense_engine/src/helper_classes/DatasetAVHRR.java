package helper_classes;

/* DatasetAVHRR
 * 
 * File and data handling for AVHRR data.
 */

import java.io.File;
import java.io.FilenameFilter;

public class DatasetAVHRR extends Dataset {

	/*
	 * Constructor
	 * 
	 * A single file name is required to read the metadata, and a path for
	 * the files containing the lat/lon location data.
	 */
	public DatasetAVHRR(String filename, String locationsPath) {
		metadata = new Metadata();
		readMetadata(filename);

		locs = new GriddedLocation[metadata.rows][metadata.cols];
		readLocations( locationsPath );
	}

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
			String time, String channel) {

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
		
		
		final String date = yearString + dayString;

		final String suffix = time + "_" + channel;
		
		// Search for this date in the directory of file names.
		File dir = new File(path);

		File[] matches = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.contains(date) && name.contains(suffix);
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
	 * readMetadata
	 * 
	 * Read the metadata from a file.
	 */
	public Metadata readMetadata(String filename) {

		// AVHRR data comes in a binary file without metadata. It is assumed
		// the user will know 'a priori' the dimensions of the data.
		// TODO: A description of the files can be found here: ???
		
		long length = 0;
		
		try {
			DataFile file = new DataFile(filename);
			length = file.length();
			file.close();
		} catch(Exception e) {
			Tools.warningMessage("DatasetAVHRR::readMetadata: could not get metadata from " +
		                       "the file: " + filename + " -- Unable to open it.");
			return null;
		}
		
		// TODO: set rows and columns for 5km or 25km data, by looking at the file size
		if (length < 999999) {
			metadata.rows = 664;
			metadata.cols = 632;			
		}

		haveMetadata = true;
		
		return metadata;		
	}

	/*
	 * metadata
	 * 
	 * Return the metadata.
	 */
	public Metadata metadata() {
		if (!haveMetadata) {
			Tools.errorMessage(
					"DatasetAVHRR",
					"metadata",
					"Attempt to retrieve metadata when it hasn't been read yet",
					new Exception());
		}

		return metadata;
	}

	/*
	 * readLocations
	 * 
	 * Read the locations for this gridded dataset.
	 */
	protected void readLocations(String locationsPath) {

		// Temporary file name hard-code until we have time for something better. :-(
		
		// 25km resolution grid cells
		String latsFileName = locationsPath + "pss25lats_v3.dat";
		String lonsFileName = locationsPath + "pss25lons_v3.dat";
		
		// 12.5km resolution grid cells
		if (metadata.rows > 332 ) {
			latsFileName = locationsPath + "pss12lats_v3.dat";
			lonsFileName = locationsPath + "pss12lons_v3.dat";
		}
		
		// Read the files
		try {
			
			// Open the files
			DataFile latitudes = new DataFile( latsFileName );
			DataFile longitudes = new DataFile( lonsFileName );

			for (int r = 0; r < metadata.rows; r++) {
				for (int c = 0; c < metadata.cols; c++) {
							
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
			Tools.warningMessage("Failed to open file");
		}				
	}

	/*
	 * readData
	 * 
	 * Read the AVHRR data from a file. FileName should include the full path.
	 * Returns null if it doesn't find the file.
	 */
	public VectorAVHRR[][] readData(String filename, GriddedLocation[][] locs, int timestampID) throws Exception {

		VectorAVHRR[][] AVHRRVectors = null;

		DataFile file = new DataFile(filename);

		try {
			file.open();
		} catch (Exception error) {
			System.out.println("could not open data file");
			Tools.warningMessage("DatasetAVHRR::readData: Could not open data file: "
					+ filename);
			return null;
		}

		// Read the data.
		try {
			// Read the data
			int[][] data = file.read2ByteInts2D(rows(), cols());

			// Create a place to store the data.
			AVHRRVectors = new VectorAVHRR[rows()][cols()];

			// Convert from NETcdf bytes to vectorData.
			for (int r = 0; r < rows(); r++) {
				for (int c = 0; c < cols(); c++) {
					AVHRRVectors[r][c] = new VectorAVHRR(data[r][c], locs[r][c], timestampID);
				}
			}
		} catch (Exception error) {
			Tools.warningMessage("DatasetAVHRR::readData: when reading data, "
					+ error);

			throw error;
		}

		try {
			file.close();
		} catch (Exception error) {
			System.out.println("error in file close");
			Tools.warningMessage("DatasetAVHRR::readData: Error on file close, "
					+ error);
			throw error;
		}

		return AVHRRVectors;
	}
}
