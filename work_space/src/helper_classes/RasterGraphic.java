package helper_classes;

/* RasterGraphic
 * 
 * Abstract class for a shape or point that can be drawn in a raster image.
 */
public abstract class RasterGraphic extends GeoObject {

	RasterLocation origin;

    // Location methods
    public void row(int r) {origin.row(r);}
    public int row() {return origin.row();}
    
    public void col(int c) {origin.col(c);}
    public int col() {return origin.col();}
	  	  
    public void setLocation( int r, int c ) {
    	origin.row(r);
    	origin.col(c);
    }
    
	// Boolean operators for pixels
    public boolean isInSameLocationAs( Pixel pixel ) {
		if (this.row() == pixel.row() && this.col() == pixel.col() ) return true;
		return false;
	}
    
}
