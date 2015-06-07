package helper_classes;

/* Metadata
 * 
 * A class holder for any metadata associated with the imagery we're processing.
 * 
 * Intended for use as a stored object in a database.
 */

public class Metadata {

	private int rows;
	private int cols;
	protected int timestamps = 0;
	protected int locations = 0;
	protected int vectors = 0;
	
	// Constructors
	public Metadata() {}

	public int rows() { return rows; }
	public void rows( int r ) {rows = r;}

	public int cols() { return cols; }
	public void cols( int c ) { cols = c; }
}