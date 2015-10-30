package condense;

/* DatasetSurface
 * 
 * File and data handling for surface datasets.
 */

public class DatasetSurface extends Dataset {

	public enum Surface {
		LAND(0, "LAND"),
		ICE(101, "ICE"),
		COAST(252, "COAST"),
		UNKNOWN(254, "UNKNOWN"),
		WATER(255, "WATER");
		
		private final int type;
		private final String name;

		Surface( int type, String name ) {
			this.type = type;
			this.name = name;
		}

		public boolean isLand(int i) {return (i == LAND.type);}
		public boolean isIce(int i) {return (i == ICE.type);}
		public boolean isCoast(int i) {return (i == COAST.type);}
		public boolean isWater(int i) {return (i == WATER.type);}

		public String surfaceAsText(int i) {
		    if (isIce(i))return ICE.name;
		    if (isLand(i)) return LAND.name;
		    if (isCoast(i)) return COAST.name;
		    if (isWater(i)) return WATER.name;
		    return UNKNOWN.name;
		}
		
		public int value() { return type; };
		public String asString() { return name; };
	}
	
	/*
	 * readData
	 * 
	 * Read a surface mask file, add it to the database. - surfaceFile contains
	 * the surface data - surfaceLats is a file containing the latitudes -
	 * surfaceLons is a file containing the longitudes
	 */
	public static short[][] readData(String surfaceFile, GriddedLocation[][] locs,
			String surfaceLats,	String surfaceLons) {

		Tools.debugMessage("Reading surface mask files");

		int rows = locs.length;
		int cols = locs[1].length;
		
		short[][] data = new short[rows][cols];

		// Read all the surface data, add it to the database.
		try {

			DataFile file = new DataFile(surfaceFile);
			//surfaceBytes = file.readBytes2D(rows, cols);
			data = file.readShorts2D(rows, cols);

			/*
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					data[r][c] = Tools.unsignedByteToInt(surfaceBytes[r][c]);
					
					// Create the surface vector. 0 is used for the timestampID
					// because all surface vectors will have the same time.
					data[r][c] = face(value, locs[r][c], 0);
				}
			}
			*/
			file.close();
		} catch (Exception e) {
			Tools.errorMessage("DatasetSurface", "readData",
					"error on file read", e);
			return null;
		}

		return data;
	}

	/*
	 * getLocations
	 * 
	 * Return the list of locations.
	 */
	public static GriddedLocation[][] getLocations(String path) {

		// TODO: hardcoded dimensions, for now.
		// Size of the 12.5 km resolution files
		int rows = 1441;
		int cols = 1441;

		GriddedLocation[][] locs = new GriddedLocation[rows][cols];
		
		// TODO
		String surfaceLats = path + "";
		String surfaceLons = path + "";
		
		// Read all the surface data, add it to the database.
		try {
			// Read the locations.
			// Start with the latitudes
			DataFile file = new DataFile(surfaceLats);
			int[][] lats = file.readInts2D(rows, cols);
			file.close();

			// Read the longitudes
			file = new DataFile(surfaceLons);
			int[][] lons = file.readInts2D(rows, cols);
			file.close();

			// Locations of the vectors
			locs = new GriddedLocation[rows][cols];

			// Create the array of locations.
			// Divide by 100000 to decode integer lat/lon into decimal degrees
			// (doubles).
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					locs[r][c] = new GriddedLocation(r, c,
							((double) lats[r][c]) / 100000.,
							((double) lons[r][c]) / 100000.);
				}
			}

					} catch (Exception e) {
			Tools.errorMessage("DatasetSurface", "getLocations",
					"error on file read", e);
			return null;
		}

		return locs;
	}

	/*
	 * getTime
	 * 
	 * Return a timestamp of the surface data.
	 */
	public Timestamp getTime() {
		return new Timestamp(2001, 1, 1);
	}

	public static Timestamp getTimeStatic() {
		return new Timestamp(2001, 1, 1);
	}
}
