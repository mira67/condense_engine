package condense;

/* Algorithms
 * 
 * Static condensation algorithms
 * 
 */


public class Algorithms extends GeoObject {

	// The first time the algorithm is used, print out
	// what our criteria is -- logs the thresholds and
	// may eliminate confusion later.
	static boolean printCriteria = true;

	/*
	 * algorithm1
	 * 
	 * Use the climatological mean and standard deviation to condense data.
	 * 
	 * sdThreshold is the number of standard deviations to use for selecting
	 * anomalous pixels.
	 * 
	 * minValue and maxValue are the minimum and maximum "reasonable" values
	 * for the data we're condensing. For instance, if the data set is
	 * temperature data (K), values below, say, 50K or above 400K are clearly
	 * errors-- ignore that data. This will also take care of missing data that
	 * slips through the other filters, such as a pole hole.  
	 */

	public static short[][] algorithm1(
			short[][] data,
			double[][] mean,
			double[][] sd,
			double sdThreshold,
			short minValue,
			short maxValue ) {
		
		int rows = data.length;
		int cols = data[0].length;

		// Number of adjacent anomalous pixels necessary for deciding whether
		// any one pixel is a keeper?
		final int minAnomalies = 2;
		
		// A place to store the anomalous pixels
		short[][] condensedData = new short[ rows ][ cols ];

		// Threshold pixel values
		double low;
		double high;

		// PART 1...
		// Keep only the pixels that exceed the threshold value (# of standard deviations)
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {

				// Default to "not an anomaly"
				condensedData[r][c] = NODATA;

				// Don't store data that is clearly bad.
				if (data[r][c] < minValue || data[r][c] > maxValue) {
					continue;
				}
				
				// Calculate a low and high threshold using the mean and standard deviation
				low = mean[r][c] - sdThreshold * sd[r][c];
				high = mean[r][c] + sdThreshold * sd[r][c];
				
				// If the pixel value exceeds either threshold, keep it. It's an anomaly.
				// TODO: should we offer different high and low thresholds?
				if (data[r][c] < low || data[r][c] > high) {
					condensedData[r][c] = data[r][c];
				}
			}
		}

		// PART 2...
		// Filter: If a pixel doesn't have enough adjacent anomalous pixels, it
		// might be noise -- ignore it.
		
		// A new place to put the newly filtered pixels
		short[][] anomalies = new short[ rows ][ cols ];

		// Go through every pixel location
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				
				// Is there an anomalous pixel here? If not, don't bother.
				if (condensedData[r][c] == NODATA) continue;

				// How many anomalous pixels are adjacent to this pixel?
				int adjacentAnomalies = 0;
				
				// Look at adjacent pixels, counting the anomalies
				for (int r1 = r-1; r1 < r+2; r1++) {
					for (int c1 = c-1; c1 < c+2; c1++) {

						// Default to "not an anomaly"
						anomalies[r][c] = NODATA;
						
						// Don't compare the pixel with itself
						if (r1 == r && c1 == c) continue;
					
						// Don't exceed the array bounds.
						if (r1 < 0 || c1 < 0 || r1 > rows-1 || c1 > cols-1) continue;
						
						// Found an adjacent anomaly? Count it.
						if (condensedData[r1][c1] != NODATA) adjacentAnomalies++;
					}

					// Did we find enough adjacent anomalous pixels?
					if (adjacentAnomalies >= minAnomalies) {
						// Yup.
						anomalies[r][c] = condensedData[r][c];
						
						break;
					}
				}
			}
		}
		
		// The first time through, print the criteria.
		if (printCriteria) {
			Tools.message("Algorithm1:");
			Tools.message("    Min value allowable: " + minValue);
			Tools.message("    Max value allowable: " + maxValue);
			Tools.message("    SD threshold: " + sdThreshold);
			Tools.message("    Adjacent anomalies required: " + minAnomalies);
			
			// Turn off the printing.
			printCriteria = false;
		}
		
		return anomalies;
	}
}
