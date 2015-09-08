package helper_classes;

import java.util.ArrayList;
import java.util.Iterator;

/* GriddedVector
 * 
 * Vector for storing sensor data, with time and a grid location.
 */

public class GriddedVector extends GeoObject {

	// Data is a 4-byte integer
	// - byte [3]: classification (256 possibilities, 0 - 255)
	protected int data = NODATA;
	
	protected int timestampID;
	protected GriddedLocation location;

	protected GriddedVector() {
	}

	public GriddedVector(GriddedLocation loc) {
		location = loc;
	}

	public GriddedVector(int value, GriddedLocation loc, int timeID) {
		data(value);
		location = loc;
		timestampID = timeID;
	}

	
	public int row() {return location.row();}
	public int col() {return location.col();}
	public double lat() {return location.lat();}
	public double lon() {return location.lon();}
	public boolean hasLatLon() {return location.hasLatLon;}
	public GriddedLocation location() {return location;}
	public int locationID() {return location.id;}
	
	/*
	 * classification
	 * 
	 * Set the classification. The input value must be between 0-255.
	 */
	public void classification(int i) {

		byte[] dataBytes = Tools.intToByteArray(data);
		dataBytes[3] = Tools.intToUnsignedByte(i);
		data = Tools.byteArrayToInt(dataBytes);
	}

	/*
	 * classification
	 * 
	 * Set the classification using an unsigned byte.
	 */
	public void classification(byte b) {

		byte[] dataBytes = Tools.intToByteArray(data);
		dataBytes[3] = b;
		data = Tools.byteArrayToInt(dataBytes);
	}

	/*
	 * classification
	 * 
	 * Get the classification as an integer.
	 */
	public int classification() {

		byte[] dataBytes = Tools.intToByteArray(data);
		return Tools.unsignedByteToInt(dataBytes[3]);
	}

	/*
	 * print
	 * 
	 * Print the vector information.
	 */
	public void print() {
		System.out.println("value = " + data +
				" locationID = " + location.id +
				" row = " + location.row() +
				" col = " + location.col() +
				" lat = " + location.lat() +
				" lon = " + location.lon() +
				" timestampID = " + timestampID );
	}

	// /public int byteData(int byte) { return data[byte]; }
	public int data() {
		return data;
	}

	public void data(int d) {
		data = d;
	}

	/*
	 * filterBadData
	 * 
	 * Given a 2-D array of vectors, replace any data that exceeds min/max
	 * thresholds with a 'no data' value.
	 */
	public static GriddedVector[][] filterBadData(GriddedVector array[][],
			int min, int max, int noData) {

		for (int r = 0; r < array.length; r++) {
			for (int c = 0; c < array[0].length; c++) {

				if (array[r][c] == null)
					continue;

				if (array[r][c].data() < min || array[r][c].data() > max)
					array[r][c].data(noData);
			}

		}

		return array;
	}

	/*
	 * createArrayFromVectorList
	 * 
	 * Given an array-list of vectors, create an integer array containing only
	 * the vectors' scalar data values at each row/col location.
	 */
	public static int[][] createArrayFromVectorList(int rows, int cols,
			ArrayList<GriddedVector> list) {
		int[][] array = new int[rows][cols];

		Iterator<GriddedVector> i = list.iterator();
		while (i.hasNext()) {
			GriddedVector v = i.next();

			if (v.data() != NODATA) {
				if (v.row() < rows && v.col() < cols) {
					array[v.row()][v.col()] = v.data();
				}
			}
		}

		return array;
	}

	/*
	 * createIntArrayFromVectorArray3d
	 * 
	 * Given a 3-D array of vectors, create an integer array containing only
	 * the vectors' scalar data values at each date/row/col location.
	 */
	public static int[][][] createIntArrayFromVectorArray3d(
			GriddedVector[][][] vectors) {

		int dates = vectors.length;
		int rows = vectors[0].length;
		int cols = vectors[0][0].length;
		
		int[][][] array = new int[dates][rows][cols];

		for (int i = 0; i < dates; i++) {
			for (int j = 0; j < rows; j++) {
				for (int k = 0; k < cols; k++) {
					array[i][j][k] = vectors[i][j][k].data();
				}
			}
		}

		return array;
	}
}
