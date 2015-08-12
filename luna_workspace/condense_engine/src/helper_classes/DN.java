package helper_classes;

/* Digital Number (DN)
 * 
 * An integer intended to represent the DN (0-255, 8 bits) of data commonly
 * found in satellite images and colors values. Provides automatic error checking.
 */

public class DN extends GeoObject {

	private static final int BITS = 8;
	
	public static final int MINIMUM = 0;
	public static final int MAXIMUM = (int) Math.pow(2, BITS) - 1;
	public static int DEFAULT = MINIMUM;
	
    private int dn;
    
    // Constructors
    DN() { dn = MINIMUM; }
    DN(int i) { dn = validateDN(i); }
    
    
    /* validateDN
     * 
     * Ensure that a number is within the valid range.
     */
    public static int validateDN( int i ) {
    	if (i == NODATA) return DEFAULT;
    	if (i < MINIMUM) return MINIMUM;
    	if (i > MAXIMUM) return MAXIMUM;
    	
    	return i;
    }
    

    public int asInt() { return dn; }
    public float asFloat() { return (float) dn; }
    
    
    // setDefault
    public static void setDefault( int i ) { DEFAULT = validateDN(i); }    
}
