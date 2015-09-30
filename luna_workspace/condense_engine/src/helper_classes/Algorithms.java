package helper_classes;

/* Algorithms
 * 
 * Static condensation algorithms
 * 
 */

public class Algorithms extends GeoObject {

	/*
	 * algorithm1
	 * 
	 * Example algorithm.
	 * 
	 * Use the climatological mean and standard deviation to condense data.
	 * 
	 * sdThreshold is the number of standard deviations to use for selecting
	 * anomalous pixels.
	 */

	public static GriddedVector[][] algorithm1(
			GriddedVector[][] data,
			double[][] mean,
			double[][] sd,
			double sdThreshold) {
		
		int rows = data.length;
		int cols = data[0].length;
		
		GriddedVector[][] condensedData = new GriddedVector[ rows ][ cols ];

		double low;
		double high;

		// PART 1...
		// Keep only the pixels that exceed the threshold value (# of standard deviations)
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				
				// Calculate a low and high threshold using the mean and standard deviation
				low = mean[r][c] - sdThreshold * sd[r][c];
				high = mean[r][c] + sdThreshold * sd[r][c];
				
				// If the pixel value exceeds either threshold, keep it. It's an anomaly.
				// TODO: should we offer different high and low thresholds?
				if (data[r][c].data() < low || data[r][c].data() > high) {
					condensedData[r][c] = data[r][c];
				}
			}
		}

		// PART 2...
		// Filter: If a pixel doesn't have enough adjacent anomalous pixels, it
		// might be noise -- ignore it.
		
		// Set up a new place to put the filtered pixels
		GriddedVector[][] anomalies = new GriddedVector[ rows ][ cols ];

		// Number of adjacent anomalous pixels necessary for deciding whether
		// any one pixel is a keeper?
		int minAnomalies = 1;

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				
				// Is this an anomalous pixel? If not, don't bother.
				if (condensedData[r][c] == null) continue;

				// How many anomalous pixels are adjacent to this pixel?
				int adjacentAnomalies = 0;
				
				// Look at adjacent pixels, counting the anomalies
				for (int r1 = r-1; r1 < r+2; r1++) {
					for (int c1 = c-1; c1 < c+2; c1++) {
						
						// Don't compare the pixel with itself
						if (r1 == r && c1 == c) continue;
					
						// Don't exceed the array bounds.
						if (r1 < 0 || c1 < 0 || r1 > rows-1 || c1 > cols-1) continue;
						
						// Found one. Count it.
						if (condensedData[r1][c1] != null) adjacentAnomalies++;
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
		
		return anomalies;
	}
}
