package condense;

/* DatasetAVHRR
 * 
 * File and data handling for AVHRR data.
 */

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
		//readLocations( locationsPath );
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
	 * readMetadata
	 * 
	 * Read the metadata from a file.
	 */
	public Metadata readMetadata(String filename) {

		if (haveMetadata) return metadata;
		
		// AVHRR data comes in a binary file without metadata. It is assumed
		// the user will know 'a priori' the dimensions of the data.
		// A description of the files can be found here:
		// http://nsidc.org/data/docs/daac/nsidc0066_avhrr_5km.gd.html#size
		
		// Northern hemisphere
		if (filename.indexOf("_n") > 0) {
			metadata.rows = 1805;
			metadata.cols = 1805;			
		}
		else {
			// Southern hemisphere
			metadata.rows = 1605;
			metadata.cols = 1605;			
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
	 * Read the locations for this gridded dataset. LatsPath and lonsPath
	 * are the latitude and longitude files.
	 */
	protected void readLocations(String latsPath, String lonsPath) {
		
		// If no file information was provided, bail out.
		if (latsPath == null) return;
		if (latsPath.length() == 0) return;
		
		// Read the files
		try {
			
			// Open the files
			DataFile latitudes = new DataFile( latsPath );
			DataFile longitudes = new DataFile( lonsPath );

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

		if (filename == null) return AVHRRVectors;
		
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
			short[][] data = file.readShorts2D(rows(), cols());

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
