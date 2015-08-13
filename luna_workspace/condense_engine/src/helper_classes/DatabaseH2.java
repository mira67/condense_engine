package helper_classes;

import java.util.ArrayList;
import java.sql.*;

/* DatabaseH2
 * 
 * Encapsulates the H2 database, for standardized interfacing. 
 *
 * Example connection paths:
 * 			"jdbc:h2:tcp://localhost/~/"
 * 			"jdbc:h2:tcp://10.240.210.131:9292/mem:~/"
 */

public class DatabaseH2 extends Database {
	
	Connection conn;
	
	private PreparedStatement sqlStmt_tb;
	private PreparedStatement sqlStmt_map;
	private Statement sqlCreate;
	private ResultSet results;

	// Constructor
	public DatabaseH2(String path, String name) {
		super( path, name );
	}

	/*
	 * connect
	 * 
	 * Connect to the database for writing. If it does not exist it will be created.
	 */
	public boolean connect() {

		// Open the database.
		try {
			Class.forName("org.h2.Driver");
			conn = DriverManager.getConnection(dbPath + dbName, null, null);

			conn.setAutoCommit(true);

			// Make sure the tables exist. Create them if they don't exist. 
			checkTables( true );
			
		} catch (Exception e) {
			Tools.errorMessage("DatabaseH2", "connect",
					"Could not connect with database " + dbName +
					", Path: " + dbPath, e);
			return false;
		}

		status = Status.CONNECTED;
		
		return true;
	}


	/*
	 * clean
	 * 
	 * Reset the database, clean out all existing data and tables. Empty
	 * tables are created afterward.
	 */
	public void clean() {
		for (Table table : Table.values()) {
			try {
				sqlCreate = conn.createStatement();
				Boolean status = sqlCreate.execute("DROP TABLE IF EXISTS " + table.name);
				Tools.debugMessage("DatabaseH2 table clean: " + table.name + " status: " + status);				
			} catch (Exception e) {
				// TODO Do nothing right now.
				// Assume it either didn't exist, or was dropped successfully.
				// Might want to re-visit this assumption.
			}
		}
		
		// We've deleted everything. Now make the tables again
		try {
			checkTables( true );
		} catch( Exception e ) {
			Tools.warningMessage("Could not re-create tables after cleaning the database.");
		}
	}
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

	/*
	 * connectReadOnly
	 * 
  	 * Connect to an existing database for reading. Return true on success.
	 */
    public boolean connectReadOnly() {
		try {
	        Class.forName("org.h2.Driver");
	        conn = DriverManager.getConnection(dbPath+dbName+";IFEXISTS=TRUE", null, null);//

			// Make sure the tables exist. Don't create any if they don't exist.
	        checkTables( false );
	        
	    } catch(Exception e) {
			Tools.errorMessage("DatabaseH2", "connectReadOnly", "Could not connect with database " + dbName, e);
			
			return false;
		}
		
		Tools.statusMessage("Connected to database, read-only: " + dbName);
		status = Status.CONNECTED_READ_ONLY;
		
		return true;
    }

