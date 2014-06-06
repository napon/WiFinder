package com.napontaratan.wifi.controller;

import java.sql.Date;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.provider.Settings.Secure;

import com.google.android.gms.maps.model.LatLng;
import com.napontaratan.wifi.database.OfflineWifiDB;
import com.napontaratan.wifi.model.WifiConnection;

/**
 * BroadcastReceiver object triggered when a requested
 * wifi scan is complete.
 * 
 * @author Kurt Ahn
 */
public class WifiProcessor extends BroadcastReceiver {
	/**
	 * 
	 */
	private static OfflineWifiDB database = null;
	
	/**
	 * Process wifi scan results to produce WifiConnection objects,
	 * and then push the objects either to the context's buffer or
	 * to the server.
	 *
	 * @author Kurt Ahn
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// Create database for the first time.
		if (database == null) database = new OfflineWifiDB(context);
		
		WifiManager manager = (WifiManager) context.getSystemService(
				Context.WIFI_SERVICE);
		
		List<ScanResult> scans = manager.getScanResults();
		LatLng location = new LatLng(100, 100); // Temporary
		Date date = new Date(0); // Temporary
		String clientId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		
		ServerConnection server = ServerConnection.getInstance();
		if (manager.getConnectionInfo() != null /*Has internet?*/) {
			for (ScanResult s : scans) {
				// Create WifiConnection object based on scan.
				WifiConnection c = new WifiConnection(
						s, location, date, clientId);
				
				// Send data directly to server.
				try {
					server.pushNewConnection(c);
				} catch (ServerConnectionFailureException e) {
					System.err.println(
							"Failed to connect to server." +
							"Writing to database instead.");
					e.printStackTrace();
					database.push(c);
				}
			}
			
			while (!database.isEmpty()) {
				// Send WifiConnection object to server.
				try {
					WifiConnection next = database.pop();
					server.pushNewConnection(next);
				} catch (ServerConnectionFailureException e) {
					System.err.println(
							"Failed to connect to server." +
							"Connection data remains in database.");
					e.printStackTrace();
				}
			}
		} else {
			for (ScanResult s : scans) {
				// Create WifiConnection object based on scan.
				WifiConnection c = new WifiConnection(
						s, location, date, clientId);
				
				// Push object to database.
				database.push(c);
			}
		}
	}
}
