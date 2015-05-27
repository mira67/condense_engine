package helper_classes;

import java.util.*;

/* RasterLayer
 * 
 * A layer of pixels, used to construct a raster image.
 *
 */

public class RasterLayer extends GeoObject {

	// The layer can be opaque, clear, or anywhere in between. This is
	// independent of pixel opacity.
	protected final float CLEAR = 0;
	protected final float OPAQUE = 1;

	// Opacity of the layer.
	protected float opacity = OPAQUE;	// Opaque by default.

	protected ArrayList<PixelDisplayable> pixels = new ArrayList<PixelDisplayable>();

	protected String layerName = "unknown";

	// Constructors
	RasterLayer() {
	}

	// Constructor with layer name.
	RasterLayer(String name) {
		layerName = name;
	}

	// Constructor with integer pixel data. Create monochrome pixels.
	RasterLayer(ColorTable c, int data[][]) {
		addPixels(c, data);
	}

	/*// Constructor with sensor pixel data
	RasterLayer(ColorTable c, GriddedVector sensorPix[][]) {
		addPixels(c, sensorPix);
	}*/

	// Constructor with displayable pixel data.
	RasterLayer(ArrayList<PixelDisplayable> pix) {
		addPixels(pix);
	}

	/*
	 * clone
	 * 
	 * Clone the layer
	 */
	public RasterLayer clone() {
		RasterLayer newLayer = new RasterLayer(pixels);
		return newLayer;
	}

	/*
	 * pixels
	 * 
	 * Return a (cloned) list of this layer's pixels.
	 */
	public ArrayList<PixelDisplayable> pixels() {

		ArrayList<PixelDisplayable> list = new ArrayList<PixelDisplayable>();

		Iterator<PixelDisplayable> iter = pixels.iterator();
		while (iter.hasNext()) {
			list.add(iter.next().clone());
		}

		return list;
	}

	/*
	 * pixels
	 * 
	 * Return an iterator over this layer's pixels.
	 */
	public Iterator<PixelDisplayable> pixelIterator() {
		return pixels.iterator();
	}

	// Layer opacity
	public void opacity(float f) {
		opacity = f;
	}

	public float opacity() {
		return opacity;
	}

	public String name() {
		return layerName;
	}

	public void name(String s) {
		layerName = s;
	}

	// Is the layer opaque?
	public boolean opaque() {
		if (opacity == OPAQUE)
			return true;
		return false;
	}

	/*
	 * addPixel
	 * 
	 * Add a pixel.
	 */
	public void addPixel(PixelDisplayable pix) {
		pixels.add(pix);
	}

	/*
	 * addPixels
	 * 
	 * Add integer data as indexed color pixels. Skip any 'nodata' values.
	 
	public void addPixels(ColorTable ct, int[][] data) {
		for (int row = 0; row < data.length; row++) {
			for (int col = 0; col < data[0].length; col++) {
				if (data[row][col] == NODATA)
					continue;
				pixels.add(new PixelIndexedColor(ct, data[row][col], row, col));
			}
		}
	}*/

	/*
	 * addPixels
	 * 
	 * Add pixels to the layer. Skip any 'nodata' values.
	 */
	public void addPixels(ColorTable ct, int pixArray[][]) {

		for (int r = 0; r < pixArray.length; r++) {
			for (int c = 0; c < pixArray[0].length; c++) {
				if (pixArray[r][c] == NODATA)
					continue;
				pixels.add(new PixelIndexedColor(ct,
						pixArray[r][c], r, c));
			}
		}
	}

	/*
	 * addPixels
	 * 
	 * Add a list of pixels.
	 */
	protected void addPixels(ArrayList<PixelDisplayable> list) {

		Iterator<PixelDisplayable> iter = list.iterator();

		while (iter.hasNext()) {
			pixels.add(iter.next());
		}
	}
}
