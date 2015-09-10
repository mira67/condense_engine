package helper_classes;

/* GriddedLocation
 * 
 * A location of a gridded data point, with both a raster (r,c) location
 * and a lat/lon location. 
 */

public class GriddedLocation extends Location {
	
	protected GeoLocation geoloc;		// Decimal degrees
	protected RasterLocation rastloc;
	
	// Database table entry id. -1 indicates not entered.
	protected int id = -1;
	
	protected boolean hasLatLon = false;
	
	protected GriddedLocation() {}

	public GriddedLocation(int r, int c) {
		rastloc = new RasterLocation(r,c);
		geoloc = new GeoLocation();
	}

	public GriddedLocation(int r, int c, double lat, double lon) {
		rastloc = new RasterLocation(r,c);
		geoloc = new GeoLocation(lat,lon);

		hasLatLon = true;
	}

	public GriddedLocation(int indexID, int r, int c, double lat, double lon) {
		rastloc = new RasterLocation(r,c);
		geoloc = new GeoLocation(lat,lon);
		id = indexID;
		
		hasLatLon = true;
	}
	
	public int row() { return rastloc.row(); }
	public int col() { return rastloc.col(); }
	public void row( int r ) { rastloc.row(r); }
	public void col( int c ) { rastloc.col(c); }
	
	public double lat() { return geoloc.lat(); }
	public double lon() { return geoloc.lon(); }
	
	public void latlon( double lat, double lon ) {
		geoloc.lat(lat);
		geoloc.lon(lon);
		hasLatLon = true;
	}
  
	/* equals
	 *
	 * Do two locations match exactly?
	 */
	public boolean equals( GriddedLocation loc ) {
		if (geoloc.equals(loc.geoloc) && rastloc.equals(loc.rastloc)) return true;
		return false;
	}


    /* findDistance
     *
     *  Find the great-circle distance between this location and another geoLocation (km).
     */
    public double findDistance( GeoLocation there ) {
    	return GeoLocation.findDistance(lat(), lon(), there.lat(), there.lon());
    }

    
	public void print(){
		System.out.print("(" + row() + "," + col() + ")  " + lat() + " / " + lon());
  	}
}
