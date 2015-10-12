package helper_classes;

/* SeaIceVector
 * 
 * A vector for sea ice concentration data.
 * 
 */

public class VectorSeaIce extends GriddedVector {
	
	public VectorSeaIce(GriddedLocation loc) {		
		super(loc);
		
		// No sea ice by default.
		data = 0;
	}

	// Constructor using a supplied value and location.
	public VectorSeaIce( int i, GriddedLocation loc, int timeID ) {
		super(i, loc, timeID);
	}
	
	public void print() {
		System.out.println( data() );  /// TBD needs work
	}

}
