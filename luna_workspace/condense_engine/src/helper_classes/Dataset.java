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
	
	public abstract int rows();
	public abstract int cols();

	public abstract Metadata readMetadata( String filename );
	public abstract ArrayList<GriddedLocation> locationsAsArrayList();
}
