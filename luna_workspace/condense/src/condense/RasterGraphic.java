package condense;

/* RasterGraphic
 * 
 * Abstract class for a shape or point that can be drawn in a raster image.
 */
public abstract class RasterGraphic extends GeoObject {

	RasterLocation location;

    // Location methods
    public void row(int r) {location.row(r);}
    public int row() {return location.row();}
    
    public void col(int c) {location.col(c);}
    public int col() {return location.col();}
	  	  
    public void setLocation( int r, int c ) {
    	location.row(r);
    	location.col(c);
    }
    
	// Boolean operators for pixels
    public boolean isInSameLocationAs( Pixel pixel ) {
		if (this.row() == pixel.row() && this.col() == pixel.col() ) return true;
		return false;
	}
    
}
