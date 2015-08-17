package helper_classes;

///todo
///import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class Database extends GeoObject {
	
	/*
	 * Status
	 * 
	 * Define the possible statuses for the database.
	 */
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

	/*
	 *  Table
	 *  
	 *  Define what tables will be in the SQL database. The 'name' is the name of the table,
	 *  the 'columns' define what the data entries will be in the table (use SQL format).
	 */
	public enum Table {
		METADATA("METADATA","(ID INT PRIMARY KEY, ROWS SMALLINT, COLS SMALLINT, TIMESTAMPS SMALLINT, LOCATIONS SMALLINT, VECTORS INT)"),
		LOCATIONS("LOCATIONS","(ID INT PRIMARY KEY, ROW SMALLINT, COL SMALLINT, LAT DOUBLE, LON DOUBLE)"),
		TIMESTAMPS("TIMESTAMPS","BOGUS"),
		VECTORS("VECTORS","BOGUS");
			
		protected final String name;
		protected final String SQLcolumns;

		private Table(String s, String c) {
			name = s;
			SQLcolumns = c;
		}

		public String toString() {
			return name;
		}
		

		public String columnNames() {
			return SQLcolumns;
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

	public abstract void storeTimestamp(Timestamp t);

	public abstract int storeLocation(GriddedLocation loc);
	public abstract void storeLocationArray(GriddedLocation[][] locs);
	///public abstract void storeLocationList(ArrayList<GriddedLocation> locs);

	//this is crap
	///public abstract void storeVector(int id, int row, int col, int data, int time); // Raw vector storage
	
	public abstract void storeVector(GriddedVector v);
	public abstract void storeVectorArray(GriddedVector[][] v);
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
	}
	
	/*
	 * CheckTable
	 * 
	 * Returns true if the table exists, false if it does not (and creates the table).
	 */
	public Boolean checkTables(String tbNames) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void createMap(String tbN) {
		// TODO Auto-generated method stub
		
	}
}
