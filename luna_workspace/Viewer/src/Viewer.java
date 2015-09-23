
import helper_classes.*;

/* Viewer
 * 
 * Show an image file. The image is assumed to be in a file,
 * containing an array of double precision numbers.
 * 
 * This is intended to be a viewer to QA the climatology output files.
 */

public class Viewer {

	static String filename = "C:\\Users\\glgr9602\\Desktop\\condense\\climatology\\ssmi\\climate-ssmi19h-sd-jja-19900101.bin";
	
	public static void main(String[] args) {
	
		DataFile file;
		
		// Read the file
		try {
			file = new DataFile( filename );
			
			// Everything less than 85 GHz.
			int rows = 332;
			int cols = 316;

			// Anything more than 85 GHz
			//int rows = 664;
			//int cols = 632;
			
			double array[][] = new double[rows][cols];
			
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					array[r][c] = file.readDouble();
				}
			}

			file.close();
			
			DisplayImage(array, rows, cols);
		}
		catch(Exception e) {
			Tools.warningMessage("Failed to open file: " + filename );
		}

		Tools.message("End program");
	}
	
	static public void DisplayImage( double[][] array, int rows, int cols ) {
		
		// Create an image obect
		Image image = new Image();
		
		// Create the color table
		ColorTable ct = new ColorTable();
		ct.grayScale();
		
		// Convert the data to integers and scale it for display
		int[][] intArray = Tools.doubleArrayToInteger(array);
		intArray = Tools.scaleIntArray2D(intArray, 0, 255);
		
		// Create a raster layer for the image, with the data
		RasterLayer layer = new RasterLayer( ct, intArray );
		image.addLayer(layer);
		
		// Display the image
		image.display("19GHz horizontal, Feb 1990-2014 SD", rows, cols);
		
	}

}
