
import condense.*;
import java.awt.Color;
import java.awt.Font;

/* Viewer
 * 
 * Show an image file. The image is assumed to be in a file,
 * containing an array of numbers.
 * 
 * This is intended to be a viewer to QA the climatology output files.
 */

public class Viewer {

	public enum DataType {
		DOUBLE,
		SHORT,
		FLOAT,
		INTEGER,
		BYTE,
		LONG;
	}
	
	public static void main(String[] args) {

		//String path = "C:/Users/glgr9602/Desktop/condense/climatology/ssmi/1990-2014/";
		String path = "C:/Users/glgr9602/Desktop/condense/climatology/avhrr/";
		String filenameRoot = "sd-temp";
		String filenameTail = "";
		
		DataType type = DataType.SHORT;
		
		String[] months = {"jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec"};
		String[] frequency = {"19", "22", "37", "85"};
		String[] polarization = {"h", "v"};
		String[] stat = {"mean", "sd"};

		String filename;
		String imageFilename;
		
		int rows = 0;
		int cols = 0;
		
		
						//filename = path + filenameRoot + frequency[f] + polarization[p] + "-" + stat[s] + "-" + months[m] + filenameTail;
						filename = Tools.findFile(path, filenameRoot);
						//filename = path + filenameRoot + frequency[f] + polarization[p] + "-" + stat[s] + "-" + months[m] + filenameTail;
						
						Tools.message(filename);

						// Determine rows and columns in the file...

						rows = 1605;
						cols = 1605;
						/*
						// Everything less than 85 GHz.
						rows = 332;
						cols = 316;

						// 85 or 91 GHz
						if ( f > 2 ) {
							rows = 664;
							cols = 632;							
						}
						*/
						
						// Read the file
						double[][] array = readFile( filename, rows, cols, type);
						
						//int numericalMonth = m + 1;
						
						// Display the image
						DisplayImage(filename, array, rows, cols);
			
		Tools.message("End program");
	}
	
	/* readFile
	 * 
	 *  Read the file, return the data.
	 */
	public static double[][] readFile( String filename, int rows, int cols, DataType type ){

		DataFile file;
		double array[][] = new double[rows][cols];
		
		// Read the file
		try {
			file = new DataFile( filename );
				
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
		}
		catch(Exception e) {
			Tools.warningMessage("Failed to open file: " + filename );
		}
	
		return array;
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
		
		// Add some verbage
		Annotation a = new Annotation("First annotation", 1000, 1000);
		a.setColor(new Color(0, 0, 255));
		a.setFont(new Font("PLAIN", 0, 15));
		image.addAnnotation(a);
		image.addAnnotation("My annotation", 500, 500);
		
		// Display the image
		image.display(filename, rows, cols);
		
		// Save a png version
		image.savePNG(filename, rows, cols);
	}

}
