package helper_classes;

///todo
///import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;

/* Database
 * 
 * Database emulator: for sensor vector data, locations, timestamps, and metadata. 
 */

public class DatabaseFileSchema extends Database {

	String metadataFilename = "";
	String locationsFilename = "";
	String timestampsFilename = "";
	String vectorsFilename = "";

	DataFile metadataFile = null;
	DataFile locationsFile = null;
	DataFile timestampsFile = null;
	DataFile vectorsFile = null;

	ArrayList<Timestamp> timestamps = null;

	public DatabaseFileSchema(String path, String name ) {
		super(path, name);
		
		metadataFilename = path + dbName + "_metadata.db";
		locationsFilename = path + dbName + "_locations.db";
		timestampsFilename = path + dbName + "_timestamps.db";
		vectorsFilename = path + dbName + "_vectors.db";

		metadata = new Metadata();
		
		status = Status.DISCONNECTED;
	}

	/*
	 * connect
	 * 
	 * Attaches to the database. Warning: in a file-based schema, this will
	 * over-write existing database files.
	 */
	public boolean connect() {

		metadataFile = new DataFile();
		timestampsFile = new DataFile();
		locationsFile = new DataFile();
		vectorsFile = new DataFile();

		try {
			metadataFile.create(metadataFilename);
			timestampsFile.create(timestampsFilename);
			locationsFile.create(locationsFilename);
			vectorsFile.create(vectorsFilename);
		} catch (Exception e) {
			Tools.errorMessage("DatabaseFileSchema", "create", "could not open file", e);
			
			return false;
		}

		status = Status.CONNECTED;
		
		return true;
	}

	/*
	 * connectReadOnly
	 * 
	 * Open the database (files) for reading.
	 */
	public boolean connectReadOnly() {
		try {
			metadataFile.open();
			timestampsFile.open();
			locationsFile.open();
			vectorsFile.open();
		} catch (Exception e) {
			Tools.errorMessage("DatabaseFileSchema", "open", "error trying to open database file(s)", e);
			return false;
		}

		status = Status.CONNECTED_READ_ONLY;
		
		return true;
	}

	/*
	 * disconnect
	 * 
	 * Close any open database connections.
	 */
	public void disconnect() {

		// If the database was just written, also write the total number of
		// entries into the metadata file before closing... will come in handy
		// when the database is re-opened.
		if (status == Status.CONNECTED && metadataFile != null) {
			try {
				metadataFile.writeInt(metadata.timestamps);
				metadataFile.writeInt(metadata.locations);
				metadataFile.writeInt(metadata.vectors);
			} catch (Exception e) {
				Tools.errorMessage("DatabaseFileSchema", "close", "Could not write entries to metadata file on close", e);
			}
		}

		if (metadataFile != null)
			metadataFile.close();
		if (timestampsFile != null)
			timestampsFile.close();
		if (locationsFile != null)
			locationsFile.close();
		if (vectorsFile != null)
			vectorsFile.close();

		status = Status.DISCONNECTED;
	}

	/*
	 * clean
	 * 
	 * Any existing files will be overwritten, so we don't need to do anything.
	 */
	public void clean() {};
	
	/*
	 * store Metadata
	 * 
	 * Create a data file (database) containing the metadata.
	 */
	public void storeMetadata(Metadata m) {

		// Sanity check
		if (status != Status.CONNECTED) {
			Tools.errorMessage("DatabaseFileSchema", "store", "Metadata: database is not open for writing",
					new Exception());
		}
		
		metadata = m;
		
		// Write the data
		try {
			// Write the metadata
			metadataFile.writeInt(m.rows);
			metadataFile.writeInt(m.cols);
		} catch (Exception e) {
			Tools.errorMessage("DatabaseFileSchema", "store", "error on file write", e);
		}
	}

	/*
	 * store Locations
	 * 
	 * Create a data file (database) containing an array of locations.
	 */
	//TODO: id?
	public void storeLocationArray(GriddedLocation[][] locs) {

		for (int r = 0; r < locs.length; r++) {
			for (int c = 0; c < locs[0].length; c++) {
				storeLocation(locs[r][c]);
			}
		}
	}

