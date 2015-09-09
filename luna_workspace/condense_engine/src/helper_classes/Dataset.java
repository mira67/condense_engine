package helper_classes;

/* Dataset
 * 
 * Abstract class to encapsulate entire datasets. The derived classes should
 * handle file reading and data retrieval.
 */

import java.util.ArrayList;

public abstract class Dataset extends GeoObject {
	
	protected Metadata metadata;
	protected boolean haveMetadata = false;
	protected GriddedLocation[][] locs;
	// /protected ArrayList<GriddedLocation> locs;

	public int rows() {
		return metadata.rows;
	}

	public int cols() {
		return metadata.cols;
	}

	public abstract Metadata readMetadata( String filename );
	public abstract ArrayList<GriddedLocation> locationsAsArrayList();
	
	// Data types
	public enum DataType {
		SEA_ICE("seaice"), SSMI("ssmi"), AVHRR("avhrr");
		private final String name;

		private DataType(String s) {
			name = s;
		}

		public String toString() {
			return name;
		}
	}

}
