package helper_classes;

import java.util.ArrayList;
import java.sql.*;

/* DatabaseH2
 * 
 * Encapsulates the H2 database, for standardized interfacing. 
 *
 * Example connection paths:
 * 			"jdbc:h2:tcp://localhost/~/"
 * 			"jdbc:h2:tcp://localhost/~/Desktop/myProject/Databases/"
 * 			"jdbc:h2:tcp://10.240.210.131:9292/mem:~/"
 */

public class DatabaseH2 extends Database {

	/*
	 *  Table
	 *  
	 *  Define what tables will be in the SQL database. The 'name' is the name of the table,
	 *  the 'columns' define what the data entries will be in the table (use SQL format).
	 */
	public enum Table {
		
		METADATA("METADATA","(ID INT PRIMARY KEY, ROWS SMALLINT, COLS SMALLINT, TIMESTAMPS INT, LOCATIONS INT, VECTORS BIGINT)"),
		LOCATIONS("LOCATIONS","(ID INT PRIMARY KEY, ROW SMALLINT, COL SMALLINT, LAT DOUBLE, LON DOUBLE)"),
		TIMESTAMPS("TIMESTAMPS","(ID INT PRIMARY KEY, TIMESTAMP FLOAT)"),
		VECTORS("VECTORS","(ID BIGINT PRIMARY KEY, LOCATIONID INT, TIMESTAMPID INT)");
			
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

	
	Connection conn;

	///private PreparedStatement sqlStmt_tb;
	///private PreparedStatement sqlStmt_map;
	private Statement sqlCreate;
	///private ResultSet results;
	private boolean metadataStored = false;

	// Constructor
	public DatabaseH2(String path, String name) {
		super(path, name);
	}

	/*
	 * connect
	 * 
	 * Connect to the database for writing. If it or the tables do not exist
	 * they will be created.
	 */
	public boolean connect() {

		createIfDoesNotExist = true;

		// Open the database.
		try {
			Class.forName("org.h2.Driver");
			conn = DriverManager.getConnection(dbPath + dbName, null, null);

			conn.setAutoCommit(true);

		} catch (Exception e) {
			Tools.errorMessage("DatabaseH2", "connect",
					"Could not connect with database " + dbName + ", Path: "
							+ dbPath, e);
			return false;
		}

		status = Status.CONNECTED;

		return true;
	}

	/*
	 * clean
	 * 
	 * Reset the database, clean out all existing data and tables. Empty tables
	 * are created afterward.
	 */
	public void clean() {
		
		// Drop each table.
		for (Table table : Table.values()) {
			try {
				sqlCreate = conn.createStatement();
				Boolean status = sqlCreate.execute("DROP TABLE IF EXISTS "
						+ table.name);
				Tools.debugMessage("DatabaseH2 table clean: " + table.name
						+ " status: " + status);
			} catch (Exception e) {
				// Do nothing right now.
				// Assume it either didn't exist, or was dropped successfully.
				// Might want to re-visit this assumption at a later time.
			}
		}
	}

	/*
	 * connectReadOnly
	 * 
	 * Connect to an existing database for reading. Return true on success.
	 */
	public boolean connectReadOnly() {
		
		// Read-only access. Don't modify the database.
		createIfDoesNotExist = false;
		
		try {
			Class.forName("org.h2.Driver");
			conn = DriverManager.getConnection(dbPath + dbName
					+ ";IFEXISTS=TRUE", null, null);//

		} catch (Exception e) {
			Tools.errorMessage("DatabaseH2", "connectReadOnly",
					"Could not connect with database " + dbName, e);

			return false;
		}

		Tools.statusMessage("Connected to database, read-only: " + dbName);
		status = Status.CONNECTED_READ_ONLY;

		return true;
	}

