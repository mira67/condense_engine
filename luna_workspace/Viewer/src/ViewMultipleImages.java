

import condense.*;

/* ViewMultipleImage
 * 
 * Show or store image files as png images.
 * 
 * This is intended to be a viewer to QA the data files.
 */

public class ViewMultipleImages {

	//static String[] channels = {"temp", "albd", "chn1", "chn2", "chn3", "chn4", "chn5"};
	static String[] channels = {"temp", "albd", "chn1", "chn2"};

	static String inputPath = "C:/Users/glgr9602/Desktop/condense/climatology/avhrr/south/";
	static String outputPath = "C:/Users/glgr9602/Desktop/condense/climatology/avhrr/";

	// Southern hemisphere AVHRR
	static int rows = 1605;
	static int cols = 1605;			

	// Northern hemisphere AVHRR
	//static int rows = 1805;
	//static int cols = 1805;
	
	
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
				
				String searchString = channel+"1400-sd-" + increment.name().toLowerCase();

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
					DisplayImage(imageFilename, data);
					
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
	
	static public void DisplayImage( String filename, int[][] array ) {
		
		int rows = array.length;
		int cols = array[0].length;
		
		// Create an image object
		Image image = new Image();
		
		// Create the color table
		ColorTable ct = new ColorTable();
		ct.grayScale();
		
		// Scale the data for display
		array = Tools.scaleIntArray2D(array, 0, 255);
		
		// Create a raster layer for the image, with the data
		RasterLayer layer = new RasterLayer( ct, array );
		image.addLayer(layer);
		
		// Display the image
		///image.display(filename, rows, cols);
		
		// Save a png version
		image.savePNG(filename, rows, cols);
	}
}