	/*
	 * storeLocations
	 * 
	 * Create a data file (database) containing an arraylist of locations.
	 */
	public void storeLocationList(ArrayList<GriddedLocation> locs) {
		Iterator<GriddedLocation> iterator = locs.iterator();
		while (iterator.hasNext()) {
			GriddedLocation location = iterator.next();
			storeLocation(location);
		}
	}

	/*
	 * store a single Location
	 * 
	 * Create a data file (database) entry for a single location.
	 */
	public int storeLocation(GriddedLocation loc) {

		try {
			locationsFile.writeInt(loc.row());
			locationsFile.writeInt(loc.col());
			locationsFile.writeDouble(loc.lat());
			locationsFile.writeDouble(loc.lon());
			metadata.locations++;
		} catch (Exception e) {
			Tools.errorMessage("DatabaseFileSchema", "store", "Location: error on file write", e);
		}
		
		metadata.locations++;
		
		return metadata.locations;
	}

	/*
	 * store a Timestamp
	 */
	public int storeTimestamp(Timestamp t) {

		try {
			timestampsFile.writeDouble(t.days());
			metadata.timestamps++;
		} catch (Exception e) {
			Tools.errorMessage("DatabaseFileSchema", "store", "Timestamp: error when writing to file: "
					+ timestampsFilename, e);
		}
		
		return metadata.timestamps;
	}

	/*
	 * store a vector by values
	 * 
	 */
	public void storeVector(int data, int row, int col, int timeID) {
		try {
			vectorsFile.writeInt(data);
			vectorsFile.writeInt(row);
			vectorsFile.writeInt(col);
			vectorsFile.writeInt(timeID);
			metadata.vectors++;
		} catch (Exception e) {
			Tools.errorMessage("DatabaseFileSchema", "store", "vector data: error when writing to file: "
					+ vectorsFilename, e);
		}
	}

	/*
	 * store a vector
	 */
	public void storeVector(GriddedVector v) {

		try {
			vectorsFile.writeInt(v.data());
			vectorsFile.writeInt(v.locationID());
			vectorsFile.writeInt(v.timestampID);
			metadata.vectors++;
			
		} catch (Exception e) {
			Tools.errorMessage("DatabaseFileSchema", "store", "vector: error when writing to file: "
					+ vectorsFilename, e);
		}
	}

	/*
	 * store a vector array
	 */
	public void storeVectorArray(GriddedVector[][] v) {
		for (int r = 0; r < v.length; r++) {
			for (int c = 0; c < v[0].length; c++) {
				storeVector(v[r][c]);
			}
		}
	}

	public int numberOfTimestamps() {return metadata.timestamps;}


	/*
	 * get the metadata
	 */
	public Metadata getMetadata() {

		// Sanity check
		if (status != Status.CONNECTED && status != Status.CONNECTED_READ_ONLY) {
			Tools.errorMessage("DatabaseFileSchema", "getMetadata", "database is not open for reading",
					new Exception());
		}
		
		// Read the data
		try {
			// Write the metadata
			metadata.rows = metadataFile.readInt();
			metadata.cols = metadataFile.readInt();
			metadata.timestamps = metadataFile.readInt();
			metadata.locations = metadataFile.readInt();
			metadata.vectors = metadataFile.readInt();
		} catch (Exception e) {
			Tools.errorMessage("DatabaseFileSchema", "getMetadata", "error on file read", e);
		}
		
		return metadata;
	}

	/* getLocation
	 * 
	 * Retrieve a single location from the database file based on the location ID.
	 */
	public GriddedLocation getLocation(int id) {
		GriddedLocation loc = new GriddedLocation( 0,0,0,0,0 );
		
		Tools.statusMessage("GetLocation not implemented in DatabaseFileSchema");
		return loc;
	}

