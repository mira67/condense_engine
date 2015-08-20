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
	 *  the 'SQLcolumns' define what the data entries will be in the table (use SQL format).
	 */
	public enum Table {
		
		METADATA("METADATA","(ID INT PRIMARY KEY, ROWS SMALLINT, COLS SMALLINT, TIMESTAMPS INT, LOCATIONS INT, VECTORS BIGINT)"),
		LOCATIONS("LOCATIONS","(ID INT PRIMARY KEY, ROW SMALLINT, COL SMALLINT, LAT DOUBLE, LON DOUBLE)"),
		TIMESTAMPS("TIMESTAMPS","(ID INT PRIMARY KEY, TIMESTAMP FLOAT)"),
		VECTORS("VECTORS","(ID BIGINT PRIMARY KEY, VALUE INT, LOCATIONID INT, TIMESTAMPID INT)");
			
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

	private Statement sqlCreate;
	private boolean metadataStored = false;

	// If we read all the locations or timestamps from the database, keep
	// a record of them (so we don't have to do it again).
	ArrayList<GriddedLocation> locations = null;
	ArrayList<Timestamp> timestamps = null;
	
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
					"1," +		// Primary key index is always 1.
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
     * and row entry already exist in the database.
     */
	public void updateMetadata( Metadata m ) {

		// Make sure the database is open for writing.		
		if (status == Status.CONNECTED) {
			
			// Update the locally-stored metadata.
			metadata = m;
			
			// Update the database.
			try {
				sqlCreate.execute("UPDATE " + Table.METADATA.name() + " SET " +
						"ROWS = " +	metadata.rows + "," +
						"COLS = " +	metadata.cols + "," +
						"TIMESTAMPS = " + metadata.timestamps + "," +
						"LOCATIONS = " + metadata.locations + "," +
						"VECTORS = " + metadata.vectors + " WHERE ID = 1");

			} catch (Exception e) {
				Tools.errorMessage("DatabaseH2", "updateMetadata", "When updating metadata", e);
			}
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
			// Increment the number of locations stored.
	        metadata.locations++;
	        
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
		
		return metadata.locations;
	}
	
	/*
	 *  storeTimestamp
	 *  
	 *  Store a single timestamp. The returned id is a unique table identifier
	 *  for this time.
	 */
	public int storeTimestamp(Timestamp t) {
        
		// Increment the number of timestamps stored. Use it as the ID.
		metadata.timestamps++;

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
		
		return metadata.timestamps;
	}
	
	/*
	 * storeVector
	 * 
	 * Stores a single gridded vector of sensor data in the database.
	 */
	public void storeVector(GriddedVector v) {

		// Increment the number of vectors stored, and use that as the ID.
		metadata.vectors++;
		
		try {
			if (createIfDoesNotExist) sqlCreate.execute("CREATE TABLE IF NOT EXISTS " + Table.VECTORS.name()
					+ Table.VECTORS.columnNames());

			sqlCreate.execute("INSERT INTO " + Table.VECTORS.name() + " VALUES(" +
					metadata.vectors + "," +
					v.data() + "," +
					v.location.id + "," +
					v.timestampID + ")");

		} catch (Exception e) {
			Tools.errorMessage("DatabaseH2",
					"storeVector",
					"When storing vector " + v.toString(), e);
		}
	
		return;
	}


	//
	// RETRIEVAL METHODS
	//
	
	public Metadata getMetadata() {
		
		// Make sure what's in the database is up-to-date too.
		updateMetadata(metadata);
		
		return metadata;
	}
	
	/* getTimestamp
	 * 
	 * Query the database for the specified id (id is the timestamps table index).
	 */
	 public Timestamp getTimestamp(int id) {
	 	Timestamp t = null;
		
		String query = "SELECT * FROM " + Table.TIMESTAMPS.name() +	" WHERE ID = " + id;
	    
		try {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(query);
		    
			if (rs.next()) {
				t = new Timestamp(
					rs.getInt("ID"),
					rs.getFloat("TIMESTAMP"));
			}
			rs.close();
			statement.close();
		} catch(SQLException e) {
			Tools.errorMessage("DatabaseH2", "getTimestamp", "query failed", e);
		}
		
		return t;
	}
	
	/* getLocation
	 * 
	 * Query the database for the specified id (id is the Locations table index).
	 */
	public GriddedLocation getLocation(int id) {
		GriddedLocation loc = null;
		
		String query = "SELECT * FROM " + Table.LOCATIONS.name() +	" WHERE ID = " + id;
	    
		try {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(query);
		    
			if (rs.next()) {
				loc = new GriddedLocation(
					rs.getInt("ID"),
					rs.getInt("ROW"),
					rs.getInt("COL"),
					rs.getDouble("LAT"),
					rs.getDouble("LON"));
			}
			rs.close();
			statement.close();
		} catch(SQLException e) {
			Tools.errorMessage("DatabaseH2", "getLocation", "query failed", e);
		}
		
		return loc;
	}
	
	/* getTimestamps
	 * 
	 * Return an ArrayList of all timestamps in the database.
	 */
	public ArrayList<Timestamp> getTimestamps() {

		// Have we already read them in? Don't bother... but only if we're
		// not writing into the database.
		if (timestamps != null && status == Status.CONNECTED_READ_ONLY) return timestamps;
		
		timestamps = new ArrayList<Timestamp>();

	    String query = "SELECT * FROM " + Table.TIMESTAMPS.name() +	" ORDER BY ID";
	    
		try {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(query);
		    
			while (rs.next()) {
				Timestamp t = new Timestamp(rs.getInt("ID"), rs.getFloat("TIMESTAMP"));
				timestamps.add(t);
			}
			rs.close();
			statement.close();
		} catch(SQLException e) {
			Tools.errorMessage("DatabaseH2", "getTimestamps", "query failed", e);
		}
				
		return timestamps;
	}

	/* getLocations
	 * 
	 * Return an arraylist of all locations in the database.
	 */
	 public ArrayList<GriddedLocation> getLocations() { 

		// Have we already read them in? Don't bother... but only if we're
		// not writing into the database.
		if (locations != null && status == Status.CONNECTED_READ_ONLY) return locations;
			
		locations = new ArrayList<GriddedLocation>();

	    String query = "SELECT * FROM " + Table.LOCATIONS.name() +	" ORDER BY ID";
	    
		try {
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(query);
		    
			while (rs.next()) {
				GriddedLocation loc = new GriddedLocation(
						rs.getInt("ROW"),
						rs.getInt("COL"),
						rs.getDouble("LAT"),
						rs.getDouble("LON"));
				
				loc.id = rs.getInt("ID");
				locations.add(loc);
			}
			rs.close();
			statement.close();
		} catch(SQLException e) {
			Tools.errorMessage("DatabaseH2", "getLocations", "query failed", e);
		}
				
		return locations;
	 }
	
	
	/* getVectorsAtTime
	 * 
	 * Return all the vectors in the database at the specified time index ID.
	 */
	public ArrayList<GriddedVector> getVectorsAtTime( int timeID ) {

		// When we create the vectors, we'll also need the locations and timestamps.
		locations = getLocations();
		timestamps = getTimestamps();
		
		// An arraylist to store the vectors.
		ArrayList<GriddedVector> vectors = new ArrayList<GriddedVector>();
		
	    String query = "SELECT * FROM " + Table.VECTORS.name() + " WHERE TIMESTAMPID = " + timeID;
	    ///String query = "SELECT * FROM " + Table.VECTORS.name();
	    
		try {
			GriddedLocation loc;
			GriddedVector vec;
			
			Statement statement = conn.createStatement();
			ResultSet rs = statement.executeQuery(query);

			while (rs.next()) {
				loc = getLocation(rs.getInt("LOCATIONID"));
				
				// Create the vector from the value, location and timestamp.
				vec = new GriddedVector(rs.getInt("VALUE"),	loc, timeID );
				
				// Add it to the arraylist.
				vectors.add(vec);
			}
			
			rs.close();
			statement.close();
		} catch(SQLException e) {
			Tools.errorMessage("DatabaseH2", "getVectorsAtTime", "query failed", e);
		}

		return vectors;
	}	

	/* getVectorsInTimeRange
	 * 
	 * Return all the vectors in the database in the range of indices.
	 */
	public ArrayList<GriddedVector> getVectors( int first, int last ) {

		ArrayList<GriddedVector> vectors = new ArrayList<GriddedVector>();
		
		for (int i = first; i <= last; i++) {
			vectors.addAll( getVectorsAtTime(i) );			
		}
	
		return vectors;
	}	

	public int numberOfTimestamps() { return metadata.timestamps; }	
	public int numberOfLocations() { return metadata.locations; }	
	public int numberOfVectors() { return metadata.vectors; }
	public int rows() { return metadata.rows; }
	public int cols() { return metadata.cols; }
	
	public void status() {
		updateMetadata(metadata);
		
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