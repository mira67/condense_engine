package condense;

/* PixelMonochrome
 * 
 * A monochrome pixel, gray-scale.
 * 
 */

import java.awt.Color;

public class PixelMonochrome extends PixelDisplayable {

	protected static DN BLACK = new DN(DN.MINIMUM);
	protected static DN WHITE = new DN(DN.MAXIMUM);
	protected static DN DEFAULT = BLACK;

	DN value = DEFAULT;

	PixelMonochrome(int r, int c) {
		super(r, c);
		value = DEFAULT;
	}

	PixelMonochrome(int i, int r, int c) {
		super(r, c);
		value = new DN(i);
	}

	PixelMonochrome(PixelMonochrome pix) {
		super(pix.row(), pix.col());
		value = pix.value();
	}

	public DN value() {return value;}
	public void value(DN i) {value = i;}
	public void value(int i) {value = new DN(i);}

	public int red() {return value.asInt();}
	public int green() {return value.asInt();}
	public int blue() {return value.asInt();}

	public int grayScale() {return value.asInt();}

	public Color color() {
		return new Color(value.asInt(), value.asInt(), value.asInt());
	}

	public static void setDefault(int i) {DEFAULT = new DN(i);}

	public static void setDefault(DN i) {DEFAULT = i;}

	public void print() {System.out.println("Gray value = " + grayScale());}

	public PixelMonochrome clone() {return new PixelMonochrome(this);}

}
