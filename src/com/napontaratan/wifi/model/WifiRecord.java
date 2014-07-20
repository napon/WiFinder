package com.napontaratan.wifi.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

public class WifiRecord {
	/**
	 * 
	 */
	public final String ssid;
	
	/**
	 * 
	 */
	public final double distance;
	
	/**
	 * 
	 */
	public final int strength;
	
	/**
	 * 
	 * @param marker
	 * @param ssid
	 * @param location
	 * @param strength
	 */
	public WifiRecord(WifiMarker marker,
			String ssid, LatLng location, int strength) {
		this.ssid = ssid;
		this.distance = SphericalUtil.computeDistanceBetween(
				marker.location, location);
		this.strength = strength;
	}
}
