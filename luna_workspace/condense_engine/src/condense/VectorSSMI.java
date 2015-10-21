package condense;

/* SSMIVector
 * 
 * A vector for SSMI data.
 * 
 */

public class VectorSSMI extends GriddedVector {

	// Default constructor. Location only, no data.
	public VectorSSMI(GriddedLocation loc) {
		super(loc);

		data = NODATA;
	}

	// Constructor using a supplied value and location, and timestamp ID.
	public VectorSSMI( int i, GriddedLocation loc, int timeID ) {		
		super(i, loc, timeID);
	}
	
	
	public void print() {
		System.out.println( location.row() + "," + location.col() + " " + data() );  /// TBD needs work
	}

}
