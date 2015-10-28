package condense;

/* Location
 * 
 * A lat/lon location. 
 */

public class GeoLocation extends Location {
	
	protected double latitude;		// Decimal degrees
	protected double longitude;
	
	protected Projection projection;
	
	public GeoLocation() {
		lat( NODATA );
		lon( NODATA );
	}
	
	
	public GeoLocation(double lat, double lon) {
		lat( lat );
		lon( lon );
	}
	
	public double lat() { return latitude; }
	public void lat( double lat ) { latitude = lat; }

	public double lon() { return longitude; }
	public void lon( double lon ) { longitude = lon; }
	  
	
	/* equals
	 * 
	 * Does this location match another exactly?
	 */
	public boolean equals(GeoLocation loc) {
		if (this.lat() == loc.lat() && this.lon() == loc.lon()) return true;
		return false;
	}
		  
		
	/* equals
	 * 
	 * Do two locations match exactly?
	 */
	public static boolean equals( GeoLocation loc1, GeoLocation loc2 ) {
		if (loc1.lat() == loc2.lat() && loc1.lon() == loc2.lon()) return true;
		return false;
	}


    /* findDistance
     *
     *  Given two lat/lons in decimal degrees, find the great-circle distance
     *   between them (km).
     */
    public static double findDistance( double lat1, double lon1, double lat2, double lon2 ) {

	    double lat1Rad = Math.toRadians(lat1);
	    double lat2Rad = Math.toRadians(lat2);
	    double lonDiffRad = Math.toRadians(lon2) - Math.toRadians(lon1);

	    // Distance in radians between to two locations
	    double deltaSigma = Math.acos( Math.sin(lat1Rad)*Math.sin(lat2Rad) + 
	                                   Math.cos(lat1Rad)*Math.cos(lat2Rad)*Math.cos(lonDiffRad));

	    double earthRadius = 6399.594;  // Distance in km from the center of earth to pole,
	                                    // an approximation using a flattened spheroid.

	    double distance = deltaSigma * earthRadius; 

	    return distance;
	}


	/* isApproxSame
	 * 
	 * Are the lat/lons of two locations the same, to within n/1000ths of a
	 * degree? Overcomes precision errors. Beware of projection distortions
	 * near the poles: lat and lon are not proportional.
	 */
	public boolean isApproxSame( GeoLocation loc, int n ) {
		int myLat = (int) (this.lat()*1000.0);
    	int myLon = (int) (this.lon()*1000.0);
    	int inLat = (int) (loc.lat()*1000.0);
    	int inLon = (int) (loc.lon()*1000.0);

    	if (Math.abs(myLat-inLat) < n && Math.abs(myLon-inLon) < n) return true;
    	return false;
	}


    /* findNearestLocation
     * 
     *  Given a lat/lon pair, find the nearest row,col centroid in an array of locations.
     */
    public static Location findNearestLocation( double lat, double lon, GeoLocation[][] locs) {
    	
	    GeoLocation nearestLoc = locs[0][0];
	    double minDistance = 9999999.0;
	    double distance = 9999999.0;

	    for (int y = 0; y < locs.length; y++) {
	        for (int x = 0; x < locs[0].length; x++) {
	            distance = findDistance(lat, lon, locs[y][x].lat(), locs[y][x].lon());

	            if (distance < minDistance) {
	                nearestLoc = locs[y][x];
	                minDistance = distance;
	            }
	        }
	    }

	    Tools.debugMessage("NearestLocation to lat,lon = " + lat + "," + lon +
	                       "  :distance = " + minDistance);
	    return nearestLoc;
	}


	/*
	 * findLocation
	 * 
	 * Search for an index in an array of gridded locations. Warning: returns
	 * null if it doesn't find it.
	 */
	public static GriddedLocation findLocation(GriddedLocation[][] locs,
			int index) {
		for (int r = 0; r < locs.length; r++) {
			for (int c = 0; c < locs[0].length; c++) {
				if (locs[r][c].id == index)
					return locs[r][c];
			}
		}

		return null;
	}

	public void print(){
		System.out.print(lat() + " / " + lon());
  	}
}
