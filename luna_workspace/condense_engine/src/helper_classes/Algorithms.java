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
	 * Use the climatological mean and standard deviation to condense data.
	 * 
	 */

	public static GriddedVector[][] algorithm1(
			GriddedVector[][] data,
			double[][] mean,
			double[][] sd,
			double sdThreshold) {
		
		GriddedVector[][] condensedData = new GriddedVector[ data.length ][ data[0].length ];

		double low;
		double high;
		
		// Keep only the pixels that exceed the threshold value.
		for (int r = 0; r < data.length; r++) {
			for (int c = 0; c < data[0].length; c++) {
				
				// Calculate a low and high threshold using the mean and standard deviation
				low = mean[r][c] - sdThreshold * sd[r][c];
				high = mean[r][c] + sdThreshold * sd[r][c];
				
				// If the vector exceeds either threshold, keep it. Otherwise discard it.
				// TODO: should we enable differing high and low thresholds?
				if (data[r][c].data() < low || data[r][c].data() > high) condensedData[r][c] = data[r][c];
			}
		}
		return condensedData;
	}
}
