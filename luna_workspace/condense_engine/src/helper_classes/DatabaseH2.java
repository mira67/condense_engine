package helper_classes;

import java.util.ArrayList;
///TODO
///import java.util.Iterator;
import java.sql.*;

/* Database
 * 
 * Database emulator: for sensor vector data, locations, timestamps, and metadata. 
 */

public class DatabaseH2 extends Database {
	
	Connection conn;
	DatabaseMetaData md;
	
	// For Hibernate usage. Does nothing.
	private DatabaseH2() {}
	private PreparedStatement sqlStmt_tb = null;
	private PreparedStatement sqlStmt_map = null;
	private Statement sqlCreate = null;
	private ResultSet sqlRs = null;
	private int index = 0;
   	
	public DatabaseH2(String name, String polarization, int frequency) {
		this();
		dbName = name;
		tbName = "ch" + frequency + polarization;
		status = Status.DISCONNECTED;
		chFreq = frequency;
	}

	/*
	 * connect
	 * 
     * (non-Javadoc)
	 * @see org.nsidc.bigdata.Database#connect()
	 * 
	 * Connect to a database. If it does not exist it will be created.
	 */
	public void connect() {
		
		// Open the database.
		try {
	        Class.forName("org.h2.Driver");
	        //conn = DriverManager.getConnection("jdbc:h2:tcp://localhost/mem:~/"+dbName+";DB_CLOSE_DELAY=-1", "sa", null);//jdbc:h2:mem:db1
	        conn = DriverManager.getConnection("jdbc:h2:tcp://10.240.210.131:9292/mem:~/"+dbName+ "Test"+";DB_CLOSE_DELAY=-1", null, null);//DB_CLOSE_DELAY=-1
	        //conn = DriverManager.getConnection("jdbc:h2:tcp://localhost/~/"+dbName+ "?user=sa&password=1234", null, null);//DB_CLOSE_DELAY=-1

	        conn.setAutoCommit(true);
	     	
	        //drop table is existed for repeat debugging purpose -  Todo-get to another function for easy management
	        sqlCreate = conn.createStatement();
	        Boolean status = sqlCreate.execute("DROP TABLE IF EXISTS " + tbName);
	        Tools.debugMessage("DatabaseH2 Table Status " + status);
	        
	        //test sql statement - create a table
	        sqlCreate = conn.createStatement();
	     	//Execute SQL query
	     	status = sqlCreate.execute("CREATE TABLE IF NOT EXISTS " + tbName + "(date DATE, locID INT, bt SMALLINT, CONSTRAINT IF NOT EXISTS pixelID PRIMARY KEY (date, locID))");
	     	Tools.debugMessage("DatabaseH2 Create Table Status " + status);
	     	//test sql statement - row record
	        sqlStmt_tb = conn.prepareStatement(
					"INSERT INTO " + tbName +
					"(date, locID, bt) " + 
					"values " + 
					"(?,?,?)");
	        
		} catch(Exception e) {
			Tools.errorMessage("DatabaseH2", "connect", "Could not connect with database " + dbName, e);
		}
		
		Tools.statusMessage("Connected to database: " + dbName + tbName);
		status = Status.CONNECTED;
    }

	/*
	 * connectReadOnly
	 * 
     * (non-Javadoc)
	 * @see org.nsidc.bigdata.Database#connectReadOnly()
	 * 
	 * Connect to an existing database. If it doesn't exist throw an error.
	 */
    public void connectReadOnly() {
		try {
	        Class.forName("org.h2.Driver");
	        //conn = DriverManager.getConnection("jdbc:h2:tcp://localhost/mem:~/"+dbName+";IFEXISTS=TRUE", "sa", null);//
	        //in-memory db test
	        conn = DriverManager.getConnection("jdbc:h2:tcp://10.240.210.131:9292/mem:~/"+dbName+ "Test"+";DB_CLOSE_DELAY=-1", null, null);//DB_CLOSE_DELAY=-1
		} catch(Exception e) {
			Tools.errorMessage("DatabaseH2", "connectReadOnly", "Could not connect with database " + dbName, e);
		}
		
		Tools.statusMessage("Connected to database read-only: " + dbName);
		status = Status.CONNECTED_READ_ONLY;
    }

    /*
     * disconnect
     *
     * Disconnect from an open database.
     * (non-Javadoc)
     * @see org.nsidc.bigdata.Database#disconnect()
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
    }
    //Check if table already existed, if not create new->return new, otherwise return old
    //currently only for location table
    public String checkTable(String tbNames){
    	
    	try {
			md = conn.getMetaData();
			ResultSet rs = md.getTables(null, null, tbNames, null);
	    	while (rs.next()) {
	    		Tools.statusMessage("LOCATION MAP Table Found: " + rs.getString("TABLE_NAME"));
	    		if (rs.getString("TABLE_NAME") == tbNames){
	    			rs.close();
	    			return "Old";
	    		}
	        }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
      try {
		sqlCreate = conn.createStatement();
	   	//Execute SQL query
	   	Boolean status = sqlCreate.execute("CREATE TABLE IF NOT EXISTS "+ tbNames + "(id INT primary key, row SMALLINT, col SMALLINT)");
	    sqlStmt_map = conn.prepareStatement(
					"INSERT INTO "+ tbNames +
					"(id,row, col) " + 
					"values " + 
					"(?,?,?)");
	  } catch (SQLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	  }
    return "New";
   }
    
    //If table not existied, create new table and pre-defined SQL for table entries recording
    public void createMap(String tbNames){
		//write location map table
		int pixelId = 0;
		Tools.statusMessage("Creating pixel location map...");
		int rows = 316, cols = 332;
		if ((chFreq == 85) || (chFreq == 91)){
			rows = 632;
			cols = 664;
		}
		for (int r = 0; r < rows; r++){
			for (int c = 0; c < cols; c++){
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
	}
	
	public void store(Timestamp t) {}
	public void store(ArrayList<GriddedLocation> locs) {}
	public void store(GriddedLocation loc) {}
	public void store(GriddedVector v) {}

	/*
	 * store a vector by values
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
	        //Tools.debugMessage("Affected rows = " + rowsAffected);
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
	        //Tools.debugMessage("Affected rows = " + rowsAffected);
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
	public void add( GriddedLocation[][] loc) { 
		for (int r = 0; r < loc.length; r++) {
			for (int c = 0; c < loc[0].length; c++) {
				//TODO locations.add(loc[r][c]);
			}
		}
	}

	/*
	 *  Add an array of sensor vectors (like surface data)
	 */
	public void add( GriddedVector[][] v) { 
		for (int r = 0; r < v.length; r++) {
			for (int c = 0; c < v[0].length; c++) {
				//TODO vectors.add(v[r][c]);
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
	
	
	public int rows() { return metadata.rows(); }
	public int cols() { return metadata.cols(); }
	
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
		Tools.statusMessage("Timestamp entries = " + metadata.timestamps);
		Tools.statusMessage("Location entries  = " + metadata.locations);
		Tools.statusMessage("Vector entries    = " + metadata.vectors);
	}

	@Override
	public void store(int data, int row, int col, int time) {
		// TODO Auto-generated method stub
		
	}

}

