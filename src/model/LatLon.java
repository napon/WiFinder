package model;

/**
 * Simple class to represent a Location
 * @author napontaratan
 */
public class LatLon {

	int latitude;
	int longitude;

	public LatLon(int lat, int lon){
		latitude = lat;
		longitude = lon;
	}

	public int getLatitude(){
		return this.latitude;
	}

	public int getLongitude(){
		return this.longitude;
	}
}