package helper_classes;

/* Metadata
 * 
 * A class holder for any metadata associated with the imagery we're processing.
 * 
 * Intended for use as a stored object in a database.
 */

public class Metadata {
	
	protected int rows = 0;
	protected int cols = 0;
	protected int timestamps = 0;
	protected int locations = 0;
	protected int vectors = 0;
	
	// Constructors
	public Metadata() {}
	
	public Metadata( int r, int c, int t, int l, int v) {
		rows = r;
		cols = c;
		timestamps = t;
		locations = l;
		vectors = v;
	}
	
	public int timestamps() { return timestamps; }
	public int vectors() { return vectors; }
	public int locations() { return locations; }
	public int rows() { return rows; }
	public int cols() { return cols; }
}
