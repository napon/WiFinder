package com.napontaratan.wifi.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * BroadcastReceiver object triggered on:
 *	a) Wifi state change (enabled, disabled, etc.),
 *	b) Wifi connectivity state change, or
 *	c) Location change.
 */
public class WifiScanner extends BroadcastReceiver {
	private static final String TAG = 
			"com.napontaratan.wifi.controller.WifiScanner";
	
	/**
	 * Request wifi scan to WifiManager object of the context.
	 * @author Kurt Ahn
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Scanning...");

		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		wifiManager.startScan();
		
		if(wifi.isAvailable()) {
			WifiProcessor wifiProcessor = new WifiProcessor();
			IntentFilter onScanCompleted = new IntentFilter();
			onScanCompleted.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
			context.registerReceiver(wifiProcessor, onScanCompleted);
		}
	}
}
