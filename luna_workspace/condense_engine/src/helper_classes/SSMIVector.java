package helper_classes;

/* SeaIceVector
 * 
 * A vector for sea ice data.
 * 
 */

public class SSMIVector extends GriddedVector {

	// Default constructor. Location only, no data.
	public SSMIVector(int r, int c) {
		super(r,c);
		// The pixel is classified as unknown by default.
		data = 0;
	}

	// Constructor using a supplied value and location.
	public SSMIVector( int i, int r, int c ) {		
		super(r,c);
		this.data( i );
	}
	
	
	public void print() {
		System.out.println( loc.row() + "," + loc.col() + " " + data() );  /// TBD needs work
	}

}
