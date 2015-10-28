package condense;

/* Pixel
 * 
 * Abstract pixel class, a single element in a raster image.
 * 
 */

public abstract class Pixel extends RasterGraphic {

    public Pixel(int r, int c) {
    	location = new RasterLocation(r, c);
    }
}
