package com.napontaratan.wifi.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Date;

import android.net.wifi.ScanResult;

import com.google.android.gms.maps.model.LatLng;

/**
 * Simple struct-like object with description about an available wireless connection.
 *
 * @author Kurt Ahn
 */
public class WifiMarker {
    /**
     * Unique ID of the connection.
     */
	private final String ssid;
	
	/**
	 * Unique ID of the access point.
	 */
	private final String bssid;
	
	/**
	 * Detected signal strength.
	 */
	private final int strength;
	
	/**
	 * Location of discovery.
	 */
	private final LatLng location;
	
	/**
	 * Time of discovery.
	 */
	private final Date date;
	
	/**
	 * Unique ID given to the application's user.
	 */
	private final int clientId;
	
	// ==== getters ====
	
	public String getSSID() {
		return ssid;
	}

	public String getBSSID() {
		return bssid;
	}

	public int getSignalStrength() {
		return strength;
	}

	public LatLng getLocation() {
		return location;
	}

	public Date getDate() {
		return date;
	}

	public int getClientId() {
		return clientId;
	}
	
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
	public WifiMarker(
			ScanResult scan, LatLng location, int clientId) {
		this.ssid = scan.SSID;
		this.bssid = scan.BSSID;
		this.strength = scan.level;
		this.location = location;
		//this.time = scan.timestamp; // This requires min API level of 17
		this.date = Date.valueOf("0000-00-00"); // Temporary
		this.clientId = clientId;
	}
	
	public WifiMarker(String ssid, int signal, double lat, double lon, String date, int userID) {
		this.ssid = ssid;
		this.bssid = null;
		this.strength = signal;
		this.location = new LatLng(lat, lon);
		this.date = Date.valueOf(date);
		this.clientId = userID;
	}
	
	/**
	 * @return Information about the connection
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
			"Time: " + String.valueOf(date) + "\n" + 
			"Client ID: " + clientId;
	}
	
	// =========================== Database related stuff below ===============================
	
	/**
	 * Convert a WifiConnection object into an array of Bytes to be stored into the Database
	 * @param obj - (Object) WifiConnection object
	 * @return byte[]
	 * @throws IOException
	 * 
	 * @author Napon Taratan
	 */
	public static byte[] serialize(Object obj) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(out);
		os.writeObject(obj);
		return out.toByteArray();
	}
	
	/**
	 * Convert an array of Bytes back to its object form
	 * @param byte[] - data
	 * @return (Object) WifiConnection object
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * 
	 * @author Napon Taratan
	 */
	public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream is = new ObjectInputStream(in);
		return is.readObject();
	}
	
}