	/*
	 * disconnect
	 * 
	 * Close the connection to the database.
	 */
	public void disconnect() {
		
		// If we may have been writing to the file, update the metadata first.
		if (status == Status.CONNECTED) {
			storeMetadata(metadata);
		}
		
		try {
			conn.close();
		} catch (Exception e) {
			Tools.warningMessage("Unable to close database: " + dbName);
			status = Status.UNKNOWN;
			return;
		}

		status = Status.DISCONNECTED;

		Tools.statusMessage("Disconnected from database");
	}

    //
    // STORAGE METHODS
    //

    /*
     * storeMetadata
     * 
     * Store metadata for the dataset and database, or update existing data. Returns the
     * number of 'metadata' entries in the database. Should always be just 1!
     */
	public void storeMetadata( Metadata m ) {
		
		// If we've already stored it, update the metadata instead of making 
		// a new table entry.
		if (metadataStored) {
			updateMetadata(m);
			return;
		}
		

		// New database entry. First, make a copy of the metadata.
		metadata = m;
		
		// Now store it in the database.
		try {
			if (createIfDoesNotExist) sqlCreate.execute("CREATE TABLE IF NOT EXISTS " + Table.METADATA.name()
					+ Table.METADATA.columnNames());

			sqlCreate.execute("INSERT INTO " + Table.METADATA.name() + " VALUES(" +
					"0," +
					metadata.rows + "," +
					metadata.cols + "," +
					metadata.timestamps + "," +
					metadata.locations + "," +
					metadata.vectors + ")");

		} catch (Exception e) {
			Tools.errorMessage("DatabaseH2", "storeMetadata", "When storing metadata", e);
		}
		
		metadataStored = true;
		
		return;
	}

    /*
     * updateMetadata
     * 
     * Update the metadata for the dataset and database. Assumes the metadata table
     * and entry already exist in the database.
     */
	public void updateMetadata( Metadata m ) {

		metadata = m;
		
		try {
			sqlCreate.execute("UPDATE " + Table.METADATA.name() + " SET " +
					"ROWS = " +	metadata.rows + "," +
					"COLS = " +	metadata.cols + "," +
					"TIMESTAMPS = " + metadata.timestamps + "," +
					"LOCATIONS = " + metadata.locations + "," +
					"VECTORS = " + metadata.vectors + " WHERE ID = 0");

		} catch (Exception e) {
			Tools.errorMessage("DatabaseH2", "updateMetadata", "When updating metadata", e);
		}
		
		return;
	}
	
	/*
	 *  storeLocation
	 *  
	 *  Store a single gridded location. The returned id is a unique table identifier
	 *  for this location.
	 */
	public int storeLocation(GriddedLocation loc) {
		try {
			if (createIfDoesNotExist) sqlCreate.execute("CREATE TABLE IF NOT EXISTS " + Table.LOCATIONS.name()
					+ Table.LOCATIONS.columnNames());

			sqlCreate.execute("INSERT INTO " + Table.LOCATIONS.name() + " VALUES(" +
					+ metadata.locations + "," +
					+ loc.row() + "," +
					+ loc.col() + "," +
					+ loc.lat() + "," +
					+ loc.lon() + ")");

		} catch (Exception e) {
			Tools.errorMessage("DatabaseH2",
					"storeLocation",
					"When storing location", e);
		}
		
        metadata.locations++;
		
		return metadata.locations;
	}
	
	/*
	 *  storeTimestamp
	 *  
	 *  Store a single timestamp. The returned id is a unique table identifier
	 *  for this time.
	 */
	public int storeTimestamp(Timestamp t) {
		try {
			if (createIfDoesNotExist) sqlCreate.execute("CREATE TABLE IF NOT EXISTS " + Table.TIMESTAMPS.name()
					+ Table.TIMESTAMPS.columnNames());

			sqlCreate.execute("INSERT INTO " + Table.TIMESTAMPS.name() + " VALUES(" +
					+ metadata.timestamps + "," +
					+ t.days() + ")");

		} catch (Exception e) {
			Tools.errorMessage("DatabaseH2",
					"storeTimestamp",
					"When storing timestamp " + t.toString(), e);
		}
		
        metadata.timestamps++;
		
		return metadata.timestamps;
	}
	
