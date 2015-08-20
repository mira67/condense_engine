package helper_classes;

/* SurfaceVector
 * 
 * Vector class for surface types of land, ice, coastlines and ocean (LOCI).
 *
 * Reference: http: *nsidc.org/data/ease/ancillary.html
 *
 * For surface type bytes:
 * 	0 = land
 * 	101 = ice
 * 	252 = coast
 * 	254 = unknown
 * 	255 = water
 */


public class SurfaceVector extends GriddedVector {

	public enum Surface {
		LAND(0),
		ICE(101),
		COAST(252),
		UNKNOWN(254),
		WATER(255);
		
		private final int type;
		
		Surface( int type ) {
			this.type = type;
		}
		
		public int value() { return type; };
	}
	
	
	// Constructor using a supplied classification value
	public SurfaceVector( int i, GriddedLocation loc, int timeID) {
	    super(i, loc, timeID);
	}
	
	// Various gets and sets
	public int getSurface() { return classification(); }
	public void setSurface( int surf ) { classification(surf); }

	public boolean isIce() { return (data == Surface.ICE.value()); }
	public boolean isWater() { return (data == Surface.WATER.value()); }
	public boolean isCoast() { return (data == Surface.COAST.value()); }
	public boolean isLand() { return (data == Surface.LAND.value()); }
	public boolean isUnknown() { return (data == Surface.UNKNOWN.value()); }

	public String surfaceAsText() {
	    if (isIce()) return "ice";
	    if (isLand()) return "land";
	    if (isCoast()) return "coast";
	    if (isWater()) return "water";
	    return "unknown";
	}

	
	public void printLong() {
	    this.printValue();
	}
	
	
	public void printValue() {
	    System.out.println("Surface vector: " + surfaceAsText() +
	                     ", value = " + data );
	}

	
	public void print() {
	    System.out.print("Surface vector: " + surfaceAsText() +
                ", " + data + ", ");
	}
	
}
