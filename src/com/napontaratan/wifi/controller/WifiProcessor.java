package com.napontaratan.wifi.controller;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.Settings.Secure;
import android.text.format.Time;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.napontaratan.wifi.database.OfflineBuffer;
import com.napontaratan.wifi.model.WifiConnection;
import com.napontaratan.wifi.server.DatabaseServerConnection;
import com.napontaratan.wifi.server.ServerConnectionFailureException;

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
	private static final String TAG = 
			"com.napontaratan.wifi.controller.WifiProcessor";
	
	/**
	 * 
	 */
	private static OfflineBuffer buffer = null;
	
	/**
	 * Process wifi scan results to produce WifiConnection objects,
	 * and then push the objects either to the context's buffer or
	 * to the server.
	 *
	 * @author Kurt Ahn
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "in process");
		new ProcessWifis().execute(context);
		
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		SupplicantState supplicantState = wifiInfo.getSupplicantState();
		if(supplicantState.equals(SupplicantState.COMPLETED))
			context.unregisterReceiver(this);
	}
	
	/**
	 * Android can't perform networking operations on the main thread
	 * so I created an AsyncTask to handle that - Napon
	 */
	private class ProcessWifis extends AsyncTask<Context,Void,Void> {
		@Override
		protected Void doInBackground(Context... params) {
			Context context = params[0];
			
			// Create database for the first time.
			if (buffer == null) buffer = new OfflineBuffer(context);
			
			WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			
			List<ScanResult> scans = manager.getScanResults();
			Log.d(TAG, "Found this many wifis: " + scans.size());
			
			LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if(currentLocation == null) currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			
			LatLng location = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
			
			Time now = new Time();
			now.setToNow();
			Date date = new Date(now.toMillis(true));
			
			String clientId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
			
			// Check if the device is connected to the Internet.
			if (manager.getConnectionInfo() != null) {
				for (ScanResult s : scans) {
					// Create WifiConnection object based on scan.
					WifiConnection c = 
							WifiConnection.createWifiConnection(
									s, location, date, clientId);
					
					// Try to send the data straight to the server.
					try {
						DatabaseServerConnection.addWifiConnection(c);
					} catch (IOException e) {
						Log.d(TAG,
								"Failure to push to the server; " +
								"Pushing to the local buffer instead.");
						e.printStackTrace();
						buffer.push(c);
						return null;
					}
				}
				
				while (!buffer.isEmpty()) {
					// Send WifiConnection object to server.
					try {
						WifiConnection next = buffer.pop();
						DatabaseServerConnection.addWifiConnection(next);
					} catch (IOException e) {
						Log.e(TAG,
								"Failed to connect to server." +
								"Connection data remains in database.");
						e.printStackTrace();
					}
				}
			} else {
				for (ScanResult s : scans) {
					// Create WifiConnection object based on scan.
					WifiConnection c = 
							WifiConnection.createWifiConnection(
									s, location, date, clientId);
					
					// Push object to database.
					buffer.push(c);
				}
			}
			return null;
		}	
	}
}