	public void storeVector(GriddedVector v) {}

	/*
	 * Store a vector by values
	 * 
	 */
	public void storeVector(int a, int b, int c, int d) {}
	/*
		//TODO vectors.add( new GriddedVector( data, row, col, time));
		//temporal storage method for database/spark testing
		try {
	        sqlStmt_tb.setDate(1, date);
	        sqlStmt_tb.setInt(2, locID);
	        sqlStmt_tb.setInt(3, data);
	        int rowsAffected = sqlStmt_tb.executeUpdate();
	        Tools.debugMessage("Affected rows = " + rowsAffected);
	        conn.commit();
	        
		} catch(Exception e) {
			Tools.errorMessage("DatabaseH2", "store", "Could not store with database " + dbName, e);
		}
		
	}*/
	


	/*
	 * store a vector array
	 * 
	 */
	//TODO: id?
	public void storeVectorArray(GriddedVector[][] v) {
		for (int r = 0; r < v.length; r++) {
			for (int c = 0; c < v[0].length; c++) {
				storeVector(v[r][c]);
			}
		}
	}

	/*
	 *  Add an array of locations
	 */
	public void storeLocationArray( GriddedLocation[][] locs) { 
		for (int r = 0; r < locs.length; r++) {
			for (int c = 0; c < locs[0].length; c++) {
				locs[r][c].id = storeLocation(locs[r][c]);
			}
		}
	}

	/*
	 *  Add an array of sensor vectors
	 */
	//TODO: id?
	public void add( GriddedVector[][] v) { 
		for (int r = 0; r < v.length; r++) {
			for (int c = 0; c < v[0].length; c++) {
				storeVector(v[r][c]);
			}
		}
	}

	//
	// RETRIEVAL METHODS
	//
	
	public Metadata getMetadata() { 
		updateMetadata();
		return metadata;
	}
	
	protected void updateMetadata() {
		// TODO metadata.vectors = vectors.size();
		//metadata.timestamps = timestamps.size();
		//metadata.locations = locations.size();				
	}
	
	public Timestamp get(int i) {
		// TODO
		return new Timestamp();
	}
	
	public int numberOfTimestamps() {
		// TODO
		return 0;
	}
	
	public ArrayList<Timestamp> getTimestamps() {
		return new ArrayList<Timestamp>();
	}

	public ArrayList<GriddedLocation> getLocations() { 
		// TODO
		return new ArrayList<GriddedLocation>();
	 }
	
	
	public int numberOfVectors() {
		// TODO
		return 0;
	}
	
	public ArrayList<GriddedVector> getVectors() {
		// TODO
		return new ArrayList<GriddedVector>();
	}
	
	
	public int rows() { return metadata.rows; }
	public int cols() { return metadata.cols; }
	
	/* getVectorsAtTime
	 * 
	 * Return all the vectors in the database at the specified time index.
	 */
	public ArrayList<GriddedVector> getVectorsAtTime( int time ) {
		// TODO
		ArrayList<GriddedVector> subset = new ArrayList<GriddedVector>();
		return subset;
	}	

	
	/* getVectorsAtTimeIndex
	 * 
	 * Return all the vectors in the database at the specified time index.
	 */
	public ArrayList<GriddedVector> getVectorsAtTimeIndex( int index ) {

		// TODO
		ArrayList<GriddedVector> subset = new ArrayList<GriddedVector>();
		
		return subset;
	}	

	
	/* getVectorsInTimeRange
	 * 
	 * Return all the vectors in the database in the range of indices.
	 */
	public ArrayList<GriddedVector> getVectors( int first, int last ) {

		ArrayList<GriddedVector> subset = new ArrayList<GriddedVector>();
		
		// TODO
		
		if (first > last || first < 0) {
			Tools.warningMessage("DatabaseH2::getVectorsInTimeRange: Warning: " +
					"Requested time range " + first + " to " + last +
					" may exceed the range of the timestamps in the datbase " +
					"or be malformed. Be sure to index from zero.");
			
			return subset;
		}
		
		return subset;
	}	

