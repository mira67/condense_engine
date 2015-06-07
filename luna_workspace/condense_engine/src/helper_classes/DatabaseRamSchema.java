package helper_classes;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;

/* Database
 * 
 * Database emulator: for sensor vector data, locations, timestamps, and metadata. 
 * 
 * Since this database stores everything in RAM, it is transitory -- as soon as the
 * program ends the database will cease to exist.
 */

public class DatabaseRamSchema extends Database {

	String metadataFilename = "";
	String locationsFilename = "";
	String timestampsFilename = "";
	String vectorsFilename = "";

	ArrayList<GriddedLocation> locations;
	ArrayList<Timestamp> timestamps;
	ArrayList<GriddedVector> vectors;

    public DatabaseRamSchema() {
		metadata = new Metadata();
		timestamps = new ArrayList<Timestamp>();
		locations = new ArrayList<GriddedLocation>();
		vectors = new ArrayList<GriddedVector>();
	}

	public DatabaseRamSchema(String name) {
		this();
		dbName = name;
		status = Status.DISCONNECTED;
	}

	public void connect() {
		status = Status.CONNECTED;
    }

    public void connectReadOnly() {
		status = Status.CONNECTED_READ_ONLY;
    }

    public void disconnect() {
		status = Status.DISCONNECTED;
    }
    
    protected void writeCheck(String methodName) {
    	if (status != Status.CONNECTED) {
			Tools.errorMessage("DatabaseRamSchema", methodName, 
					": database is not open for writing", new Exception());
			Tools.exit(1);
		}
    }
    
    //
    // STORAGE METHODS
    //
    
	public void store( Metadata m ) {
		writeCheck("Metadata");
		metadata = m;
	}
	
	public void store(Timestamp t) {timestamps.add(t);}
	public void store(ArrayList<GriddedLocation> locs) {locations.addAll(locs);}
	public void store(GriddedLocation loc) {locations.add( loc );}
	public void store(GriddedVector v) {vectors.add(v);}

	/*
	 * store a vector by values
	 * 
	 */
	public void store(int data, int row, int col, int time) {
		vectors.add( new GriddedVector( data, row, col, time));
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
				locations.add(loc[r][c]);
			}
		}
	}

	/*
	 *  Add an array of sensor vectors (like surface data)
	 */
	public void add( GriddedVector[][] v) { 
		for (int r = 0; r < v.length; r++) {
			for (int c = 0; c < v[0].length; c++) {
				vectors.add(v[r][c]);
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
		metadata.vectors = vectors.size();
		metadata.timestamps = timestamps.size();
		metadata.locations = locations.size();				
	}
	
	public Timestamp get(int i) { return timestamps.get(i); }
	
	public int numberOfTimestamps() { return timestamps.size(); }
	public ArrayList<Timestamp> getTimestamps() { return timestamps; }

	public ArrayList<GriddedLocation> getLocations() { return locations; }
	
	public int numberOfVectors() { return vectors.size(); }
	public ArrayList<GriddedVector> getVectors() { return vectors; }
	
	public int rows() { return metadata.rows(); }
	public int cols() { return metadata.cols(); }
	
	/* getVectorsAtTime
	 * 
	 * Return all the vectors in the database at the specified time index.
	 */
	public ArrayList<GriddedVector> getVectorsAtTime( int time ) {
		ArrayList<GriddedVector> subset = new ArrayList<GriddedVector>();
		
		Iterator<GriddedVector> iterator = vectors.iterator();
        while (iterator.hasNext()) {
            GriddedVector vector = iterator.next();
            if (vector.time() == time) subset.add(vector);
        }
		
		return subset;
	}	

	
	/* getVectorsAtTimeIndex
	 * 
	 * Return all the vectors in the database at the specified time index.
	 */
	public ArrayList<GriddedVector> getVectorsAtTimeIndex( int index ) {

		ArrayList<GriddedVector> subset = new ArrayList<GriddedVector>();
		
		if (index > timestamps.size()) {
			Tools.warningMessage("DatabaseRamSchema::getVectorsAtTimeIndex: Warning: " +
					"Requested time index " + index +
					" is greater than number of timestamps in database (" +
			        timestamps.size() + "). Be sure to index from zero.");
			
			return subset;
		}
		
		
		// Find the timestamp associated with the time index, i.e. the i-th
		// timestamp in the database.
		int counter = 0;
		Iterator<Timestamp> timeIter = timestamps.iterator();
        Timestamp t = null;

        while (timeIter.hasNext()) {
            t = timeIter.next();
            if (counter == index) break;
            counter++;
        }
		
        // If we didn't find anything... Subset will be empty.
        if (t == null || counter > timestamps.size()) return subset;
        
		subset = getVectorsAtTime( (int) t.days() );

		return subset;
	}	

	
	/* getVectorsInTimeRange
	 * 
	 * Return all the vectors in the database in the range of indices.
	 */
	public ArrayList<GriddedVector> getVectors( int first, int last ) {

		ArrayList<GriddedVector> subset = new ArrayList<GriddedVector>();
		
		if (first > last || first < 0) {
			Tools.warningMessage("DatabaseRamSchema::getVectorsInTimeRange: Warning: " +
					"Requested time range " + first + " to " + last +
					" may exceed the range of the timestamps in the datbase " +
					"or be malformed. Be sure to index from zero.");
			
			return subset;
		}
		
		for (int day = first; day <= last; day++) {
    		// Find the timestamp associated with the time index, i.e. the i-th
    		// timestamp in the database.
    		int counter = 0;
    		Iterator<Timestamp> timeIter = timestamps.iterator();
            Timestamp t = null;

            // Find the timestamp associated with 'day' index.
            while (timeIter.hasNext()) {
                t = timeIter.next();
                if (counter == day) break;
                counter++;
            }
            
            // If we've gone beyond the number of timestamps in the database, bail out.
            if (counter > timestamps.size()) return subset;
            
            // If we didn't find anything (no timestamp) subset will be empty.
            if (t == null ) continue;
            
            // Add any vectors we found to the subset list.
    		subset.addAll( getVectorsAtTime( (int) t.days() ) );
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
	public void store(int data, int locID, Date date) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void store(int id, int R, int C) {
		// TODO Auto-generated method stub
		
	}

}
