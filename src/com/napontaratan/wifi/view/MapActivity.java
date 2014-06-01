package com.napontaratan.wifi.view;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.napontaratan.wifi.R;
import com.napontaratan.wifi.controller.ServerConnection;
import com.napontaratan.wifi.model.WifiMarker;

public class MapActivity extends Activity {
	/*
	 * TODO FIX plotMarker() !!!!!
	 * TODO @PrestonChang: Fix the map around where user is in doInBackground()
	 */
	
	private static final LatLng VANCOUVER = new LatLng(49.22, -123.15);
	private GoogleMap map;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		setUpMap();
	}

	/**
	 * Basic map view centered at Vancouver
	 * @author Napon Taratan
	 */
	private void setUpMap() {
		if(map == null) {
			System.out.println("map is null, setting it up");
			map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		}	

		map.moveCamera(CameraUpdateFactory.newLatLng(VANCOUVER));
		map.animateCamera(CameraUpdateFactory.zoomTo(11));

		new GetLocationsTask(this).execute();
	}

	/**
	 * Plot a marker on the map view
	 * @param marker - Marker to plot on the map
	 * @author Napon Taratan
	 */
	private void plotMarker(WifiMarker marker) {
		//map.addMarker(new MarkerOptions()
		//.position(new LatLng(
		//		marker.getLocation().latitude,
		//		marker.getLocation().longitude))
		//		.title("SSID: " + marker.getSSID())
		//		.snippet("Signal Strength: " + marker.getSignalStrength()));
	}

	/**
	 * Create a single thread to fetch locations based on the user input and plot them on the map
	 * Also start a loading wheel to work in progress
	 * 
	 * @author Napon Taratan
	 */
	private class GetLocationsTask extends AsyncTask<String, Void, Void>  {

		private ProgressDialog dialog;

		private ServerConnection connection = 
				ServerConnection.getInstance();

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
		protected Void doInBackground(String ...s) {
			String url = connection.WEBSERVER + "locations.php?lat=49.263604&lon=-123.247805&rad=3"; // sample code for the time being
			String jsonResponse = connection.makeJSONQuery(url);
			connection.parseJSONLocationData(jsonResponse);
			return null;
		}

		// plot the locations on the map
		@Override
		protected void onPostExecute(Void v) {
			List<WifiMarker> markers = connection.getWifiMarkers();
			for(WifiMarker m : markers){
				plotMarker(m);
			}

			System.out.println("*** NUMBER OF LOCATIONS = " + markers.size() + " ***");
			dialog.dismiss();
		}
	}
}