
import helper_classes.*;

/* Viewer
 * 
 * Show an image file. The image is assumed to be in a file,
 * containing an array of numbers.
 * 
 * This is intended to be a viewer to QA the climatology output files.
 */

public class Viewer {

	///static String filename = "C:\\Users\\glgr9602\\Desktop\\condense\\climatology\\ssmi\\climate-ssmi19h-sd-jja-19900101.bin";
	static String path = "C:\\Users\\glgr9602\\Desktop\\condense\\data\\surface\\";
	static String filename = "surface-land-1441.bin";

	public enum DataType {
		DOUBLE,
		FLOAT,
		INTEGER,
		BYTE,
		LONG;
	}
	
	static final DataType type = DataType.DOUBLE;
	
	public static void main(String[] args) {
	
		DataFile file;
		
		// Read the file
		try {
			file = new DataFile( path+filename );

			// Surface, high res
			int rows = 1441;
			int cols = 1441;

			// Everything less than 85 GHz.
			///int rows = 332;
			///int cols = 316;

			// Anything more than 85 GHz
			//int rows = 664;
			//int cols = 632;
			
			double array[][] = new double[rows][cols];
			
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					
					switch (type) {
						case DOUBLE:
							array[r][c] = file.readDouble();
							break;
						case INTEGER:
							array[r][c] = (double) file.readInt();
							break;
						default:
							break;
					}
				}
			}

			file.close();
			
			DisplayImage(filename, array, rows, cols);
		}
		catch(Exception e) {
			Tools.warningMessage("Failed to open file: " + filename );
		}

		Tools.message("End program");
	}
	
	static public void DisplayImage( String filename, double[][] array, int rows, int cols ) {
		
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
		image.display(filename, rows, cols);
		
	}

}