	public void status() {
		updateMetadata();
		
		Tools.statusMessage("  ========================================");
		Tools.statusMessage("  Database name = " + dbName + "  Status: "
				+ status.toString());
		Tools.statusMessage("  Database path = " + dbPath);
		Tools.statusMessage("  Timestamp entries = " + metadata.timestamps);
		Tools.statusMessage("  Location entries  = " + metadata.locations);
		Tools.statusMessage("  Vector entries    = " + metadata.vectors);
		Tools.statusMessage("  ========================================");
	}
}

/*
 * Store a location
 * 
 */
// TODO: need this?
/*public void store(int id, int row, int col) {
	//TODO vectors.add( new GriddedVector( data, row, col, time));
	//temporal storage method for database/spark testing
	try {
        sqlStmt_map.setInt(1, id);
        sqlStmt_map.setInt(2, row);
        sqlStmt_map.setInt(3, col);
        int rowsAffected = sqlStmt_map.executeUpdate();
        Tools.debugMessage("Affected rows = " + rowsAffected);
        conn.commit();
        
	} catch(Exception e) {
		Tools.errorMessage("DatabaseH2", "store", "Could not store with database map table" + dbName, e);
	}
	
} */

/*
 * checkTables
 * 
 * Check to see if the tables already exist. If not, create a new one (if
 * the flag is set to true).
 *
public void checkTables(Boolean createIfDoesNotExist) throws Exception {

	// Iterate through the tables that should be in the database.
	for (Table table : Table.values()) {

		// Ask the database for the table.
		results = conn.getMetaData()
				.getTables(null, null, table.name, null);

		boolean exists = false;

		// Does the table exist?
		try {
			// Look through the returned information for the table name.
			while (results.next()) {
				if (results.getString("TABLE_NAME") == table.name) {

					// Yes, found it.
					exists = true;
					Tools.debugMessage("checkTables: found table "
							+ table.name);
					break;
				}
			}

			results.close();

		} catch (SQLException e) {
			Tools.errorMessage("DatabaseH2", "checkTables",
					"When looking for a table: " + table.name, e);
			throw (e);
		}

		// If the table doesn't exist, create it.
		/*if (!exists && createIfDoesNotExist) {
			if (!createTable(table)) {
				Tools.errorMessage("DatabaseH2", "checkTables",
						"Failed to create table: " + table.name,
						new Exception("Giving up."));
			}
			exists = true;
		}

		// One way or the other, the table should be in there now. If not,
		// we've got problems.
		if (!exists) {
			Tools.errorMessage("DatabaseH2", "checkTables",
					"Could not find and/or create " + "table: "
							+ table.name, new Exception("Giving up."));
		}
	}
}*/



/*
 * createTable
 * 
 * Create a new table in the database if it doesn't already exist. Returns
 * true on success.
 */
/*protected boolean createTable(Table table) {
	try {
		Tools.debugMessage("Creating table: " + table.name);
		sqlCreate = conn.createStatement();
		sqlCreate.execute("CREATE TABLE IF NOT EXISTS " + table.name
				+ table.columnNames());
	///			+ "(id INT primary key, row SMALLINT, col SMALLINT)");
	///	sqlStmt_map = conn.prepareStatement("INSERT INTO " + table.name
	///			+ "(id,row, col) " + "values " + "(?,?,?)");
	} catch (SQLException e) {
		Tools.warningMessage("Could not create table: " + table.name);
		Tools.warningMessage("Exception: " + e);
		return false;
	}

	return true;
}*/

/*
 * /If table does not exist, create a new table and pre-defined SQL for table entries recording
 */
/*
// check map table
String tbN = "LOCMAP_S";
if (!checkTable(tbN)) {
	//write to location
	createMap();
}
*/

/* 	
	//test sql statement - row record
sqlStmt_tb = conn.prepareStatement(
		"INSERT INTO " + tbName +
		"(date, locID, bt) " + 
		"values " + 
		"(?,?,?)");
*/