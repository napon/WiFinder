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
		LatLng location = null;
		Date date = null;
		int clientId = 0; // Where do we get this stuff??
		
		if (manager.getConnectionInfo() != null /*connected?*/
				/*&& succeeded to connect to server?*/) {
			
			for (ScanResult s : scans) {
				// Create WifiConnection object based on scan.
				WifiConnection c = new WifiConnection(
						s, location, date, clientId);
				
				// Send data directly to server.
				ServerConnection.getInstance().pushNewConnection(c);
			}
			
			{	// WHILE database is not empty,
				
				// Send WifiConnection object to server.
				// Remove object from buffer.
			}
		} {	// ELSE,
			
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
