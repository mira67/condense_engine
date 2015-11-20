package condense;

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
    
    public ColorTable() {
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
    
    public void temperature() { temperature(256); }
    public void prism() { prism(256); }

    /* prism
     * 
     * Create a chromatic color table with 'number' of colors, 
     */
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
    	
    	// First and last colors on the the table will be black and white.
    	set(low, BLACK);
    	set(high, WHITE);
    }

    /* temperature
     * 
     * Create a chromatic color table with 'number' of colors,
     * representing temperatures, cold to hot. 
     */
    public void temperature(int number) {
       	clear();
    	
       	double high = 255;
       	
    	for (double i = 0; i < number; i++) {
    		double percent = i/number;
    		
    		int red = (int) Math.round(percent * high);
    		int blue = (int) Math.round((1.0 - percent) * high);

    		// Low green, to add brightness
    		add(new Color(red, 50, blue));
    	}
    	
    	//set(0, BLACK);
    }
}
