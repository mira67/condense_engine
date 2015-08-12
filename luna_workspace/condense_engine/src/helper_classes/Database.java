package helper_classes;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class Database extends GeoObject {

	public enum Status {
		DISCONNECTED("DISCONNECTED"), CONNECTED("CONNECTED"), CONNECTED_READ_ONLY("CONNECTED READ-ONLY"),
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
	protected int chFreq = 0;
	protected Metadata metadata;
	protected Status status = Status.DISCONNECTED;
	
	public abstract void connect();
	public abstract void connectReadOnly();
	public abstract void disconnect();
	public void setName(String name) {
		dbName = name;
	}

	// STORAGE METHODS

	public abstract void store(Metadata m);

	public abstract void store(Timestamp t);

	public abstract void store(ArrayList<GriddedLocation> locs);
	public abstract void store(GriddedLocation loc);

	public abstract void store(int data, int row, int col, int time); // Raw vector storage
	public abstract void store(GriddedVector v);
	public abstract void store(GriddedVector[][] v);
	public abstract void store(int data, int locID, Date date);
	public abstract void store(int id, int R, int C);

	// RETRIEVAL METHODS

	public abstract Timestamp get(int i);
	public abstract Metadata getMetadata();
	public abstract int numberOfTimestamps();
	public abstract int numberOfVectors();
	public abstract ArrayList<Timestamp> getTimestamps();
	public abstract ArrayList<GriddedVector> getVectors(int startIndex,
			int endIndex);

	// /public abstract ArrayList<GriddedLocation> getLocations();

	// OTHER NONSENSE

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
	public static GriddedVector[][] createArrayFromSensorVectorList(
			ArrayList<GriddedVector> list, Metadata m) {

		GriddedVector[][] vectors = new GriddedVector[m.rows()][m.cols()];

		// Initialize the array
		for (int r = 0; r < m.rows(); r++) {
			for (int c = 0; c < m.cols(); c++) {
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
	}
	
	/*
	 * CheckTable
	 * 
	 * Returns true if the table exists, false if it does not (and creates the table).
	 */
	public Boolean checkTable(String tbNames) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void createMap(String tbN) {
		// TODO Auto-generated method stub
		
	}
}
