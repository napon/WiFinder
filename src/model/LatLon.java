package model;

/**
 * Simple class to represent a Location
 * @author napontaratan
 */
public class LatLon {

	double latitude;
	double longitude;

	public LatLon(double lat, double lon){
		latitude = lat;
		longitude = lon;
	}

	public double getLatitude(){
		return this.latitude;
	}

	public double getLongitude(){
		return this.longitude;
	}
}