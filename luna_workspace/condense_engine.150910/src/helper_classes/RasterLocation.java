package helper_classes;

/* RasterLocation
 * 
 * A row,column location for raster pixels. Coordinates are based on a
 * Cartesian coordinate system, but with the origin in the upper left
 * (positive row values are downward).
 */

public class RasterLocation extends Location {
	
	protected int row;
	protected int col;
  
	public RasterLocation(int r, int c) {
		row( r );
		col( c );
	}
	
	public int row() { return row; }
	public void row( int r ) { row = r; }

	public int col() { return col; }
	public void col( int c ) { col = c; }
	
	
	// isEqual - Do two locations match exactly?
	public boolean isEqual( RasterLocation that ) {
		if (this.row() == that.row() && this.col() == that.col() ) return true;
		return false;
	}


	public void print(){
		System.out.print(row() + "," + col());
  	}
}
