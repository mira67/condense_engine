package condense;

/* Algorithms
 * 
 * Condensation algorithms
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
	 * minValue and maxValue are the minimum and maximum "reasonable" values for
	 * the data we're condensing. For instance, if the data set is temperature
	 * data (K), values below, say, 50K or above 400K are clearly errors--
	 * ignore that data. This will also take care of missing data that slips
	 * through the other filters, such as a pole hole.
	 * 
	 * minAnomalies is the number of adjacent anomalies required to declare any
	 * one pixel anomalous.
	 */

	public static short[][] algorithm1(short[][] data, double[][] mean,
			double[][] sd, double sdThreshold, short minAnomalies,
			short minValue, short maxValue) {

		// Find anomalies outside of the standard deviation threshold.
		short[][] anomalies = findAnomaliesBasedOnStats(data, minValue,
				maxValue, sdThreshold, mean, sd);

		// Filter the anomalies based on adjacency
		if (minAnomalies > 0)
			anomalies = findAnomaliesBasedOnAdjacency(anomalies, minAnomalies);

		// The first time through, print the criteria.
		if (printCriteria) {
			Tools.message("Algorithm1:");
			Tools.message("    Min value allowable: " + minValue);
			Tools.message("    Max value allowable: " + maxValue);
			Tools.message("    Standard deviations: " + sdThreshold);
			Tools.message("    Adjacent anomalies required: " + minAnomalies);

			// Turn off the printing.
			printCriteria = false;
		}

		return anomalies;
	}

	/*
	 * findAnomaliesBasedOnStats
	 * 
	 * Look in a data array for anomalies. An anomaly is identified when it is
	 * 1) in the valid range (minvalue - maxvalue), and 2) outside of
	 * (deviations * standard deviations) from the mean.
	 */
	public static short[][] findAnomaliesBasedOnStats(short[][] data,
			short minValue, short maxValue, double deviations, double[][] mean,
			double[][] sd) {

		int rows = data.length;
		int cols = data[0].length;

		double high;
		double low;

		short[][] anomalies = new short[rows][cols];

		// Iterate through the pixels, keeping only the data that
		// exceeds the threshold value (# of standard deviations)
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {

				// Default to "not an anomaly"
				anomalies[r][c] = NODATA;

				// Don't store data that is clearly bad.
				if (data[r][c] < minValue || data[r][c] > maxValue) {
					continue;
				}

				// Calculate a low and high threshold using the mean and
				// standard deviation
				low = mean[r][c] - deviations * sd[r][c];
				high = mean[r][c] + deviations * sd[r][c];

				// If the pixel value exceeds either threshold, keep it. It's an
				// anomaly.
				// TODO: should we offer different high and low thresholds?
				if (data[r][c] < low || data[r][c] > high) {
					anomalies[r][c] = data[r][c];
				}
			}
		}

		return anomalies;
	}

	/*
	 * findAnomaliesBasedOnAdjacency
	 * 
	 * Given an array of anomaly data, filter it: only keep an anomaly if it has
	 * 'minAnomalies' adjacent to it. minanomalies = 0 would keep everything.
	 */
	public static short[][] findAnomaliesBasedOnAdjacency(short data[][],
			int minAnomalies) {

		int rows = data.length;
		int cols = data[0].length;

		// Filter: If a pixel doesn't have enough adjacent anomalous pixels, it
		// might be noise -- ignore it.

		// A new place to put the newly filtered pixels
		short[][] anomalies = new short[rows][cols];

		// Go through every pixel location
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {

				// Default to "not an anomaly"
				anomalies[r][c] = NODATA;

				// Is there an anomalous pixel here? If not, don't bother.
				if (data[r][c] == NODATA)
					continue;

				// How many anomalous pixels are adjacent to this pixel?
				short adjacentAnomalies = 0;

				// Search the adjacent pixels, counting the anomalies

				search: for (int r1 = r - 1; r1 < r + 2; r1++) {
					for (int c1 = c - 1; c1 < c + 2; c1++) {

						// Don't compare the pixel with itself
						if (r1 == r && c1 == c)
							continue;

						// Don't exceed the array bounds.
						if (r1 < 0 || c1 < 0 || r1 >= rows || c1 >= cols)
							continue;

						// Found an adjacent anomaly? Count it.
						if (data[r1][c1] != NODATA) {
							adjacentAnomalies++;

							// Did we find enough adjacent anomalous pixels?
							if (adjacentAnomalies >= minAnomalies) {
								// Yup.
								anomalies[r][c] = data[r][c];

								break search;
							}
						}
					}
				} // End search
			}
		}

		return anomalies;
	}

}