    /*
     * disconnect
     *
     * 
     */
    public void disconnect() {
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

	/*
	 * checkTables
	 * 
	 * Check to see if the tables already exist. If not, create a new one (if the
	 * flag is set to true).
	 */
	public void checkTables(Boolean createIfDoesNotExist) throws Exception {
		
		// Iterate through the tables that should be in the database.
		for (Table table : Table.values()) {

			// See if the table is in the database.
			results = conn.getMetaData().getTables(null, null, table.name, null);

			boolean exists = false;
			
			// Does the table exist?
			try {
				// Look through the returned information for the table name.
				while (results.next()) {
					if (results.getString("TABLE_NAME") == table.name) {
						
						// Yes, found it.
						exists = true;
						Tools.debugMessage("checkTables: found table " + table.name);
						break;
					}
				}
				
				results.close();
				
			} catch (SQLException e) {
				Tools.errorMessage("DatabaseH2", "checkTables", "When looking for a table: " +
						table.name, e);
				throw(e);
			}

			// If the table doesn't exist, create it.
			if (!exists && createIfDoesNotExist) {
				if (!createTable(table)) {
					Tools.errorMessage("DatabaseH2", "checkTables", "Failed to create table: " 
							+table.name, new Exception("Giving up."));
				}
				exists = true;
			}
			
			// Okay, the table should be in there now. If not, we've got problems.
			if (!exists) {
				Tools.errorMessage("DatabaseH2", "checkTables", "Could not find and/or create " +
						"table: " +table.name, new Exception("Giving up."));
			}	
		}
	}

	/*
	 * createTable
	 * 
	 * Create a new table in the database if it doesn't already exist. Returns
	 * true on success.
	 */
	protected boolean createTable(Table table) {
		try {
			Tools.debugMessage("Creating table: " + table.name);
			sqlCreate = conn.createStatement();
			sqlCreate.execute("CREATE TABLE IF NOT EXISTS " + table.name
					+ "(id INT primary key, row SMALLINT, col SMALLINT)");
			sqlStmt_map = conn.prepareStatement("INSERT INTO " + table.name
					+ "(id,row, col) " + "values " + "(?,?,?)");
		} catch (SQLException e) {
			Tools.warningMessage("Could not create table: " + table.name);
			Tools.warningMessage("Exception: " + e);
			return false;
		}

		return true;
	}
	
	
    /*
     * /If table does not exist, create a new table and pre-defined SQL for table entries recording
     */
    public void createMap(){
		//write location map table
		int pixelId = 0;
		Tools.statusMessage("Creating pixel location map...");
		
		if (metadata == null) {
			Tools.errorMessage("DatabaseH2", "createMap", "Metadata has not been set", new Exception());
		}
	
		for (int r = 0; r < metadata.rows; r++){
			for (int c = 0; c < metadata.cols; c++){
				store(pixelId, r, c);
				pixelId++;
			}
		}
		
		Tools.statusMessage("Done with MAP, starting condense...");
    }

    
    protected void writeCheck(String methodName) {
    	if (status != Status.CONNECTED) {
			Tools.errorMessage("DatabaseH2", methodName, 
					"Database " + dbName + " is not open for writing", new Exception());
		}
    }
    
    //
    // STORAGE METHODS
    //
    
	public void store( Metadata m ) {
		writeCheck("Metadata");
		metadata = m;
		// TODO: store the metadata in the database
	}
	
	// TODO
	public void store(Timestamp t) {}
	public void store(ArrayList<GriddedLocation> locs) {}
	public void store(GriddedLocation loc) {}
	public void store(GriddedVector v) {}

	/*
	 * Store a vector by values
	 * 
	 */
	public void store(int data, int locID, Date date) {
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
		
	}
	
	/*
	 * store a loc map id: {row, col}
	 * 
	 */
	public void store(int id, int row, int col) {
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
		
	}

	/*
	 * store a vector array
	 */
	public void store(GriddedVector[][] v) {
		for (int r = 0; r < v.length; r++) {
			for (int c = 0; c < v[0].length; c++) {
				store(v[r][c]);
			}
		}
	}

	/*
	 *  Add an array of locations
	 */
	public void add( GriddedLocation[][] locs) { 
		for (int r = 0; r < locs.length; r++) {
			for (int c = 0; c < locs[0].length; c++) {
				store(locs[r][c]);
			}
		}
	}

	/*
	 *  Add an array of sensor vectors
	 */
	public void add( GriddedVector[][] v) { 
		for (int r = 0; r < v.length; r++) {
			for (int c = 0; c < v[0].length; c++) {
				store(v[r][c]);
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
		
		Tools.statusMessage("Database name = " + dbName + "  Status: "
				+ status.toString());
		Tools.statusMessage("Database path = " + dbPath);
		Tools.statusMessage("Timestamp entries = " + metadata.timestamps);
		Tools.statusMessage("Location entries  = " + metadata.locations);
		Tools.statusMessage("Vector entries    = " + metadata.vectors);
	}

	@Override
	public void store(int data, int row, int col, int time) {
		// TODO Auto-generated method stub
		
	}

}

