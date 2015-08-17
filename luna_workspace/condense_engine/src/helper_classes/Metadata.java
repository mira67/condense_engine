package helper_classes;

/* Metadata
 * 
 * A class holder for any metadata associated with the imagery we're processing.
 * 
 * Intended for use as a stored object in a database.
 */

public class Metadata {

	// Id for the database table entry (there should be only one).
	protected int id = 0;
	
	protected int rows = 0;
	protected int cols = 0;
	protected int timestamps = 0;
	protected int locations = 0;
	protected int vectors = 0;
	
	// Constructors
	public Metadata() {}
}
