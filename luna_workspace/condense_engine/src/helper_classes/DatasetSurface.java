package helper_classes;

import java.util.ArrayList;

/* DatasetSurface
 * 
 * File and data handling for surface datasets.
 */

public class DatasetSurface extends Dataset {

	protected VectorSurface[][] vectors;
	protected int rows = 0;
	protected int cols = 0;

	/*
	 * Constructor
	 * 
	 * A single file name is required to read the metadata.
	 */
	DatasetSurface() {
		metadata = new Metadata();
		readMetadata(null);

		locs = new GriddedLocation[metadata.rows][metadata.cols];
	}

	/*
	 * readMetadata
	 * 
	 * Read a surface mask file.
	 */
	public Metadata readMetadata(String filename) {

		metadata = new Metadata();

		// TODO: hardcoded dimensions, for now.
		// Size of the 12.5 km resolution files
		rows = 1441;
		cols = 1441;

		// Set up the metadata for the surface database
		metadata.rows = rows;
		metadata.cols = cols;

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
			metadata = readMetadata(null);
		}

		return metadata;
	}

	/*
	 * readData
	 * 
	 * Read a surface mask file, add it to the database. - surfaceFile contains
	 * the surface data - surfaceLats is a file containing the latitudes -
	 * surfaceLons is a file containing the longitudes
	 */
	public VectorSurface[][] readData(String surfaceFile, String surfaceLats,
			String surfaceLons) {

		Tools.debugMessage("Reading surface mask files");

		// Make sure we have the metadata.
		metadata = readMetadata(surfaceFile);

		vectors = new VectorSurface[rows][cols];

		byte[][] surfaceBytes = new byte[rows][cols];

		// Read all the surface data, add it to the database.
		try {
			// Read the locations.
			// Start with the latitudes
			DataFile file = new DataFile(surfaceLats);
			int[][] lats = file.readInt2d(rows, cols);
			file.close();

			// Read the longitudes
			file = new DataFile(surfaceLons);
			int[][] lons = file.readInt2d(rows, cols);
			file.close();

			// Locations of the vectors
			locs = new GriddedLocation[rows][cols];

			// Create the array of locations.
			// Divide by 100000 to decode integer lat/lon into decimal degrees
			// (doubles).
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					locs[r][c] = new GriddedLocation(r, c,
							((double) lats[r][c]) / 100000.,
							((double) lons[r][c]) / 100000.);
				}
			}

			file = new DataFile(surfaceFile);
			surfaceBytes = file.readBytes2D(rows, cols);

			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					int value = Tools.unsignedByteToInt(surfaceBytes[r][c]);
					
					// Create the surface vector. 0 is used for the timestampID
					// because all surface vectors will have the same time.
					vectors[r][c] = new VectorSurface(value, locs[r][c], 0);
				}
			}
			file.close();
		} catch (Exception e) {
			Tools.errorMessage("DatasetSurface", "readSurfaceData",
					"error on file read", e);
			return null;
		}

		return vectors;
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
	 * getTime
	 * 
	 * Return a timestamp of the surface data.
	 */
	public Timestamp getTime() {
		return new Timestamp(2001, 1, 1);
	}

	public static Timestamp getTimeStatic() {
		return new Timestamp(2001, 1, 1);
	}

	/*
	 * locationsAsArrayList
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.nsidc.bigdata.Dataset#locationsAsArrayList()
	 * 
	 * Return all the locations as an arraylist.
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
