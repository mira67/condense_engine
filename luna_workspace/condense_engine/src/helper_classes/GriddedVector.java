package helper_classes;

/* GriddedVector
 * 
 * Vector for storing sensor data, with time and a grid location.
 */

public class GriddedVector extends GeoObject {

	// Data is a 4-byte integer
	//   - byte [3]: classification (256 possibilities, 0 - 255)
	protected int data = NODATA;
	private int time = 0; // Time: days since epoch*1000

	// TODO: maybe an index into the locations, rather than the actual location?
	protected GriddedLocation loc;

	protected GriddedVector() {}

	public GriddedVector(int r, int c) {
		loc = new GriddedLocation(r, c);
	}

	public GriddedVector(int value, int r, int c) {
		loc = new GriddedLocation(r, c);
		data(value);
	}

	public GriddedVector(int value, int r, int c, double t) {
		loc = new GriddedLocation(r, c);
		data(value);
		time(t);
	}

	public GriddedVector(int r, int c, double lat, double lon) {
		loc = new GriddedLocation(r, c, lat, lon);
	}

	// Time methods
	public void time(double t) {
		time = (int) t * 1000;
	}

	public double time() {
		return time / 1000.0;
	}

	public int encodedTime() {
		return time;
	}

	public int row() {
		return loc.row();
	}

	public int col() {
		return loc.col();
	}

	public double lat() {
		return loc.lat();
	}

	public double lon() {
		return loc.lon();
	}

	public boolean hasLatLon() {
		return loc.hasLatLon;
	}

	public GriddedLocation location() {
		return loc;
	}

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
		// TODO
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
	 public static GriddedVector[][] filterBadData( GriddedVector array[][], int min, int max, int noData) {
		 
		 for (int r = 0; r < array.length; r++){
			 for (int c = 0; c < array[0].length; c++){
				 
				 if (array[r][c] == null) continue;
				 
				 if (array[r][c].data() < min || array[r][c].data() > max)
					 array[r][c].data(noData);
			 }
			 
		 }
		 
		 return array;
	 }
}