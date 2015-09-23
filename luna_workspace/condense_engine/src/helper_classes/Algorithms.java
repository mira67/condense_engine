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

		return condensedData;
	}
}
