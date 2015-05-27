package helper_classes;

/* SeaIceVector
 * 
 * A vector for sea ice concentration data.
 * 
 */

public class SeaIceVector extends GriddedVector {
	
	public SeaIceVector(int r, int c) {		
		super(r,c);
		
		// No sea ice by default.
		data = 0;
	}

	// Constructor using a supplied value and location.
	public SeaIceVector( int i, int r, int c ) {
		super(r,c);

		this.data( i );

	}
	
	public void print() {
		System.out.println( loc.row() + "," + loc.col() + " " + data() );  /// TBD needs work
	}

}
