package model;

/**
 * An object to be pushed to the server
 * - contains information about a WiFi Location
 * @author napontaratan
 */
public class WiFiPoint {

	String name;
	int signalStrength;
	LatLon location;

	public WiFiPoint(String name, int signal, double lat, double lon){
		this.name = name;
		this.signalStrength = signal;
		this.location = new LatLon(lat, lon);
	}
	
	public String getName() {
		return name;
	}
	
	public int getSignalStrength() {
		return signalStrength;
	}
	
	public double getLatitude() {
		return location.getLatitude();
	}
	
	public double getLongitude() {
		return location.getLongitude();
	}

}