	/*
	 * getLocations
	 * 
	 * Retrieve all the locations from the database file, and return them as
	 * an array list.
	 */
	public ArrayList<GriddedLocation> getLocations() {
		ArrayList<GriddedLocation> locations = new ArrayList<GriddedLocation>();
		
		Tools.statusMessage("GetLocations not implemented in DatabaseFileSchema");
		return locations;
	}

	/*
	 * getTimestamps
	 * 
	 * Retrieve all the timestamps from the database file, and return them as
	 * an array list.
	 */
	public ArrayList<Timestamp> getTimestamps() {
		timestamps = new ArrayList<Timestamp>();
		
		try {
			for (int i = 0; i < metadata.timestamps; i++) {
				double ts = timestampsFile.readDouble();
				timestamps.add( new Timestamp( ts ));
			}			
		} catch( Exception e ) {
			Tools.warningMessage("DatabaseFileSchema::getTimestamps - Error when reading timestamps");
			Tools.warningMessage("Exception: " + e);
		}

		return timestamps;
	}

	/*
	 * get a timestamp
	 * 
	 * Return the timestamp at index i;
	 */
	public Timestamp getTimestamp(int index) {
		if (timestamps == null) timestamps = getTimestamps();
		
		return timestamps.get(index);
	}

	public int numberOfVectors() { return metadata.vectors; }

	/*
	 * getVectors
	 * 
	 * Retrieve all the vectors between a range of time stamps.
	 */
	public ArrayList<GriddedVector> getVectors(int startIndex, int endIndex) {
		
		ArrayList<GriddedVector> vectorList = new ArrayList<GriddedVector>(); 
		
		if (metadata == null) metadata = getMetadata();
		if (timestamps == null) timestamps = getTimestamps();

		try {
			for (int i = 0; i < metadata.vectors; i++) {
				int value = vectorsFile.readInt();
				int locID =  vectorsFile.readInt();
				int timeID =  vectorsFile.readInt();
				
				GriddedLocation loc = getLocation( locID );
				
				if (timeID >= startIndex && timeID <= endIndex) {
					vectorList.add( new GriddedVector(value, loc, timeID));
				}
			}
		} catch( Exception e ) {
			Tools.warningMessage("DatabaseFileSchema::getVectors: while reading vector file.");
			Tools.warningMessage("Exception: " + e);
			return vectorList;
		}

		return vectorList;
	}

	/*
	 * getVectorsAtTime
	 * 
	 * Retrieve all the vectors at a specific timestamp.
	 */
	public ArrayList<GriddedVector> getVectorsAtTime(int ID) {
		
		ArrayList<GriddedVector> vectorList = new ArrayList<GriddedVector>(); 
		
		if (metadata == null) metadata = getMetadata();
		if (timestamps == null) timestamps = getTimestamps();

		try {
			for (int i = 0; i < metadata.vectors; i++) {
				int value = vectorsFile.readInt();
				int locID =  vectorsFile.readInt();
				int timeID =  vectorsFile.readInt();
				
				GriddedLocation loc = getLocation( locID );
				
				if (timeID == ID) {
					vectorList.add( new GriddedVector(value, loc, timeID));
				}
			}
		} catch( Exception e ) {
			Tools.warningMessage("DatabaseFileSchema::getVectorsAtTime: while reading vector file.");
			Tools.warningMessage("Exception: " + e);
			return vectorList;
		}

		return vectorList;
	}

	public void status() {

		Tools.statusMessage("Database name = " + dbName + "  Status: "
				+ status.toString());
		Tools.statusMessage("Database path = " + dbPath);
		Tools.statusMessage("metadataFilename = " + metadataFilename);
		Tools.statusMessage("locationsFilename = " + locationsFilename);
		Tools.statusMessage("timestampsFilename = " + timestampsFilename);
		Tools.statusMessage("vectorsFilename = " + vectorsFilename);
		Tools.statusMessage("Timestamp entries = " + metadata.timestamps);
		Tools.statusMessage("Location entries  = " + metadata.locations);
		Tools.statusMessage("Vector entries    = " + metadata.vectors);
	}
}
