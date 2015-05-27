package helper_classes;

/* Pixel
 * 
 * Abstract pixel class, a single element in a raster image.
 * 
 */

// TODO: does this class need to exist? Or should it be its own raster class?

public abstract class Pixel extends RasterGraphic {

    public Pixel(int r, int c) {
    	origin = new RasterLocation(r, c);
    }
    
}
