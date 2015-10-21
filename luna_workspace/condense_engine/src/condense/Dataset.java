package condense;

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

	public int rows() {
		return metadata.rows;
	}

	public int cols() {
		return metadata.cols;
	}

	public abstract Metadata readMetadata( String filename );
    public GriddedLocation[][] getLocations() { return locs; }
    
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

	/*
	 * locationsAsArrayList
	 * 
	 * Return the matrix of locations as an arraylist.
	 */
	public ArrayList<GriddedLocation> locationsAsArrayList() {
		ArrayList<GriddedLocation> list = new ArrayList<GriddedLocation>();

		for (int r = 0; r < rows(); r++) {
			for (int c = 0; c < cols(); c++) {
				list.add(locs[r][c]);
			}
		}

		return list;
	}

}
