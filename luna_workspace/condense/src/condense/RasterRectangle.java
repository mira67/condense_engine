package condense;

/* RasterRectangle
 * 
 * Like a square, only rectangular.
 */

public class RasterRectangle extends RasterGraphic {

	int width = 0;
	int height = 0;
	
	RasterRectangle( int r, int c, int w, int h ) {
		location = new RasterLocation( r, c );
		width = w;
		height = h;
	}

	public void width(int w) { width = w; }
	public void height(int h) { height = h; }

	public int width() { return width; }
	public int height() { return height; }

	public void row(int r) { location.row(r); }
	public void col(int c) { location.col(c); }
	
	public int row() { return location.row(); }
	public int col() { return location.col(); }
}
