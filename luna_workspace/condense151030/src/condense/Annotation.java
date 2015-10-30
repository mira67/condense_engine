package condense;

import java.awt.Color;
import java.awt.Font;

/*
 * Annotation
 * 
 * Provides a handy class for storing annotation comments for images.
 */

public class Annotation extends GeoObject {

	RasterLocation location;
	String comment;
	Font font;
	Color color;
	
	public Annotation(String str, int row, int col) {
		
		location = new RasterLocation(row, col);
		comment = str;
		
		// Default font and color.
		font = new Font("PLAIN", 0, 15);	// Maybe comic sans?
		color = new Color(255, 255, 0);		// Yellow. Ha!
	}
	
	// Gets
	
	public String getString() {return comment;}
	public int row() {return location.row();}
	public int col() {return location.col();}
	public RasterLocation location() {return location;}
	public Font font() { return font; }
	public Color color() { return color; }
	
	// Sets
	public void location( RasterLocation loc ) {location = loc;}
	public void row( int r ) {location.row(r);}
	public void col( int c ) {location.col(c);}
	public void setString( String s ) {comment = s;}
	public void setFont( Font f ) { font = f; }
	public void setColor( Color c ) { color = c; }
}
