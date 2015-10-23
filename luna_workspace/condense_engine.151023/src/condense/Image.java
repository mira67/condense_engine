package condense;

/* Image
 *
 * An image to be viewed on a display device or saved to a file.
 * 
 * Individual layers can also be displayed or saved. Since the layers are
 * stored in an ordered ArrayList they are indexed from zero; users can edit
 * layers individually but need not be aware of them if they just want to
 * create and display a simple image. 
 *
 * @author glgr9602
 * 2014.09.17
 */

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.*;

public class Image extends GeoObject {

	protected ArrayList<RasterLayer> layers;

	public Image() {

		layers = new ArrayList<RasterLayer>();
	}

	// Construct an image with a user-supplied layer
	public Image(RasterLayer layer) {

		layers = new ArrayList<RasterLayer>();

		layers.add(layer);
	}

	/*
	 * addLayer
	 * 
	 * Add a layer to the image.
	 */
	public void addLayer(RasterLayer layer) {
		layers.add(layer);
	}

	/*
	 * getLayer
	 * 
	 * Retrieve a layer from the list of layers.
	 */
	public RasterLayer getLayer(int i) {

		RasterLayer layer = null;

		// TODO: should this be a try/catch, or just a test of the number of
		// layers?
		try {
			layer = layers.get(i);
		} catch (Exception e) {
			Tools.warningMessage("Image:ImageLayer: error on getting layer #"
					+ i);
			Tools.warningMessage("Error: " + e);
		}

		return layer;
	}

	/*
	 * defaultLayerName
	 * 
	 * Return a unique default name for a layer.
	 */
	String defaultLayerName() {
		String name = "Layer ";

		// No layers yet? This will be zero.
		if (layers == null)
			return name + "0";

		int i = layers.size();

		Iterator<RasterLayer> iter = layers.iterator();

		// Make sure the name isn't already in use.
		while (iter.hasNext()) {
			name = name + Integer.toString(i);

			if (iter.next().name() == name)
				i++;
		}

		return name;
	}

	/*
	 * mergeLayers
	 * 
	 * Merge two layers in the image into a single layer. Warning: returns null
	 * if the layers don't exist.
	 */
	public RasterLayer mergeLayers(int top, int bottom) {

		// No layers?
		if (layers == null) {
			Tools.warningMessage("Image::mergeLayers: layers = null, no layers");
			return null;
		}

		int i = layers.size();

		// Sanity check.
		if (top > i - 1 || bottom > i - 1 || top < 0 || bottom < 0) {
			Tools.warningMessage("Image::mergeLayers: layers selected for merging are out");
			Tools.warningMessage("of range. Requested top = " + top
					+ ", bottom = " + bottom + ", total layers = " + i);
		}

		RasterLayer merged = layers.get(bottom).clone();
		merged.addPixels(layers.get(top).pixels());

		// TODO - may be duplicate pixels (two pixels at the same location)
		// squish them together and eliminate duplicates

		return merged;
	}

	/*
	 * printLayerNames
	 * 
	 * Print the names for the image's layers, in order of precedence (top layer
	 * to base layer). Mostly for debugging purposes.
	 */
	public void printLayerNames() {

		// No layers yet? This will be zero.
		if (layers == null) {
			Tools.warningMessage("Image::printLayerNames: layers = null, no layers");
			return;
		}

		int i = layers.size();

		if (i == 0) {
			Tools.warningMessage("Image::printLayerNames: no layers");
			return;
		}

		System.out.println("Print layers: (" + i + " total)");

		for (int j = 0; j < i; j++) {
			RasterLayer l = layers.get(j);
			System.out.println("Layer " + j + ": " + l.name());
		}
	}

	protected BufferedImage createBufferedImage(int type, int rows, int cols) {

		// Create the image objects
		BufferedImage bufferedImage = new BufferedImage(cols, rows, type);
		Graphics2D graphics = bufferedImage.createGraphics();
		Color color;

		for (int i = 0; i < layers.size(); i++) {
			Iterator<PixelDisplayable> iter = layers.get(i).pixelIterator();

			float layerOpacity = layers.get(i).opacity();

			Tools.debugMessage("Image::createBufferedImage: layer = "
					+ layers.get(i).name());
			Tools.debugMessage("       Size = " + layers.get(i).pixels().size());
			Tools.debugMessage("       Opacity = " + layers.get(i).opacity());

			while (iter.hasNext()) {
				PixelDisplayable pixel = iter.next();

				// Set the opacity: the pixel opacity * the layer opacity.
				int alpha = new DN((int) layerOpacity * DN.MAXIMUM).asInt();

				color = new Color(pixel.red(), pixel.green(), pixel.blue(),
						alpha);
				graphics.setColor(color);
				graphics.draw(new Ellipse2D.Float(pixel.col(), pixel.row(), 0,
						0));
			}
		}

		return bufferedImage;
	}

	/*
	 * saveJPG
	 * 
	 * Save the layer as a JPG image.
	 */
	public void saveJPG(String filename, int rows, int cols) {

		// Tack on the jpg extension. TBD necessary?
		String newName = filename + ".jpg";

		// Create the image object
		BufferedImage bufferedImage = createBufferedImage(
				BufferedImage.TYPE_INT_RGB, rows, cols);

		try {
			// Create the output file.
			File file = new File(newName);
			file.createNewFile();

			// Write the buffered image to the file.
			ImageIO.write(bufferedImage, "jpg", file);
		} catch (Exception e) {
			Tools.warningMessage("Image::saveJPG: Error when saving file: " + e);
			Tools.warningMessage("Attempted file path: " + newName);
		}
	}

	/*
	 * savePNG
	 * 
	 * Save the layer as a PNG image.
	 */
	public void savePNG(String filename, int rows, int cols) {

		// Tack on the jpg extension. TBD necessary?
		String newName = filename + ".png";

		// Create the image object
		BufferedImage bufferedImage = createBufferedImage(
				BufferedImage.TYPE_INT_ARGB, rows, cols);

		try {
			// Create the output file.
			File file = new File(newName);
			file.createNewFile();

			// Write the buffered image to the file.
			ImageIO.write(bufferedImage, "png", file);
		} catch (Exception e) {
			Tools.warningMessage("Image::savePNG: Error when saving file: " + e);
			Tools.warningMessage("Attempted file path: " + newName);
		}
	}

	/*
	 * Display
	 * 
	 * Display the image.
	 */
	public void display(String title, int rows, int cols) {

		// Create the image using the pixel objects
		BufferedImage image = createBufferedImage(BufferedImage.TYPE_INT_ARGB,
				rows, cols);
		// /BufferedImage image =
		// createBufferedImage(BufferedImage.TYPE_INT_RGB, rows, cols);

		try {
			new Display(image, title);
		} catch (Exception e) {
			Tools.warningMessage("Image::display: Error on image display: " + e);
		}
	}

	/*
	 * addTestPattern
	 * 
	 * Add a layer with a test pattern.
	 */
	public void addTestPattern(String name) {
		RasterLayer layer = new RasterLayer(name);

		for (int r = 0; r < 256; r++) {
			layer.addPixel(new PixelRGBA(r, 255 - r, r, 255, r, r));
			layer.addPixel(new PixelRGBA(255 - r, r, r, 255, 255 - r, r));
		}

		layers.add(layer);
	}
}
