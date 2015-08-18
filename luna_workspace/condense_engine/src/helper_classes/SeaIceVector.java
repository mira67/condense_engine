package helper_classes;

/* SeaIceVector
 * 
 * A vector for sea ice concentration data.
 * 
 */

public class SeaIceVector extends GriddedVector {
	
	public SeaIceVector(GriddedLocation loc) {		
		super(loc);
		
		// No sea ice by default.
		data = 0;
	}

	// Constructor using a supplied value and location.
	public SeaIceVector( int i, GriddedLocation loc ) {
		super(loc);

		this.data( i );

	}
	
	public void print() {
		System.out.println( data() );  /// TBD needs work
	}

}
