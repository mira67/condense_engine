
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
		
		//String databaseName = "avhrrSouthTemp";
		String databaseName = "ssmi37h19902014a2N";
		//String databasePath = "jdbc:h2:tcp://localhost/~/Desktop/condense/databases/avhrr/2anomaly/south/";
		String databasePath = "jdbc:h2:tcp://localhost/~/Desktop/condense/databases/ssmi/2anomaly/north/";
		String locationsPath = "C:/Users/glgr9602/Desktop/condense/data/ssmi/locations/other/";
		String outputPath = "C:/Users/glgr9602/Desktop/query6ssmi/";
		String filename = databaseName + "_query6";
		
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
		
		// AVHRR
		// To speed thing up; locations query taking 45 minutes...
		//GriddedLocation[][] locs = DatasetAVHRR.getLocations(locationsPath, "south");
		
		ArrayList<GriddedLocation> locations = new ArrayList<GriddedLocation>();
		
		// currently, arraylists index from zero. the database indexes locations from 1.
		// the database will index from 1 starting with the next version. until then...
		// add a bogus location to the arraylist to keep the indexing the same.
		locations.add(new GriddedLocation(0,0,0,0));

		/*
		rows = 1605;
		cols = 1605;
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++ ) {
				locations.add(new GriddedLocation(r,c, 0, 0));
			}
		}
		*/
		
		// SSMI
		rows = DatasetSSMI.rows("north", "37");
		cols = DatasetSSMI.cols("north", "37");
		GriddedLocation[][] locs = DatasetSSMI.getLocations(locationsPath, "north", "37");
		
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++ ) {
				locations.add(locs[r][c]);
			}
		}

		ArrayList<GriddedVector> pixList = null;

		int threshold = 2730;

		//String query = "SELECT * FROM " + databaseName + ".VECTORS WHERE VALUE <= " + threshold + " AND VALUE >= 1900"; 
		String query = "SELECT * FROM timestamps a, vectors b where " + 
				"a.id=b.timestampid and a.month >= 6 and a.month <= 8 and a.year = 2012"; 

		//select b.value from timestamps a, vectors b, locations c where 
		//		a.id=b.timestampid and 
		//		c.id=b.locationid and
		//		c.id=140 and
		//		a.timestamp between 9131 and 9162

		// Vectors
		Tools.message("Start vector query: " + query);
		startTime = System.currentTimeMillis();
		pixList = db.queryVectors(query, locations);
		
	
		endTime = System.currentTimeMillis();
		endTime = (endTime - startTime) / 1000;
		Tools.statusMessage("  Total time for query = " + endTime + " seconds\n");
			
		int size = pixList.size();
		Tools.statusMessage("Pixels: " + size);

		// Create a file that contains the number of anomalies at each locations
		short[][] population = new short[rows][cols];
		
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
				
				// Add to the population array
				population[loc.row()][loc.col()]++;
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
				threshold, sensorData, 15);

		// Scale the population data so that it goes from 0 - 255
		population = Tools.scaleShortArray2D(population, 0, 255);

		// Display and save the population image
		DisplayImage( outputPath, filename+"pop", databaseName+"_pop", query,
				threshold, population, 15);
			
		db.disconnect();
		
		Tools.message("End program");
	}

				
	/* Display an image from the data
	 * 
	 * If unsure of the font size, use 25
	 */
	static public void DisplayImage( String path, String filename, String dbname, String query,
			int threshold, short[][] shortArray, int fontSize) {

		Tools.statusMessage("\nGENERATE IMAGE");
		
		int rows = shortArray.length;
		int cols = shortArray[0].length;
		
		// Create an image object
		Image image = new Image();
		image.setBackground(new Color(0,0,0));	// Black background
		
		// Create the color table
		ColorTable ct = new ColorTable();
		//ct.prism();
		//ct.temperature();
		ct.grayScale();
		
		int[][] array = Tools.shortArrayToInteger(shortArray);
		
		// Create a raster layer for the image, with the data
		RasterLayer layer = new RasterLayer( ct, array );
		image.addLayer(layer);

		// Add a color bar.
		//int height = 40;
		//int width = 400;
		int height = 15;
		int width = 200;
				RasterColorBar bar = new RasterColorBar(rows-height-1, 10, width, height, ct);
				bar.outline(2, new Color(255,255,255));
				RasterLayer colorBarLayer = new RasterLayer(bar.getPixels());
				colorBarLayer.name("Color Bar");
				image.addLayer(colorBarLayer);

		// Add some verbage
		Annotation a1 = new Annotation(filename + "_" + threshold, 20, 70);
		a1.setFont(new Font("PLAIN", 0, fontSize));
		a1.setColor(new Color(255, 255, 0));		// Yellow
		Annotation a3 = new Annotation(query, 20, 30);
		a3.setFont(new Font("PLAIN", 0, fontSize));
		a3.setColor(new Color(0, 255, 255));		// blue-green
		image.addAnnotation(a1);
		image.addAnnotation(a3);
		
		// Display the image
		image.display(filename + "  Threshold = " + threshold, rows, cols);
		
		// Save a png version
		image.savePNG(path + filename + "_" + threshold, rows, cols);
	}
}
