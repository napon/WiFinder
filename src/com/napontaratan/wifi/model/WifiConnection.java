package com.napontaratan.wifi.model;

import android.net.wifi.ScanResult;

import com.google.android.gms.maps.model.LatLng;

/**
 * Simple struct-like object with description about an available wireless connection.
 *
 * @author Kurt Ahn
 */
public class WifiConnection {
    /**
     * Unique ID of the connection.
     */
	public final String ssid;
	
	/**
	 * Unique ID of the access point.
	 */
	public final String bssid;
	
	/**
	 * Detected signal strength.
	 */
	public final int strength;
	
	/**
	 * Location of discovery.
	 */
//	public final WifiLocation location;
	public final LatLng location;
	
	/**
	 * Time of discovery.
	 */
	public final long time;
	
	/**
	 * Unique ID given to the application's user.
	 */
	public final String clientId;
	
	/*
	 * TODO: We need information about whether the connection is secured, etc.
	 */
	
	/**
	 * Construct a WifiConnection.
	 *
	 * @param scan - Data obtained from scanning for available wifi connections.
	 *        Contains the SSID, BSSID, signal strength, time recorded, etc.
	 * @param location - Location of discovery.
	 * @param clientId - Unique ID given to the application's user.
	 *
	 * @author Kurt Ahn
	 */
	public WifiConnection(
			ScanResult scan, LatLng location, String clientId) {
		this.ssid = scan.SSID;
		this.bssid = scan.BSSID;
		this.strength = scan.level;
		this.location = location;
		//this.time = scan.timestamp; // This requires min API level of 17
		this.time = 0L; // Temporary
		this.clientId = clientId;
	}
	
	/**
	 * @return Information about the connection formatted as:
	 * <p>
	 * SSID: {@link #ssid}</br>
	 * BSSID: {@link #bssid}</br>
	 * Strength: {@link #strength}</br>
	 * Location: {@link #location}</br>
	 * Time: {@link #time}</br>
	 * Client ID: {@link #clientId} </br>
	 * </p>
	 * 
	 * @author Kurt Ahn
	 */
	@Override
	public String toString() {
		return
			"SSID: " + ssid + "\n" +
			"BSSID: " + bssid + "\n" +
			"Strength: " + String.valueOf(strength) + "\n" +
			"Location: " + location.latitude + " "  + location.longitude + "\n" +
			"Time: " + String.valueOf(time) + "\n" + 
			"Client ID: " + clientId;
	}
}
