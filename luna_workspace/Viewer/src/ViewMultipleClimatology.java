

import condense.*;

/* ViewMultipleClimatology
 * 
 * Show or store image files as png images.
 * 
 * This is intended to be a viewer to QA the data files.
 */

public class ViewMultipleClimatology {

	//static String[] channels = {"19h", "19v", "22v", "37h", "37v"};
	static String[] channels = {"85h", "85v"};
	//static String[] channels = {"85v"};
	//static String[] channels = {"temp", "albd", "chn1", "chn2", "chn3", "chn4", "chn5"};
	//static String[] channels = {"temp", "albd", "chn1", "chn2"};

	static String inputPath = "C:/Users/glgr9602/Desktop/condense/climatology/ssmi/south/shorts/";
	static String outputPath = "C:/Users/glgr9602/Desktop/condense/climatology/ssmi/south/images/";

	// Southern hemisphere AVHRR
	//static int rows = 1605;
	//static int cols = 1605;			

	// Northern hemisphere AVHRR
	//static int rows = 1805;
	//static int cols = 1805;

	// Northern hemisphere SSMI, no 85GHz
	//static int rows = 448;
	//static int cols = 304;

	// Northern hemisphere SSMI, 85GHz
	//static int rows = 896;
	//static int cols = 608;

	// Southern hemisphere SSMI, no 85GHz
	//static int rows = 332;
	//static int cols = 316;
	
	// Southern hemisphere SSMI, no 85GHz
	static int rows = 664;
	static int cols = 632;
	
	
	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();

		Tools.setDebug(false);

		// Loop through all possible increment types.
		for (Timespan.Increment increment : Timespan.Increment.values()) {

			// Skip over unwanted increments, i.e., only process in the
			// desired increments.
			String name = increment.name().toLowerCase();
			
			if (name.compareTo("none") == 0  ||
				name.compareTo("day") == 0  ||
				name.compareTo("year") == 0  ||
				name.compareTo("week") == 0
			) continue;

    		
			// Loop through all wavelengths
			for ( String channel : channels) {
				
				// AVHRR
				//String searchString = channel+"1400-mean-" + increment.name().toLowerCase();
				
				// SSMI
				String searchString = channel+"-sd-" + increment.name().toLowerCase();
				int lowBad = 1;
				int highBad = 500;
				
				String filename = Tools.findFile(inputPath, searchString);

				if (filename == null) continue;
				
				Tools.message(filename);

				try {
					DataFile file = new DataFile(filename);
					
					// Read the file
					short[][] shortData = file.readShorts2D(rows, cols );
					int[][] data = Tools.shortArrayToInteger(shortData);
					
					// Display the image
					//Tools.message("Creating image...");
					String imageFilename = outputPath + searchString;
					createImage(imageFilename, data, lowBad, highBad);
					
					file.close();
				}
				catch(Exception e) {}
			}
		}

		// Timing stats
		long endTime = System.currentTimeMillis();
		endTime = (endTime - startTime) / 1000;
		Tools.statusMessage("Total time to read = " + endTime + " seconds");
		
		Tools.statusMessage("End program");
	}
	
	static public void createImage( String filename, int[][] array, int lowBad, int highBad ) {
		
		int rows = array.length;
		int cols = array[0].length;
		
		// Create an image object
		Image image = new Image();
		
		// Create the color table
		ColorTable ct = new ColorTable();
		ct.grayScale();
		
		// Scale the data for display
		array = Tools.scaleIntArray2DExcludeBad(array, 0, 255, lowBad, highBad);
		
		// Create a raster layer for the image, with the data
		RasterLayer layer = new RasterLayer( ct, array );
		image.addLayer(layer);
		
		// Display the image
		///image.display(filename, rows, cols);
		
		// Save a png version
		image.savePNG(filename, rows, cols);
	}
}
