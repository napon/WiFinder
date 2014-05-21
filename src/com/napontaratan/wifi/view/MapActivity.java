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
	 * @author napontaratan
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
	 * 
	 * @author napontaratan
	 * @param marker - Marker to plot on the map
	 */
	private void plotMarker(WifiMarker marker) {
		map.addMarker(new MarkerOptions()
		.position(new LatLng(
				marker.location.latitude,
				marker.location.longitude))
		.title("SSID: " + marker.ssid)
		.snippet("Signal Strength: " + marker.strength));
	}

	/**
	 * Create a single thread to fetch locations from the database and plot them on the map
	 * @author napontaratan
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
			String jsonResponse = connection.makeJSONQuery(connection.WEBSERVER);
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