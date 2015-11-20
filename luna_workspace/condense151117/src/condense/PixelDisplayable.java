package condense;

/* PixelDisplayable
 * 
 * Abstract class for pixels that can be displayed on monitors.
 */

public abstract class PixelDisplayable extends Pixel {

	public enum ColorMode { RGB, RGBA, INDEXED, MONOCHROME; }

	public abstract int red();
	public abstract int green();
	public abstract int blue();
	public abstract int grayScale();
	public abstract void print();

    public abstract PixelDisplayable clone();
    
	protected static DN UNSATURATED = new DN( DN.MINIMUM );
	protected static DN SATURATED = new DN( DN.MAXIMUM );
	protected static DN DEFAULT_SATURATION = UNSATURATED;
	
	// Opacity on a scale from zero to one.
	protected static float CLEAR = 0;
	protected static float OPAQUE = 1;
	protected static float DEFAULT_OPACITY = OPAQUE;
	
	PixelDisplayable( int r, int c) {
		super(r, c);
	}
}
