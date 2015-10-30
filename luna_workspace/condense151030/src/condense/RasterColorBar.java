package condense;

/* RasterColorBar
 * 
 * A bar of colors.
 */

import java.util.*;
import java.awt.Color;

public class RasterColorBar extends RasterGraphic {

	RasterRectangle bar;
	Color outlineColor = Color.BLACK;
	int outlineWidth = 1;
	
	enum Orientation {LEFTRIGHT, RIGHTLEFT, UPDOWN, DOWNUP}
	
	ColorTable colorTable;
	
	Orientation orientation = Orientation.LEFTRIGHT;
	
	RasterColorBar(int r, int c, int width, int height, ColorTable colors) {
		
		bar = new RasterRectangle(r, c, width, height);
		
		colorTable(colors);
	}
	
	
	public void colorTable( ColorTable c) {
		colorTable = c;
	}

	
	public void row(int r) {bar.row(r);}
	public void col(int c) {bar.col(c);}
	
	public void width(int w) {bar.width(w);}
	public void height(int h) {bar.height(h);}
	
	public void outlineWidth( int w ) {outlineWidth = w;}
	
	public void outline( int width, Color c ) {
		outlineColor = c;
		outlineWidth = width;
	}
	
	public ArrayList<PixelDisplayable> getPixels() {
		
		ArrayList<PixelDisplayable> pixels = new ArrayList<PixelDisplayable>();

		int colorIndex;
		
		float size = colorTable.size() - 1;
		
		// Dimensions of the colored part of the bar, no including the outline.
		int rSize = bar.height()-(2*outlineWidth);
		int cSize = bar.width()-(2*outlineWidth);
		
		for (int r = 0; r < rSize; r++) {
			for (int c = 0; c < cSize; c++) {
				
				switch(orientation) {
					case LEFTRIGHT:
						colorIndex = (int) (size * c / (cSize - 1));
						break;
					case RIGHTLEFT:
						colorIndex = (int) (size * ((cSize - c) - 1) / (rSize - 1));
						break;
					case UPDOWN:
						colorIndex = (int) (size * r / (rSize - 1));
						break;
					default:		// DOWNUP
						colorIndex = (int) (size * ((rSize - r) - 1) / (rSize - 1));
				}

				int row = r + outlineWidth ;
				int col = c + outlineWidth;
				
				pixels.add( new PixelIndexedColor( colorTable, colorIndex, row, col ) );
			}
		}
		
		// Add an outline
		if (outlineWidth > 0) {
			for (int r = 0; r < bar.height(); r++) {
				for (int w = 0; w < outlineWidth; w++) {
					pixels.add( new PixelRGBA(outlineColor, r, 0+w) );
					pixels.add( new PixelRGBA(outlineColor, r, (bar.width()-1)-w) );					
				}
			}
			for (int c = 0; c < bar.width(); c++) {
				for (int w = 0; w < outlineWidth; w++) {
					pixels.add( new PixelRGBA(outlineColor, 0+w, c) );
					pixels.add( new PixelRGBA(outlineColor, (bar.height()-1)-w, c) );					
				}
			}
		}
		return pixels;
	}
	
}
