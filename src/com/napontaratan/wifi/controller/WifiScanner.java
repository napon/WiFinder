package com.napontaratan.wifi.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

/**
 * BroadcastReceiver object triggered on:
 *	a) Wifi state change (enabled, disabled, etc.),
 *	b) Wifi connectivity state change, or
 *	c) Location change.
 */
public class WifiScanner extends BroadcastReceiver {
	/**
	 * Request wifi scan to WifiManager object of the context.
	 *
	 * @author Kurt Ahn
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		((WifiManager) 
				context.getSystemService(Context.WIFI_SERVICE)).
				startScan();
	}
}
