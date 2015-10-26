
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

		String inputPath = "C:/Users/glgr9602/Desktop/condense/climatology/avhrr/";
		String searchString = "albd1400-mean-jan";
		DataType type = DataType.DOUBLE;

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
		
		// Southern hemisphere AVHRR
		int rows = 1605;
		int cols = 1605;			

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
		String imageFilename = outputPath + searchString;
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
					array = file.readInt2D(rows, cols);
					break;
				case SHORT:
					short shortArray[][] = file.readShort2D(rows, cols);
					array = Tools.shortArrayToInteger(shortArray);
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
		for (int i = 0; i < rows; i = i + 40) {
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
