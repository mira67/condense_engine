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
	
	public int timestamps() { return rows; }
	public int vectors() { return rows; }
	public int locations() { return rows; }
	public int rows() { return rows; }
	public int cols() { return cols; }
}
