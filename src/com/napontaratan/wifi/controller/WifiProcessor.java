package com.napontaratan.wifi.controller;

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

import com.google.android.gms.maps.model.LatLng;
import com.napontaratan.wifi.database.OfflineBuffer;
import com.napontaratan.wifi.model.WifiConnection;
import com.napontaratan.wifi.server.ServerConnection;
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
	private static OfflineBuffer database = null;
	
	/**
	 * Process wifi scan results to produce WifiConnection objects,
	 * and then push the objects either to the context's buffer or
	 * to the server.
	 *
	 * @author Kurt Ahn
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		
		System.out.println("in process");
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
			if (database == null) database = new OfflineBuffer(context);
			
			WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			
			List<ScanResult> scans = manager.getScanResults();
			System.out.println("found this many wifis: " + scans.size());
			
			LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			Location currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if(currentLocation == null) currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			
			LatLng location = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
			
			Time now = new Time();
			now.setToNow();
			Date date = new Date(now.toMillis(true));
			
			String clientId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
			
			ServerConnection server = ServerConnection.getInstance();
			if (manager.getConnectionInfo() != null /*Has internet?*/) {
				for (ScanResult s : scans) {
					// Create WifiConnection object based on scan.
					WifiConnection c = new WifiConnection(
							s, location, date, clientId);
					
					// Send data directly to server.
					try {
						server.addWifiConnection(c);
					} catch (ServerConnectionFailureException e) {
						System.out.println(
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
						server.addWifiConnection(next);
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
			return null;
		}	
	}
}
