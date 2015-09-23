import helper_classes.*;

/* a sandbox for code experimentation.
 * 
 */

public class Scratch {

	///static String latsFileName = "C:\\Users\\glgr9602\\Desktop\\condense\\data\\surface\\SHLATLSB";
	///static String lonsFileName = "C:\\Users\\glgr9602\\Desktop\\condense\\data\\surface\\SHLONLSB";
	///static String coastlineFileName = "C:\\Users\\glgr9602\\Desktop\\condense\\data\\surface\\Sh_loci_land50_landcoast12.5km.1441x1441.bin";

	static String latsFileName = "C:\\Users\\glgr9602\\Desktop\\condense\\data\\surface\\SHLATLSB";
	static String lonsFileName = "C:\\Users\\glgr9602\\Desktop\\condense\\data\\surface\\SHLONLSB";
	static String coastlineFileName = "C:\\Users\\glgr9602\\Desktop\\condense\\data\\surface\\Sh_loci_land50_landcoast12.5km.1441x1441.bin";

	final static int ROWS = 1441;
	final static int COLS = 1441;
	
	///final static int ROWS = 721;
	///final static int COLS = 721;
	
	public static void main(String[] args) {

		SurfaceVector[][] surface = new SurfaceVector[ROWS][COLS];

		// Read the file
		try {

			DataFile latitudes = new DataFile( latsFileName );
			DataFile longitudes = new DataFile( lonsFileName );
			DataFile surfaceFile = new DataFile( coastlineFileName );

			for (int r = 0; r < ROWS; r++) {
				for (int c = 0; c < COLS; c++) {
					
					// Read the encoded data from the files
					int lat = latitudes.readInt();
					int lon = longitudes.readInt();
					int type = Tools.unsignedByteToInt(surfaceFile.readByte());
					
					// Reverse the byte order (for Windows).
					lat = Tools.reverseByteOrder(lat);
					lon = Tools.reverseByteOrder(lon);

					// Decode the data, convert to doubles.
					double latD = ((double) lat) / 100000.0;
					double lonD = ((double) lon) / 100000.0;
					
					// Create the suface object at this location
					surface[r][c] = new SurfaceVector( type,
							new GriddedLocation(r,c,latD,lonD), 0);
				}
			}

			latitudes.close();			
			longitudes.close();			
			surfaceFile.close();
		}
		catch(Exception e) {
			Tools.warningMessage("Failed to open file");
		}
		
		// Output the converted file
		makeFile(surface);
	}

	
	/*
	 * makeFile
	 */
	protected static void makeFile(SurfaceVector[][] surface) {
		
		// Use the dates to make file names
		
		String filename = "C:\\Users\\glgr9602\\Desktop\\condense\\data\\surface\\surface-land-"+ROWS+".bin";

		// Write the data to the output file
		try {
			Tools.statusMessage("    output filename = " + filename);
			
			DataFile file = new DataFile();
			file.create(filename);

			double[][] array = new double[ROWS][COLS];
			
			for (int r = 0; r < ROWS; r++) {
				for (int c = 0; c < COLS; c++) {
					array[r][c] = (double) surface[r][c].data();
					
					// Add contrast to the features
					if (array[r][c] == 0) array[r][c] = 50; // land
					if (array[r][c] == 255) array[r][c] = 100; // water
					if (array[r][c] == 101) array[r][c] = 255; // ice
					if (array[r][c] == 252) array[r][c] = 0; // Coastline
					if (array[r][c] == 254) array[r][c] = 175; // off the map
				}
			}
			
			file.writeDouble2d(array);
			file.close();
			Tools.statusMessage("end program");
		}
		catch(Exception e) {
			Tools.warningMessage("Could not open output file: " + filename);
		}
	}
}
