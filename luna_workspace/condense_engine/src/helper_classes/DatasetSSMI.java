package helper_classes;

/* DatasetSSMI
 * 
 * File and data handling for SSMI data.
 */

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class DatasetSSMI extends Dataset {

	// /protected ArrayList<GriddedLocation> locs;
	protected GriddedLocation locs[][];
	protected Metadata metadata;

	/*
	 * Constructor
	 * 
	 * A single file name is required to read the metadata.
	 */
	DatasetSSMI(String filename) {
		metadata = new Metadata();
		readMetadata(filename);

		locs = new GriddedLocation[metadata.rows()][metadata.cols()];
		readLocations();
	}

	/*
	 * getSSMIFileName
	 * 
	 * Generate the file name for a daily SSMI file.
	 */
	public static String getSSMIFileName(String path, int year, int month,
			int day, boolean addYearToInputDirectory, int frequency,
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

		final String channel = String.valueOf(frequency) + polarization;

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

		// Nothing. Return a default name, which will fail to open.
		// String filename = path + "file_not_found_" + date + ".bin";

		return "";
	}

	/*
	 * readMetadata
	 * 
	 * Read the metadata from a file.
	 */
	public Metadata readMetadata(String filename) {

		// Temporary hard-code
		metadata.rows(332);
		metadata.cols(316);

		return metadata;
	}

	/*
	 * metadata
	 * 
	 * Return the metadata.
	 */
	public Metadata metadata() {
		if (!haveMetadata) {
			Tools.errorMessage("DatasetSSMI", "metadata",
				"Attempt to retrieve metadata when it hasn't been read yet", new Exception());
		}

		return metadata;
	}

	/*
	 * readLocations
	 * 
	 * Read the locations for this gridded dataset.
	 */
	protected void readLocations() {
		for (int r = 0; r < metadata.rows(); r++) {
			for (int c = 0; c < metadata.cols(); c++) {
				locs[r][c] = new GriddedLocation(r, c);
			}
		}
	}

	/*
	 * readData
	 * 
	 * Read the SSMI data from a file. FileName should include the full path.
	 */
	public static SSMIVector[][] readData(String filename, int year, int month,
			int day, Timestamp time, int rows, int cols) throws Exception {

		SSMIVector[][] ssmiVectors = null;

		DataFile file = new DataFile(filename);

		try {
			file.open();
		} catch (Exception error) {
			Tools.warningMessage("DatasetSSMI::readData: Could not open data file: "
					+ filename);
			return null;
		}

		// Read the data.
		try {
			// Read the data
			int[][] data = file.read2ByteInts2D(rows, cols);

			// Filter out missing data points. BTs are encoded by a factor of
			// 10.
			data = Tools.discardBadData(data, 1000, 4000);

			// TODO for testing
			data = Tools.scaleIntArray2D(data, 0, 255);

			// Create a place to store the data.
			ssmiVectors = new SSMIVector[rows][cols];

			// Convert from NETcdf bytes to vectorData.
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					ssmiVectors[r][c] = new SSMIVector(data[r][c], r, c);
					ssmiVectors[r][c].time(time.days());

				}
			}
		} catch (Exception error) {
			Tools.warningMessage("DatasetSSMI::readData: when reading data, " + error);
			
			throw error;
		}

		try {
			file.close();
		} catch (Exception error) {
			Tools.warningMessage("DatasetSSMI::readData: Error on file close, " + error);
			throw error;
		}

		return ssmiVectors;
	}

	public int rows() {
		return metadata.rows();
	}

	public int cols() {
		return metadata.cols();
	}

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