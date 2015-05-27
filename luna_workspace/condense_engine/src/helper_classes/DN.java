package helper_classes;

/* Digital Number (DN)
 * 
 * An integer intended to represent the DN (typically 0-255) of data commonly
 * found in satellite images. 
 */

public class DN extends GeoObject {

	protected static int BITS = 8;
	
	public static final int MINIMUM = 0;
	public static final int MAXIMUM = (int) Math.pow(2, BITS) - 1;
	public static int DEFAULT = MINIMUM;
	
    protected int dn;
    
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
