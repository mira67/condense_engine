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
	 * Read a surface mask file. Return just the surface information.
	 * (No lats or lons.)
	 */
	public static short[][] readData(String surfaceFile, int rows, int cols ) {

		Tools.debugMessage("Reading surface mask file");
		
		short[][] data = new short[rows][cols];

		// Read the surface data
		try {

			DataFile file = new DataFile(surfaceFile);
			data = file.readShorts2D(rows, cols);
			file.close();
		} catch (Exception e) {
			Tools.errorMessage("DatasetSurface", "readData",
					"error on file read", e);
			return null;
		}

		return data;
	}
		
	/*
	 * makeSurface
	 * 
	 * Return the surface array for this data type.
	 */
	public static GriddedVector[][] makeSurface( DataType type,
			String path, String hemisphere, String frequency) {

		Tools.statusMessage("Making the surface data");

		GriddedLocation[][] locs = getLocations( type, path, hemisphere, frequency);
		
		int rows = locs.length;
		int cols = locs[0].length;
		String filename = "";
		
		switch(type) {
		case AVHRR:
			break;
		case SSMI:
			break;
		case EASE_GRID_SURFACE:
			if (hemisphere.equalsIgnoreCase("south"))
				filename = path + "Sh_loci_land50_landcoast12.5km.1441x1441.bin";
			if (hemisphere.equalsIgnoreCase("north"))
				filename = path + "Nh_loci_land50_landcoast12.5km.1441x1441.bin";
			break;
		case SEA_ICE:
			Tools.errorMessage("DatasetSurface", "makeSurface", "SEA_ICE type not implemented",
					new Exception(""));
		}

		short data[][] = readData( filename, rows, cols);
		
		GriddedVector[][] vecs = new GriddedVector[rows][cols];
		
		// Make the surface array
		try {
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					vecs[r][c] = new GriddedVector( data[r][c],
							locs[r][c], 0);
				}
			}
		} catch (Exception e) {
			Tools.errorMessage("DatasetSurface", "makeSurface",
					"Failed to make the locations array. Error:", e);
		}
		
		return vecs;
	}

	/*
	 * getLocations
	 * 
	 * Retrieve the locations for the surface data.
	 */
	public static GriddedLocation[][] getLocations( DataType type, String path,
				String hemisphere, String frequency) {
				
		double lats[][] = null; 
		double lons[][] = null;

		GriddedLocation[][] locs = null;
		
		// TODO: not all lat/lon files will have the same data format.
		// Read the surface data lat/lons
		try {

			switch(type) {
			
			case AVHRR:
				return DatasetAVHRR.getLocations(path, hemisphere);
			
			case SSMI:
				return DatasetSSMI.getLocations(path, hemisphere, frequency);
			
			case EASE_GRID_SURFACE:

				// Standard LOCI surface, unrelated to other data types.
				int rows = 1441;
				int cols = 1441;
				String latsPath = "NO FILE";
				String lonsPath = "NO FILE";
				
				if (hemisphere.equalsIgnoreCase("south")) {
					latsPath = path + "SHLATLSB";
					lonsPath = path + "SHLONLSB";
				}
				
				// Northern hemisphere
				else {
					latsPath = path + "NHLATLSB";
					lonsPath = path + "NHLONLSB";					
				}

				DataFile latsFile = new DataFile(latsPath);
				DataFile lonsFile = new DataFile(lonsPath);
				
				lats = latsFile.readDoubles2D(rows, cols);
				lons = lonsFile.readDoubles2D(rows, cols);

				latsFile.close();
				lonsFile.close();
				
				// Make the locations array
				locs = new GriddedLocation[rows][cols];
				
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < cols; c++) {
						locs[r][c] = new GriddedLocation( r, c, lats[r][c], lons[r][c]);
					}
				}

				break;
				
			case SEA_ICE:
			default:
				Tools.errorMessage("DatasetSurface", "getLocations",
						"Data type not implemented: " + type, new Exception("Exiting"));
			}
				
			
		} catch (Exception e) {
			Tools.message("Could not read lats and/or lons files:");
			Tools.message("   " + path);
			Tools.errorMessage("DatasetSurface", "getLocations",
					"error on file read", e);
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
