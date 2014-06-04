package com.napontaratan.wifi.controller;

import java.sql.Date;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

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
		int clientId = 0; // Where do we get this stuff??
		
		ServerConnection server = null;
		if (manager.getConnectionInfo() != null && /*Has internet?*/
				(server = ServerConnection.getInstance()) != null /*Connected to server?*/) {
			for (ScanResult s : scans) {
				// Create WifiConnection object based on scan.
				WifiConnection c = new WifiConnection(
						s, location, date, clientId);
				
				// Send data directly to server.
				server.pushNewConnection(c);
			}
			
			while (!database.isEmpty()) {
				WifiConnection next = database.next();
				
				// Send WifiConnection object to server.
				server.pushNewConnection(next);
				
				// Remove object from buffer.
				database.remove(next);
			}
		} else {
			for (ScanResult s : scans) {
				// Create WifiConnection object based on scan.
				WifiConnection c = new WifiConnection(
						s, location, date, clientId);
				
				// Push object to database.
				database.addToDB(c);
			}
		}
	}
}
