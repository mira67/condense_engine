package helper_classes;

import java.util.ArrayList;

/*
 * Database
 * 
 * An abstract class to encapsulate database classes. Promotes
 * standardization of the database interface, no matter what
 * kind of database we end up using.
 */
public abstract class Database extends GeoObject {
	
	/*
	 * Status
	 * 
	 * Define the possible statuses for the database.
	 */
	public enum Status {
		DISCONNECTED("DISCONNECTED"),
		CONNECTED("CONNECTED"),
		CONNECTED_READ_ONLY("CONNECTED READ-ONLY"),
		UNKNOWN("UNKNOWN");

		private final String state;

		private Status(String s) {
			state = s;
		}

		public String toString() {
			return state;
		}
	}

	protected String dbName = "";
	protected String dbPath = "";
	protected Metadata metadata;
	protected Status status = Status.DISCONNECTED;
	
	// If the database or tables do not exist, create them?
	protected Boolean createIfDoesNotExist = true;
	
	public Database(String path, String name) {
		dbPath = path;
		dbName = name;
		status = Status.DISCONNECTED;
	}
	
	public abstract boolean connect();
	public abstract boolean connectReadOnly();
	public abstract void clean();
	public abstract void disconnect();
	public void setName(String name) {
		dbName = name;
	}

	// STORAGE METHODS

	public abstract void storeMetadata(Metadata m);
	public abstract int storeTimestamp(Timestamp t);
	public abstract int storeLocation(GriddedLocation loc);
	public abstract void storeVector(GriddedVector v);

	// RETRIEVAL METHODS

	public abstract Timestamp getTimestamp(int id);
	public abstract Metadata getMetadata();
	public abstract int numberOfTimestamps();
	public abstract int numberOfVectors();
	
	public abstract ArrayList<GriddedLocation> getLocations();
	public abstract ArrayList<Timestamp> getTimestamps();
	public abstract ArrayList<GriddedVector> getVectors(int startTimestampID, int endTimestampID);
	public abstract ArrayList<GriddedVector> getVectorsAtTime(int startTimestampID);

	/*
	 *  storeLocationArray
	 *  
	 *  Store an array of locations in the database.
	 */
	public void storeLocationArray( GriddedLocation[][] locs) { 
		for (int r = 0; r < locs.length; r++) {
			for (int c = 0; c < locs[0].length; c++) {
				locs[r][c].id = storeLocation(locs[r][c]);
			}
		}
	}

	/*
	 * storeVectorArray
	 * 
	 * Store an array of gridded vectors in the database.
 	 * Assumes the locations and timestamp IDs have already been added to the database
 	 * and vector fields.
	 * 
	 */
	public void storeVectorArray(GriddedVector[][] v, GriddedLocation[][] locations) {
		
		for (int r = 0; r < v.length; r++) {
			for (int c = 0; c < v[0].length; c++) {

				// If this vector is empty, it didn't get through condensation. Continue on.
				if (v[r][c] == null) continue;
				
				// Before storing a vector, make sure it has the latest location information:
				// Location information, especially the ID, may have been updated when the
				// locations were added to the database. Don't try to update when null.
				if (locations[r][c] != null) {
					v[r][c].location = locations[r][c];					
				}
				
				// Store the vector in the database.
				storeVector(v[r][c]);					
			}
		}
	}

	/*
	 * status
	 * 
	 * Print out the status and metadata of the database.
	 */
	public abstract void status();

	/*
	 * createArrayFromSensorVectorList
	 * 
	 * Take a list of sensor vectors and create a 2D array of vectors from
	 * what's in the list, based on their row/col locations. Undefined vectors
	 * will be zeros.
	 */
	/*public static GriddedVector[][] createArrayFromSensorVectorList(
			ArrayList<GriddedVector> list, Metadata m) {

		GriddedVector[][] vectors = new GriddedVector[m.rows][m.cols];

		// Initialize the array
		for (int r = 0; r < m.rows; r++) {
			for (int c = 0; c < m.cols; c++) {
				vectors[r][c] = new GriddedVector(r, c);
			}
		}

		// Load each vector in the list onto the image array.
		Iterator<GriddedVector> iterator = list.iterator();
		while (iterator.hasNext()) {
			GriddedVector v = iterator.next();
			vectors[v.row()][v.col()] = v;
		}

		return vectors;
	}*/
}
