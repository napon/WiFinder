package com.napontaratan.wifi.controller;

import java.io.File;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentFilter;
import android.net.http.HttpResponseCache;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.napontaratan.wifi.model.WifiMarker;
import com.napontaratan.wifi.server.DatabaseServerConnection;
import com.napontaratan.wifi.view.MapActivity;

public class WifiController {
	private static final String TAG = 
			"com.napontaratan.wifi.controller.WifiController";
	
	/**
	 * 
	 */
	private final MapActivity activity;
	
	/**
	 * 
	 * @param activity
	 */
	public WifiController(MapActivity activity) {
		this.activity = activity;
	}

	/**
	 * Register wifi scan and process receivers
	 * @author Napon Taratan
	 */
	public void registerReceivers() {
		Log.d(TAG, "checked");
		WifiScanner wifiScanner = new WifiScanner();
		IntentFilter onNewWifiDiscovered = new IntentFilter();
		onNewWifiDiscovered.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		activity.registerReceiver(wifiScanner, onNewWifiDiscovered);
		
		WifiProcessor wifiProcessor = new WifiProcessor();
		IntentFilter onScanCompleted = new IntentFilter();
		onScanCompleted.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		activity.registerReceiver(wifiProcessor, onScanCompleted);
	}
	
	/**
	 * Android 4.0 added a response cache to HttpURLConnection. You can enable HTTP response caching on supported devices using reflection as follows
	 * http://developer.android.com/training/efficient-downloads/redundant_redundant.html
	 */
	public void enableHttpResponseCache() {
		  try {
		    long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
		    File httpCacheDir = new File(activity.getCacheDir(), "http");
		    Class.forName("android.net.http.HttpResponseCache")
		         .getMethod("install", File.class, long.class)
		         .invoke(null, httpCacheDir, httpCacheSize);
		  } catch (Exception e) {
		    Log.d(TAG, "HTTP response cache is unavailable.");
		  }
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void flushHttpResponseCache() {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			// Flushing the cache forces its data to the filesystem. This ensures that all responses written to the cache will be readable the next time the activity starts.
			HttpResponseCache cache = HttpResponseCache.getInstalled();
			if(cache != null) {
				cache.flush();
			}
		}
	}
	
	/**
	 * 
	 * @param context
	 * @return
	 */
	public GetLocationsTask createGetLocationsTask(Context context) {
		return new GetLocationsTask(context);
	}
	
	/**
	 * Create a single thread to fetch locations based on the user input and plot them on the map
	 * Also start a loading wheel to work in progress
	 * 
	 * @author Napon Taratan
	 */
	public class GetLocationsTask extends AsyncTask<LatLng, Void, Void>  {

		private ProgressDialog dialog;

		private WifiMarker marker;
		
		public GetLocationsTask(Context c){
			dialog = new ProgressDialog(c);
		}

		// show the spinning loading wheel for style points
		@Override
		protected void onPreExecute() {
			dialog.setMessage("Retrieving WiFi locations...");
			dialog.show(); 
		}

		// create web request and parse the response
		@Override
		protected Void doInBackground(LatLng ... locations) {
			marker = DatabaseServerConnection.getWifiMarker(locations[0]);
			return null;
		}

		// plot the locations on the map
		@Override
		protected void onPostExecute(Void v) {
			activity.plotMarker(marker);
			dialog.dismiss();
		}
	}
}