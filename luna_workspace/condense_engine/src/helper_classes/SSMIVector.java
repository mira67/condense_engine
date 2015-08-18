package helper_classes;

/* SeaIceVector
 * 
 * A vector for sea ice data.
 * 
 */

public class SSMIVector extends GriddedVector {

	// Default constructor. Location only, no data.
	public SSMIVector(GriddedLocation loc) {
		super(loc);
		// The pixel is classified as unknown by default.
		data = 0;
	}

	// Constructor using a supplied value and location.
	public SSMIVector( int i, GriddedLocation loc ) {		
		super(loc);
		this.data( i );
	}
	
	
	public void print() {
		System.out.println( loc.row() + "," + loc.col() + " " + data() );  /// TBD needs work
	}

}
