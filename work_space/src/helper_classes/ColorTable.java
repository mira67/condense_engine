package helper_classes;

/* ColorTable
 * 
 * A table of colors for index-colored pixels.
 */

import java.util.*;
import java.awt.Color;

public class ColorTable extends GeoObject {

	public Color BLACK = new Color(0,0,0);
	public Color WHITE = new Color(255,255,255);
	
    ArrayList<Color> table = new ArrayList<Color>();
    
    ColorTable() {
    	add(BLACK);
    	add(WHITE);
    }
    
    public Color get(int i) { return table.get(i); }
    public int size() { return table.size(); }
    public void clear() { table.clear(); }
    public void add(Color c) {table.add(c);}
    public void remove(int i) {table.remove(i);}
    public void remove(Color c) {table.remove(c);}
    public void set(int i, Color c) {table.set(i, c);}
    
    public void grayScale() { grayScale(256); }
    
    public void grayScale(int number) {
       	clear();
    	
    	for (int i = 0; i < number; i++) {
    		add(new Color(i,i,i));
    	}
    }
    
    public void prism() { prism(256); }
    
    public void prism(int number) {
       	clear();
    	
       	int high = 255;
       	int low = 0;
       	
    	for (double i = 0; i < number; i++) {
    		int red = low;
    		int blue = low;
    		
    		double percent = i/number;
    		
    		if (percent < 0.50) red = (int) (high - percent*2*high);
    		if (percent > 0.50) blue = (int) ((percent - 0.5)*2*high);
    		int green = (int) (percent*high*2);
    		if (percent > 0.50) green = (int) (high - (percent - 0.5)*2*high);
    		
    		add(new Color(red,green,blue));
    	}
    	
    	set(0, BLACK);
    	set(255, WHITE);
    }
}
