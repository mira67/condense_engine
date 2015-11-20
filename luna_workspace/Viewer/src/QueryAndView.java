
import condense.*;

import java.awt.Color;
import java.awt.Font;
//import java.awt.Color;
//import java.awt.Font;
import java.util.ArrayList;
//import java.util.Iterator;

/* QueryAndView
 * 
 * Query a database for data, display an image.
 * Intended for generating timing metrics.
 */

public class QueryAndView {

	public static void main(String[] args) {

		Tools.setDebug(false);
		
		String databaseName = "avhrrSouthTemp";
		String databasePath = "jdbc:h2:tcp://localhost/~/Desktop/condense/databases/avhrr/south1000-3500range/";
		//String locationsPath = "";
		String outputPath = "C:/Users/glgr9602/Desktop/query3/";
		String filename = databaseName + "_query3";
		
		Tools.message("Connecting to database: " + databasePath+databaseName);
		
		DatabaseH2 db = new DatabaseH2(databasePath, databaseName);
		db.connectReadOnly();
		db.status();

		Tools.message("Connected");
		
		// Get the metadata
		Metadata metadata = db.getMetadata();

		int rows = metadata.rows();
		int cols = metadata.cols();
		
		// Timestamps
		Tools.message("Querying for timestamps");
		ArrayList<Timestamp> timestamps = db.getTimestamps();
		
		long startTime;
		long endTime;
		
		// Locations
		Tools.message("Querying for locations");
		/*
		startTime = System.currentTimeMillis();
		ArrayList<GriddedLocation> locations = db.getLocations();
		endTime = System.currentTimeMillis();
		endTime = (endTime - startTime) / 1000;
		Tools.statusMessage("  Total time for query = " + endTime + " seconds\n");
		*/
		
		// To speed thing up; locations query taking 45 minutes...
		//GriddedLocation[][] locs = DatasetAVHRR.getLocations(locationsPath, "south");
		ArrayList<GriddedLocation> locations = new ArrayList<GriddedLocation>();
		rows = 1605;
		cols = 1605;
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++ ) {
				locations.add(new GriddedLocation(r,c, 0, 0));
			}
		}
		
		// Iterate through some thresholds
		for (int j = 0; j < 1; j++) {

			ArrayList<GriddedVector> pixList = null;

			int threshold = 1810 + j * 10;
			
			String query = "SELECT * FROM " + databaseName + ".VECTORS WHERE VALUE <= " + threshold + " AND VALUE >= 1790"; 

			// Vectors
			Tools.message("Start vector query: " + query);
			startTime = System.currentTimeMillis();
			pixList = db.queryVectors(query, locations);
			
/* For testing
pixList = new ArrayList<GriddedVector>();
pixList.add(new GriddedVector((short) 500, new GriddedLocation(200, 200, 0, 0), 1)); 
pixList.add(new GriddedVector((short) 600, new GriddedLocation(200, 202, 0, 0), 1)); 
pixList.add(new GriddedVector((short) 700, new GriddedLocation(200, 204, 0, 0), 1)); 
pixList.add(new GriddedVector((short) 800, new GriddedLocation(200, 206, 0, 0), 1));
*/
			
			endTime = System.currentTimeMillis();
			endTime = (endTime - startTime) / 1000;
			Tools.statusMessage("  Total time for query = " + endTime + " seconds\n");
			
			int size = pixList.size();
			Tools.statusMessage("Pixels: " + size);

			// Write out the data to a text file. Max 1000 pixels
			///if (size > 1000) size = 1000;

			DataFile out1 = null;
			
			try {
				out1 = new DataFile();
				out1.create(outputPath + filename + "_" + threshold +".txt");
				out1.writeString(databaseName+", " + query, true);
				out1.writeString("Pixels = " + size, true);
				out1.writeString("Vector ID, value, row, col, timestampID, date", true);
			
				for (int i = 0; i < size; i++) {
					GriddedVector vec = pixList.get(i);
					GriddedLocation loc = vec.location();
					Timestamp ts = timestamps.get(vec.timestampID());
					
					String line = i + ", " + vec.data() + ", " + loc.row() +
							", " + loc.col() + ", " + vec.timestampID() + ", " +
							ts.dateString();
					
					out1.writeString(line, true);
				}

				out1.close();
			}
			catch (Exception e) {
				Tools.warningMessage("Failed to write data file, " + filename  + "_" + threshold + ".txt");
				Tools.warningMessage("Exception: " + e);
			}
			
			// Make an integer array out of the pixel data
			short[][] sensorData = GriddedVector.createArrayFromVectorList(rows,
					cols, pixList);

			// Scale the data so that it goes from 0 - 255
			sensorData = Tools.scaleShortArray2D(sensorData, 0, 255);

			// Display and save the image
			DisplayImage( outputPath, filename, databaseName, query,
					threshold, sensorData);
			
		}
		
		db.disconnect();
		
		Tools.message("End program");
	}

				
	/* Display an image from the data
	 * 
	 */
	static public void DisplayImage( String path, String filename, String dbname, String query,
			int threshold, short[][] shortArray) {

		Tools.statusMessage("\nGENERATE IMAGE");
		
		int rows = shortArray.length;
		int cols = shortArray[0].length;
		
		// Create an image object
		Image image = new Image();
		image.setBackground(new Color(0,0,0));	// Black background
		
		// Create the color table
		ColorTable ct = new ColorTable();
		//ct.prism();
		ct.temperature();
		//ct.grayScale();
		
		int[][] array = Tools.shortArrayToInteger(shortArray);
		
		// Create a raster layer for the image, with the data
		RasterLayer layer = new RasterLayer( ct, array );
		image.addLayer(layer);

		// Add a color bar.
				int height = 40;
				int width = 400;
				RasterColorBar bar = new RasterColorBar(rows-height-5, 10, width, height, ct);
				bar.outline(2, new Color(255,255,255));
				RasterLayer colorBarLayer = new RasterLayer(bar.getPixels());
				colorBarLayer.name("Color Bar");
				image.addLayer(colorBarLayer);

		// Add some verbage
		Annotation a1 = new Annotation(filename + "_" + threshold, 20, 70);
		a1.setFont(new Font("PLAIN", 0, 25));
		a1.setColor(new Color(255, 255, 0));		// Yellow
		Annotation a3 = new Annotation(query, 20, 30);
		a3.setFont(new Font("PLAIN", 0, 25));
		a3.setColor(new Color(0, 255, 255));		// blue-green
		image.addAnnotation(a1);
		image.addAnnotation(a3);
		
		// Display the image
		image.display(filename + "  Threshold = " + threshold, rows, cols);
		
		// Save a png version
		image.savePNG(path + filename + "_" + threshold, rows, cols);
	}
}
