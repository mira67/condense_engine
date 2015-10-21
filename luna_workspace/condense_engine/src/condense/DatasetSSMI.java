package condense;

/* DatasetSSMI
 * 
 * File and data handling for SSMI data.
 */

import java.io.File;
import java.io.FilenameFilter;

public class DatasetSSMI extends Dataset {

	/*
	 * Constructor
	 * 
	 * A single file name is required to read the metadata, and a path for
	 * the files containing the lat/lon location data.
	 */
	public DatasetSSMI(String filename, String locationsPath) {
		metadata = new Metadata();
		readMetadata(filename);

		locs = new GriddedLocation[metadata.rows][metadata.cols];
		readLocations( locationsPath );
	}

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
	 * readMetadata
	 * 
	 * Read the metadata from a file.
	 */
	public Metadata readMetadata(String filename) {

		// SSMI data comes in a binary file without metadata. It is assumed
		// the user will know 'a priori' the dimensions of the data.
		// A description of the files can be found here:
		// http://nsidc.org/data/docs/daac/nsidc0001_ssmi_tbs.gd.html
		//
		// Different hemispheres and frequencies will have different dimensions
		// and thus different file sizes.
		// Below, we use the file size to determine the dimensions of the
		// data it contains. (Alternatively, the file name could be parsed
		// for the hemisphere and frequency info but this is just easier.)
		
		long length = 0;
		
		try {
			DataFile file = new DataFile(filename);
			length = file.length();
			file.close();
		} catch(Exception e) {
			Tools.warningMessage("DatasetSSMI::readMetadata: could not get metadata from " +
		                       "the file: " + filename + " -- Unable to open it.");
			return null;
		}
		
		// Southern hemisphere,	85.5 and 91.7 GHz, 839296 Bytes
		if (length == 839296) {
			metadata.rows = 664;
			metadata.cols = 632;
			return metadata;
		}
		
		// Southern hemisphere,	everything else, 209824 Bytes
		if (length == 209824) {
			metadata.rows = 332;
			metadata.cols = 316;
			return metadata;
		}
		
		// Northern hemisphere,	85.5 and 91.7 GHz, 1089536 Bytes
		if (length == 1089536) {
			metadata.rows = 896;
			metadata.cols = 608;
			return metadata;
		}

		// Northern hemisphere,	everything else, 272384 Bytes
		if (length == 272384) {
			metadata.rows = 448;
			metadata.cols = 304;
			return metadata;
		}

		Tools.warningMessage("DatasetSSMI::readMetadata: Unable to determine data dimensions: " + filename);
		
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
					"DatasetSSMI",
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
	 * Read the SSMI data from a file. FileName should include the full path.
	 * Returns null if it doesn't find the file.
	 */
	public VectorSSMI[][] readData(String filename, GriddedLocation[][] locs, int timestampID) throws Exception {

		VectorSSMI[][] ssmiVectors = null;

		DataFile file = new DataFile(filename);

		try {
			file.open();
		} catch (Exception error) {
			System.out.println("could not open data file");
			Tools.warningMessage("DatasetSSMI::readData: Could not open data file: "
					+ filename);
			return null;
		}

		// Read the data.
		try {
			// Read the data
			int[][] data = file.read2ByteInts2D(rows(), cols());

			// Filter out missing data points. BTs are encoded by a factor of 10.
			// data = Tools.discardBadData(data, 1000, 4000);

			// TODO for testing
			//data = Tools.scaleIntArray2D(data, 0, 350);

			// Create a place to store the data.
			ssmiVectors = new VectorSSMI[rows()][cols()];

			// Convert from NETcdf bytes to vectorData.
			for (int r = 0; r < rows(); r++) {
				for (int c = 0; c < cols(); c++) {
					ssmiVectors[r][c] = new VectorSSMI(data[r][c], locs[r][c], timestampID);
				}
			}
		} catch (Exception error) {
			Tools.warningMessage("DatasetSSMI::readData: when reading data, "
					+ error);

			throw error;
		}

		try {
			file.close();
		} catch (Exception error) {
			System.out.println("error in file close");
			Tools.warningMessage("DatasetSSMI::readData: Error on file close, "
					+ error);
			throw error;
		}

		return ssmiVectors;
	}
}
