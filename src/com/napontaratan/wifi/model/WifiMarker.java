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
 * A simple structure used by the server to plot discovered
 * Wi-Fi connections on the map.
 * 
 * <p>
 * <b>Note:</b> This type is different from <code>WifiConnection</code>,
 * which is used for data generated by clients and sent to the server.
 * WifiMarker on the other hand is generated using a collection of
 * WifiConnection objects. <b>DO NOT DELETE EITHER MODULE.</b>
 * </p>
 * 
 * @see WifiConnection
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
	 * Location of discovery.
	 */
	private final LatLng location;
	
	
	// What's the point of having getters for final fields?
	// Can't we just make the fields public?
	// ==== getters ====
	public String getSSID() {
		return ssid;
	}

	public String getBSSID() {
		return bssid;
	}

	public LatLng getLocation() {
		return location;
	}

	public WifiMarker(String ssid, String bssid, LatLng location) {
		// Temporary stuff
		this.ssid = ssid;
		this.bssid = bssid;
		this.location = location;
	}
	
	// =========================== Database related stuff below ===============================
	
	/**
	 * Convert a WifiMarker object into an array of Bytes to be stored into the Database
	 * @param obj - (Object) WifiMarker object
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
	 * @param data - Data to deserialize
	 * @return (Object) WifiMarker object
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
