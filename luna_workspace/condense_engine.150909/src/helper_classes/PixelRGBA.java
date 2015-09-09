package helper_classes;

import java.awt.Color;

/* PixelRGBA
 * 
 * Four-channel color pixel. Red, green, blue, and alpha (opacity).
 */

public class PixelRGBA extends PixelDisplayable {

	protected static Color DEFAULT_COLOR = Color.BLACK;
	
	Color color = DEFAULT_COLOR;
	
	PixelRGBA(int r, int g, int b, int inRow, int inCol) {
		super(inRow, inCol);
		color = new Color(r, g, b, DEFAULT_OPACITY);
	}

	
	PixelRGBA(int r, int g, int b, int a, int inRow, int inCol) {
		super(inRow, inCol);
		color = new Color(r, g, b, a);
	}

	
	PixelRGBA(Color inputColor, int r, int c) {
		super(r, c);
		color = inputColor;
	}


	PixelRGBA( PixelRGBA pix ) {
		super(pix.row(), pix.col());
		color = pix.color();
	}

	
	public Color color() {return color;}
	public int red() {return color.getRed();} 
	public int green() {return color.getGreen();} 
	public int blue() {return color.getBlue();} 
	public int alpha() {return color.getAlpha();} 
	public int grayScale() {return (red()+green()+blue())/3;}


	// Set the RGB color
	public void color( int r, int g, int b ) {
		color = new Color(r, g, b, DEFAULT_OPACITY);
	}

	// Set the RGB color and transparency (alpha)
	public void color( int r, int g, int b, int a ) {
		color = new Color(r, g, b, a);
	}

	
	// Set the color to monochromatic
	public void monochrome( int c ) {
		color = new Color(c, c, c, DEFAULT_OPACITY);
	}

	
	// Set the pixel to white or black.
	public void white() {color = Color.WHITE;}
	public void black() {color = Color.BLACK;}

	public PixelRGBA clone() {return new PixelRGBA( this );}

	
	public void print() {
		System.out.println("  red = " + red() + "  green = " + green() +
				"  blue = " + blue());
	}
}
