package condense;

/* Dataset
 * 
 * Abstract class to encapsulate datasets.
 */

public abstract class Dataset extends GeoObject {
	
	protected GriddedLocation[][] locs;

	// Data types
	public enum DataType {
		SEA_ICE("seaice"), SSMI("ssmi"), AVHRR("avhrr"),
		EASE_GRID_SURFACE("ease grid surface");
		
		private final String name;

		private DataType(String s) {
			name = s;
		}

		public String toString() {
			return name;
		}
	}
}
