package com.napontaratan.wifi.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * An object to be pushed to the server
 * - contains information about a WiFi Location
 * @author napontaratan
 */
public class WifiMarker {
	public String ssid;
	
	public int strength;
	
	public LatLng location;
	
	/**
	 * Construct a WifiMarker object. Note that this is only for 
	 * testing purposes.
	 * 
	 * @param ssid
	 * @param strength
	 * @param location
	 * 
	 * @author Kurt Ahn
	 */
	public WifiMarker(
			String ssid, int strength, double latitude, double longitude) {
		this.ssid = ssid;
		this.strength = strength;
		this.location = new LatLng(latitude, longitude);
	}
}
