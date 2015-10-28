

import condense.*;

/* ViewMultipleAVHRR
 * 
 * Show or store image files as png images.
 * 
 * This is intended to be a viewer to QA the data files.
 */

public class ViewMultipleAVHRR {

	//static String[] channels = {"temp", "albd", "chn1", "chn2", "chn3", "chn4", "chn5"};
	static String[] channels = {"temp", "albd", "chn1", "chn2"};

	static String inputPath = "C:/Users/glgr9602/Desktop/condense/data/avhrr/south/";
	static String outputPath = "C:/Users/glgr9602/Desktop/condense/data/avhrr/south/images/";

	// Southern hemisphere AVHRR
	static int rows = 1605;
	static int cols = 1605;

	// Northern hemisphere AVHRR
	//static int rows = 1805;
	//static int cols = 1805;
	
	static int startYear = 1990;
	static int endYear = 1991;
	
	public static void main(String[] args) {

		Tools.setDebug(false);
		Tools.setStatus(false);
		Tools.setWarnings(false);
		
		long startTime = System.currentTimeMillis();

		Timestamp date;
		short data[][];
		
		// Loop through all channels
		for (String channel : channels) {

			// Loop through all possible days
			for (int year = startYear; year <= endYear; year++) {
				
				Timestamp lastDayOfYear = new Timestamp( year, 12, 31);
				
				for (int day = 1; day <= lastDayOfYear.dayOfYear(); day++) {

					date = new Timestamp(year, day);
					
					data = DatasetAVHRR.readData(date, rows, cols, inputPath, true, true, channel, "1400");
					
					if (data != null) {
						String imageFilename = outputPath + date.dateString() + "." + channel;
						
						// Make the image. Change the valid range based on the channel.
						if (channel.equalsIgnoreCase("temp") ||
								channel.equalsIgnoreCase("chn3") ||
								channel.equalsIgnoreCase("chn4") ||
								channel.equalsIgnoreCase("chn5")) {
							createImage(imageFilename, data, 1900, 3100);
						}
						else {
							createImage(imageFilename, data, 0, 1000);							
						}
					}
				}
			}			
		}
    		
		// Timing stats
		long endTime = System.currentTimeMillis();
		endTime = (endTime - startTime) / 1000;
		Tools.statusMessage("Total time to read = " + endTime + " seconds");
		
		Tools.statusMessage("End program");
	}
	
	static public void createImage( String filename, short[][] data, int min, int max ) {
		
		
		int rows = data.length;
		int cols = data[0].length;
		
		// Create an image object
		Image image = new Image();
		
		// Create the color table
		ColorTable ct = new ColorTable();
		ct.grayScale();
		
		// Scale the data for display
		int[][] array = Tools.shortArrayToInteger(data);
		
		// Print out some random locations in the file
		/*
		Tools.message("  " + array[ Tools.randomInt(rows)][Tools.randomInt(cols)] +
				"  " + array[ Tools.randomInt(rows)][Tools.randomInt(cols)] +
				"  " + array[ Tools.randomInt(rows)][Tools.randomInt(cols)] +
				"  " + array[ Tools.randomInt(rows)][Tools.randomInt(cols)] +
				"  " + array[ Tools.randomInt(rows)][Tools.randomInt(cols)] );
		*/
		
		array = Tools.discardBadData(array, min, max);
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
