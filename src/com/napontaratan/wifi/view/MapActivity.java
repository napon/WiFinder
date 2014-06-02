package com.napontaratan.wifi.view;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

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
	private Location myLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		setUpMap();
		setUpSearch();
	}

	/**
	 * Basic map view centered at Vancouver
	 * @author Napon Taratan
	 */
	private void setUpMap() {
		if(map == null) {
			System.out.println("map is null, setting it up");
			map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
			// hide zoom control so that it doesn't overlap the get current location button 
			// user can still zoom using pinch/release gesture
			map.getUiSettings().setZoomControlsEnabled(false); 
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
	
	/**
	 * Set up Buttons, Input event handler
	 * @author daniel
	 */
	private void setUpSearch() {
		// Search query input 
		final EditText searchInput = (EditText) findViewById(R.id.search_query);
		searchInput.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				searchInput.setCursorVisible(true);
			}
		});
		// Search button 
		ImageButton searchButton = (ImageButton) findViewById(R.id.search_button);
		searchButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				searchInput.setCursorVisible(true);
			}
		});
		// handle keyboard actions
		searchInput.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				String locationQuery = new String();
				if(actionId ==  EditorInfo.IME_ACTION_DONE){ // user presses 'done' button
					locationQuery = v.getText().toString();
				}
				searchInput.setCursorVisible(false);
				return false;
			}
		});
		// Clear button
		ImageButton clearSearch = (ImageButton) findViewById(R.id.clear_button);
		clearSearch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				searchInput.setText("");
			}
		});
		// Get current Location button 
		ImageButton getCurrentLocation = (ImageButton) findViewById(R.id.current_location_button);
		getCurrentLocation.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Sunny: implement get current location functionality
				myLocation = null;
				
			}
		});

				
	}
	
	
	
	
}