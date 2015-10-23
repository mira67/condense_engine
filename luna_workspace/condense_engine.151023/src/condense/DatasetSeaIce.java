package condense;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import ucar.ma2.ArrayByte;
import ucar.ma2.ArrayDouble;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/* DatasetSeaIce
 * 
 * File and data handling for sea ice datasets.
 */

public class DatasetSeaIce extends Dataset {

	Metadata metadata;

	/*
	 * Constructor
	 * 
	 * A single file name is required to read the metadata.
	 */
	public DatasetSeaIce(String filename) {
		// /locs = new ArrayList<GriddedLocation>();
		metadata = readMetadata(filename);
		readMetadata(filename);

	}

	/*
	 * getFileName
	 * 
	 * Generate the file name for a daily sea ice file.
	 */
	public static String getFileName(String path, int year, int month,
			int day, boolean addYearToInputDirectory) {

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

		// Search for this date in the directory of file names.
		File dir = new File(path);

		File[] matches = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.contains(date);
			}
		});

		// Did we find a file with that date? If yes, use the first occurrence.
		if (matches.length > 0)
			return matches[0].getPath();

		// Nothing.
		return null;
	}

	/*
	 * readMetadata
	 * 
	 * Given a path+filename of a sea ice data file, read the metadata from it.
	 */
	public Metadata readMetadata(String filename) {

		metadata = new Metadata();

		// Sea ice data locations
		ArrayDouble.D2 latArray;
		ArrayDouble.D2 lonArray;

		NetcdfFile ncfile = null;

		try {
			ncfile = NetcdfFile.open(filename);
			
			// Successful open. Now read the data.
			try {
				Variable latVar = ncfile.findVariable("latitude");
				Variable lonVar = ncfile.findVariable("longitude");
				latArray = (ArrayDouble.D2) latVar.read();
				lonArray = (ArrayDouble.D2) lonVar.read();

				int[] shape = latArray.getShape();

				// Be sure to store the rows and columns in the metadata, which
				// will later be part of the database.
				metadata.rows = shape[0];
				metadata.cols = shape[1];

				// Create a location array based on the lats/lons where
				// each vector is located.
				locs = new GriddedLocation[rows()][cols()];

				for (int r = 0; r < metadata.rows; r++) {
					for (int c = 0; c < metadata.cols; c++) {
						locs[r][c] = new GriddedLocation(r, c,
								latArray.getDouble(r), lonArray.getDouble(c));
					}
				}
			} catch (Exception e) {
				Tools.warningMessage("DatasetSeaIce::readMetadata: error during read. File: "
						+ filename);
			}
		} catch (Exception error) {
			Tools.warningMessage("DatasetSeaIce::readMetadata: Could not open data file: "
					+ filename);
		}

		
		try {
			ncfile.close();
		} catch (Exception e) {
			Tools.warningMessage("DatasetSeaIce::readMetadata: error during file close: "			
					+ filename);
		}
		return metadata;
	}

	/*
	 * metadata
	 * 
	 * Return the metadata.
	 */
	public Metadata metadata() {
		if (!haveMetadata) {
			Tools.errorMessage("DatasetSeaIce", "metadata",
				"Attempt to retrieve metadata when it hasn't been read yet", new Exception());
		}

		return metadata;
	}

	/*
	 * getLocations
	 * 
	 * Return the list of locations.
	 */
	public GriddedLocation[][] getLocations() {
		return locs;
	}

	/*
	 * readData
	 * 
	 * Read the sea ice data from a file. FileName should include the full path.
	 */
	public VectorSeaIce[][] readData(String filename, GriddedLocation[][] locs, int timestampID) throws Exception {

		VectorSeaIce[][] seaIceVectors = null;

		NetcdfFile ncfile = null;

		ArrayByte.D3 netcdfData;
		// /ArrayFloat.D2 netcdfFloats;

		try {
			ncfile = NetcdfFile.open(filename);
			// /Tools.debugMessage("Opened file: " + filename);
		} catch (Exception error) {
			Tools.warningMessage("DatasetSeaIce::readData: Could not open data file: "
					+ filename);
			ncfile.close();
			return null;
		}

		// Read the data.
		try {
			// There is an assumption here that the date/time contained in the NetCDF
			// file is the same as reflected in the filename. If not, well, the times will
			// be a total mess -- but that's not our fault.
			// Variable timeVar = ncfile.findVariable("time");
			// Timestamp time = new Timestamp(timeVar.read().getDouble(0), 1601);
			
			// Read the sea ice data
			Variable seaice = ncfile.findVariable("goddard_bt_seaice_conc");
			netcdfData = (ArrayByte.D3) seaice.read();

			// Create a place to store the data.
			seaIceVectors = new VectorSeaIce[rows()][cols()];

			// Convert from NETcdf bytes to vector data.
			for (int r = 0; r < rows(); r++) {
				for (int c = 0; c < cols(); c++) {
					// Sea ice coverage goes from 0 to %100. Convert to digital
					// number, 0-255
					// /seaIceVectors[r][c] = new SeaIceVector( (int)
					// ((float)netcdfData.get(0,r,c) * 2.55), r, c );

					seaIceVectors[r][c] = new VectorSeaIce(
							(int) netcdfData.get(0, r, c), locs[r][c], timestampID);
				}
			}
		} catch (Exception e) {
			Tools.warningMessage("DatasetSeaIce::readData: when reading data, " + e);
		}

		try {
			ncfile.close();
		} catch (Exception error) {
			Tools.warningMessage("DatasetSeaIce::readData: Error on file close, " + error);
			throw error;
		}

		return seaIceVectors;
	}

	/*
	 * locationsAsArrayList
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.nsidc.bigdata.Dataset#locationsAsArrayList()
	 * 
	 * Return the array of locations as an arraylist.
	 */
	public ArrayList<GriddedLocation> locationsAsArrayList() {
		ArrayList<GriddedLocation> list = new ArrayList<GriddedLocation>();

		for (int r = 0; r < rows(); r++) {
			for (int c = 0; c < cols(); c++) {
				list.add(locs[r][c]);
			}
		}

		return list;
	}
}