package condense;

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

    public DatabaseRamSchema(String path, String name) {
    	super(path, name);
		metadata = new Metadata();
		timestamps = new ArrayList<Timestamp>();
		locations = new ArrayList<GriddedLocation>();
		vectors = new ArrayList<GriddedVector>();
	}

	public DatabaseRamSchema(String name) {
		super("", name);
	}

	public boolean connect() {
		status = Status.CONNECTED;
		return true;
    }

    public boolean connectReadOnly() {
		status = Status.CONNECTED_READ_ONLY;
		return true;
    }

    public void disconnect() {
		status = Status.DISCONNECTED;
    }

	public void clean() {};
	
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
    
	public void storeMetadata( Metadata m ) {
		metadata = m;
	}
	
	public void store(Timestamp t) {timestamps.add(t);}
	public void store(ArrayList<GriddedLocation> locs) {locations.addAll(locs);}
	
	public int store(GriddedLocation loc) {
		locations.add( loc );	
		metadata.locations++;
		return metadata.locations;
	}
	
	public void store(GriddedVector v) {vectors.add(v);}

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

	//
	// RETRIEVAL METHODS
	//
	
	public Metadata getMetadata() { 
		updateMetadata();
		return metadata;
	}
	
	protected void updateMetadata() {
		metadata.vectors = vectors.size();
		metadata.timestamps = (short) timestamps.size();
		metadata.locations = locations.size();				
	}
	
	public Timestamp getTimestamp(int i) { return timestamps.get(i); }
	
	public int numberOfTimestamps() { return timestamps.size(); }
	public ArrayList<Timestamp> getTimestamps() { return timestamps; }

	public ArrayList<GriddedLocation> getLocations() { return locations; }
	
	public int numberOfVectors() { return vectors.size(); }
	public ArrayList<GriddedVector> getVectors() { return vectors; }
	
	public int rows() { return metadata.rows; }
	public int cols() { return metadata.cols; }
	
	/* getVectorsAtTimestamp
	 * 
	 * Return all the vectors in the database at the specified timestamp.
	 */
	public ArrayList<GriddedVector> getVectorsAtTimestamp( int timeID ) {
		ArrayList<GriddedVector> subset = new ArrayList<GriddedVector>();
		
		Iterator<GriddedVector> iterator = vectors.iterator();
        while (iterator.hasNext()) {
            GriddedVector vector = iterator.next();
            if (vector.timestampID == timeID) subset.add(vector);
        }
		
		return subset;
	}	

	
	/* getVectorsAtTime
	 * 
	 * Return all the vectors in the database at the specified timestamp ID
	 */
	public ArrayList<GriddedVector> getVectorsAtTime( int index ) {

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
        
		subset = getVectorsAtTimestamp( t.id );

		return subset;
	}	

	
	/* getVectorsInTimeRange
	 * 
	 * Return all the vectors in the database in the range of time IDs.
	 */
	public ArrayList<GriddedVector> getVectors( int first, int last ) {

		ArrayList<GriddedVector> subset = new ArrayList<GriddedVector>();
		
		if (first > last || first < 0) {
			Tools.warningMessage("DatabaseRamSchema::getVectors: Warning: " +
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
    		subset.addAll( getVectorsAtTimestamp( t.id ));
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
		Tools.statusMessage("Rows              = " + metadata.rows);
		Tools.statusMessage("Cols              = " + metadata.cols);
	}
	
	public short storeTimestamp(Timestamp t) {
		metadata.timestamps++;		
		return metadata.timestamps;
	}

	@Override
	public int storeLocation(GriddedLocation loc) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void storeVector(GriddedVector v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void storeVector(short i, int locationID, short timestampID) {
		// TODO Auto-generated method stub
		
	}
}

