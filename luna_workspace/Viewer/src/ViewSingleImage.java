
import condense.*;

/* ViewSingleImage
 * 
 * Show an image file. The image is assumed to be in a file,
 * containing an array of numbers.
 * 
 * This is intended to be a viewer to QA the data files.
 */

public class ViewSingleImage {

	public enum DataType {
		DOUBLE,
		FLOAT,
		INTEGER,
		BYTE,
		LONG,
		SHORT;
	}
	
	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();

		Tools.setDebug(false);
		
		//String inputPath = "C:/Users/glgr9602/Desktop/condense/data/avhrr/south/1990/001/";
		//String searchString = "albd";
		//DataType type = DataType.SHORT;

		String inputPath = "C:/Users/glgr9602/Desktop/condense/data/surface/";
		String searchString = "Nh_loci_land50_coast0km.1441x1441.bin";
		DataType type = DataType.BYTE;

		String outputPath = "C:/Users/glgr9602/Desktop/";
		
		
		String filename = Tools.findFile(inputPath, searchString);
		if (filename == null) {
			Tools.message("Could not find file with string: " + searchString);
			Tools.message("in path: " + inputPath);
			System.exit(1);
		}
		
		if (args != null) {
			if (args.length > 0) {
				Tools.message("VIEW SINGLE FILE. Input args: " + args);
				filename = args[0];
			}
		}

		// Surface data
		int rows = 1441;
		int cols = 1441;			

		// Southern hemisphere AVHRR
		//int rows = 1605;
		//int cols = 1605;			

		// Northern hemisphere AVHRR
		if (filename.indexOf("_n") > 0) {
			rows = 1805;
			cols = 1805;			
		}

		Tools.message(filename);
		Tools.message("");

		// Read the file
		int[][] data = readFile( filename, rows, cols, type);

		// Timing stats
		long endTime = System.currentTimeMillis();
		endTime = (endTime - startTime) / 1000;
		Tools.statusMessage("Total time to read = " + endTime + " seconds");
		
		// Display the image
		Tools.message("Creating image...");
		String imageFilename = outputPath + searchString + "_coast";
		DisplayImage(imageFilename, data);

		Tools.statusMessage("End program");
	}
	
	/* readFile
	 * 
	 *  Read the file, return the data.
	 */
	public static int[][] readFile( String filename, int rows, int cols, DataType type ) {

		DataFile file = null;
		int array[][] = new int[rows][cols];
		
		// Read the file
		try {
			file = new DataFile( filename );
		}
		catch(Exception e) {
			Tools.warningMessage("Failed to open file: " + filename );
			System.exit(1);
		}
				
		try {
			switch (type) {
				case DOUBLE:
					double[][] doubleArray = file.readDoubles2D(rows, cols);
					array = Tools.doubleArrayToInteger(doubleArray);
					break;
				case INTEGER:
					array = file.readInts2D(rows, cols);
					break;
				case SHORT:
					short shortArray[][] = file.readShorts2D(rows, cols);
					array = Tools.shortArrayToInteger(shortArray);
					break;
				case BYTE:
					byte byteArray[][] = file.readBytes2D(rows, cols);
					
					int newInts[][] = new int[rows][cols];
					for (int r = 0; r < rows; r++) {
						for (int c = 0; c < cols; c++) {
							newInts[r][c] = Tools.unsignedByteToInt(byteArray[r][c]);
						}
					}
					
					for (int r = 1; r < rows-1; r++) {
						for (int c = 1; c < cols-1; c++) {
							if (newInts[r][c] == 255 && newInts[r-1][c] != 255) array[r][c] = 252;
							if (newInts[r][c] == 255 && newInts[r+1][c] != 255) array[r][c] = 252;
							if (newInts[r][c] == 255 && newInts[r][c-1] != 255) array[r][c] = 252;
							if (newInts[r][c] == 255 && newInts[r][c+1] != 255) array[r][c] = 252;
						}
					}
					break;
				default:
					break;
			}
		}
			catch(Exception e) {
				Tools.warningMessage("Error on file read after " + rows + " and " + cols + " read.");
				System.exit(1);
		}

		file.close();
	
		return array;
	}
				
	
	static public void DisplayImage( String filename, int[][] array ) {
		
		int rows = array.length;
		int cols = array[0].length;
		
		// Create an image object
		Image image = new Image();
		
		// Create the color table
		ColorTable ct = new ColorTable();
		ct.grayScale();
		
		// Get rid of bad data points
		///array = Tools.discardBadData(array, 1900, 3100);
		
		/// DEBUG: look at a random selection of the data
		for (int i = 0; i < rows; i = i + 60) {
			System.out.print( array[i][i] + " ");
		}
		System.out.println();
		
		// Scale the data for display
		array = Tools.scaleIntArray2D(array, 0, 255);
		
		// Create a raster layer for the image, with the data
		RasterLayer layer = new RasterLayer( ct, array );
		image.addLayer(layer);
		
		// Display the image
		image.display(filename, rows, cols);
		
		// Save a png version
		image.savePNG(filename, rows, cols);
	}

}
