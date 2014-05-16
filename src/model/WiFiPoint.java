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

	public WiFiPoint(String name, int signal, int lat, int lon){
		this.name = name;
		this.signalStrength = signal;
		this.location = new LatLon(lat, lon);
	}

}