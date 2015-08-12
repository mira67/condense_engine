package helper_classes;

/* PixelIndexedColor
 * 
 * Abstract class for pixels that can be displayed on monitors.
 * 
 */

public class PixelIndexedColor extends PixelDisplayable {
	
	protected ColorTable colorTable;
	
	DN DEFAULT = new DN(0);		// Default to color index zero, the first color.
	
	// Index is the index into the color table.
	DN index = DEFAULT;
	
	// Contstructor with a color table and row,col location. Color defaults.
	PixelIndexedColor(ColorTable t, int r, int c) {
		super(r, c);
		index = DEFAULT;
		colorTable = t;
	}

	// Contstructor with a color table, color index, and row,col location
	PixelIndexedColor(ColorTable t, int i, int r, int c) {
		super(r, c);
		index = new DN(i);
		colorTable = t;
	}

	
	PixelIndexedColor( PixelIndexedColor pix ) {
		super(pix.row(), pix.col());
		index = pix.index();
		colorTable = pix.colorTable();
	}

	public DN index() {return index;} 
	public void index(int i) {index = new DN(i);} 
	public void colorTable( ColorTable ct ) {colorTable = ct;} 
	public ColorTable colorTable() {return colorTable;} 
	
	public int red() { return colorTable.get(index.asInt()).getRed(); }
	public int green() { return colorTable.get(index.asInt()).getGreen(); }
	public int blue() { return colorTable.get(index.asInt()).getBlue(); }
	public int alpha() { return colorTable.get(index.asInt()).getAlpha(); }
	
	public int grayScale() { 
		return (colorTable.get(index.asInt()).getRed() +
				colorTable.get(index.asInt()).getGreen() +
				colorTable.get(index.asInt()).getBlue() ) / 3;
	}

	public void setDefault(int i) { DEFAULT = new DN(i); }
	public void setDefault(DN i) { DEFAULT = i; }
	    
	public void print() {
		System.out.println("Index = " + index() + "  Alpha = " + alpha());
	}
	
	public PixelIndexedColor clone() {return new PixelIndexedColor(this);}
}
