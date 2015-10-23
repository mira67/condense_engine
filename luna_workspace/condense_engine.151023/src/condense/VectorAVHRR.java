package condense;

/* AVHRRVector
 * 
 * A vector for AVHRR data.
 * 
 */

public class VectorAVHRR extends GriddedVector {

	// Default constructor. Location only, no data.
	public VectorAVHRR(GriddedLocation loc) {
		super(loc);

		data = NODATA;
	}

	// Constructor using a supplied value and location, and timestamp ID.
	public VectorAVHRR( int i, GriddedLocation loc, int timeID ) {		
		super(i, loc, timeID);
	}
	
	
	public void print() {
		// TODO: needs work
		System.out.println( location.row() + "," + location.col() + " " + data() );
	}

}
