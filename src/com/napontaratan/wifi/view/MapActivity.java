package com.napontaratan.wifi.view;

import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.napontaratan.wifi.R;
import com.napontaratan.wifi.controller.LocationServices;
import com.napontaratan.wifi.controller.WifiController;
import com.napontaratan.wifi.geocode.GeocodeService;
import com.napontaratan.wifi.model.WifiMarker;

/*
 * TODO
 * Extract some stuff to WifiController
 */
public class MapActivity extends Activity {

	private static final LatLng VANCOUVER = new LatLng(49.22, -123.15);
	private GoogleMap map;
	private MapFragment mapFragment;
	private final Context currentActivityContext = this; // to be used when the context changes (eg. in an event handler)
	private EditText searchInput;
	private ImageButton searchButton;
	private LocationServices locationServices; 
	private LatLng myLatLng;

	private WifiController controller;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		controller = new WifiController(this);
		
		controller.enableHttpResponseCache();
		setUpMap();
		setUpSearch();
		locationServices = new LocationServices(this);
		controller.registerReceivers();
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	protected void onStop() {
		super.onStop();
		// Flushing the cache forces its data to the filesystem. This ensures that all responses written to the cache will be readable the next time the activity starts.
		HttpResponseCache cache = HttpResponseCache.getInstalled();
		if(cache != null) {
			cache.flush();
		}
	}
	
	/**
	 * Manually handle back press event
	 * @author daniel
	 */
	@Override
	public void onBackPressed() {
		if(isResultOverlayShown()){
			clearResultOverlay();
		}else {
			super.onBackPressed();
		}
	
	}

	// ========= START OF MAP ===============================================		
	/**
	 * Basic map view centered at Vancouver
	 * @author Napon Taratan
	 */
	private void setUpMap() {
		if(map == null) {
			System.out.println("map is null, setting it up");
			mapFragment= ((MapFragment) getFragmentManager().findFragmentById(R.id.map));
			map = mapFragment.getMap();
			// hide zoom control so that it doesn't overlap the get current location button 
			// user can still zoom using pinch/release gesture
			map.getUiSettings().setZoomControlsEnabled(false); 
			map.setMyLocationEnabled(true);
			
			// Get the button view 
			// http://blog.kozaxinan.com/2013/08/how-to-change-position-of.html
		    View locationButton = getFragmentManager().findFragmentById(R.id.map).getView().findViewById(0x2);
		    // and next place it, for exemple, on bottom right (in Google Maps app)
		    RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
		    // position on right bottom
		    rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
		    rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		    rlp.setMargins(0, 0, 30, 30);
		    
		    // using my location
		    map.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {
				
				@Override
				public boolean onMyLocationButtonClick() {
					Location myLocation = locationServices.getLocation();
					if(myLocation != null) {
						myLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
						controller.createGetLocationsTask(currentActivityContext).execute(myLatLng);
					} else {
						Toast.makeText(currentActivityContext, R.string.my_location_unavailable_msg, Toast.LENGTH_LONG).show();
						// open up location settings
						// http://stackoverflow.com/questions/7713478/how-to-prompt-user-to-enable-gps-provider-and-or-network-provider
						Intent gpsOptionsIntent = new Intent(  
							    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
						startActivity(gpsOptionsIntent);
					}
					
					return false;
				}
			});
		}	

		map.moveCamera(CameraUpdateFactory.newLatLng(VANCOUVER));
		map.animateCamera(CameraUpdateFactory.zoomTo(11));
		
		// Display the client ID when the map is loaded (for testing purposes)
		Toast.makeText(getApplicationContext(), "Client ID: " + Secure.getString(this.getContentResolver(), Secure.ANDROID_ID), Toast.LENGTH_SHORT).show();
	}

	/**
	 * Plot a marker on the map view
	 * @param marker - Marker to plot on the map
	 * @author Napon Taratan
	 */
	public void plotMarker(WifiMarker marker) {
		map.addMarker(new MarkerOptions().position(
				new LatLng(
						marker.location.latitude,
						marker.location.longitude)
				));
	}

	// =================  END OF MAP ==================================
	
	// ================= START OF SEARCH ===================================
	
	/**
	 * Set up Buttons, Input event handler
	 * @author daniel
	 */
	private void setUpSearch() {
		// Search query input 
		searchInput = (EditText) findViewById(R.id.search_query);
		searchInput.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				searchInput.setCursorVisible(true);
			}
		});
		// Search button 
		searchButton = (ImageButton) findViewById(R.id.search_button);
		searchButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				searchInput.setCursorVisible(true);
				// bring up keyboard 
				// http://stackoverflow.com/questions/5105354/how-to-show-soft-keyboard-when-edittext-is-focused
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
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
					new GeocodeTask(currentActivityContext).execute(locationQuery); 
				}
				searchInput.setCursorVisible(false);
				return false;
			}
		});
		// Clear button
		ImageButton clearSearchButon = (ImageButton) findViewById(R.id.clear_button);
		clearSearchButon.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				clearResultOverlay();
			}
		});			
	} // END OF SET UP SEARCH 
	
	/**
	 * Geocode address using Google Geocoding API to get latitude longitude for wifi api call, and plot nearby Wifi spots around the location on map
	 * reference: http://wptrafficanalyzer.in/blog/android-geocoding-showing-user-input-location-on-google-map-android-api-v2/
	 * @author daniel
	 */
	private class GeocodeTask extends AsyncTask<String, Void, List<Address>> {

		private ProgressDialog dialog;
		private GeocodeService geocodeService;
		
		public GeocodeTask(Context cxt) {
			dialog = new ProgressDialog(cxt);
		}
		
		@Override
		protected void onPreExecute() {
			dialog.setMessage("Retrieving Wifi Location");
			dialog.show();
		}
		
		@Override
		protected List<Address> doInBackground(String... locationName) {
			geocodeService = new GeocodeService(getApplicationContext());
			return geocodeService.getAddresses(locationName[0]);
		}
		
		@Override
		protected void onPostExecute(List<Address> addresses) {
			if(addresses.size() == 0) {
				Toast.makeText(getApplicationContext(), "No location found", Toast.LENGTH_LONG).show();
				return;
			}
			
			// clear existing markers
			map.clear();
			displayWifiSpotMarkers(addresses, geocodeService);
			dialog.dismiss();
			
		}
		
	}// END OF GEOCODE TASK 
	
	
	// Helper class(es)/method(s)
	/**
	 * Plot Wifi markers on map 
	 * @param addresses
	 * @author daniel
	 */
	private void displayWifiSpotMarkers(final List<Address> addresses, GeocodeService geocodeService) {
		// display formatted addresses from search results on list view 
		List<String> addressesStrings = geocodeService.formatAddressToStrings();
		// search result list view
		ListView searchResultListView = (ListView) findViewById(R.id.search_result_list);
		searchResultListView.setAdapter(new ArrayAdapter<String>(currentActivityContext, R.layout.search_result_item, addressesStrings));
		searchResultListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adpaterView, View view, int index,
					long id) {
				// Sets the chosen location's latitude and longitude
				Address addressSelected = addresses.get(index);
				myLatLng = new LatLng(addressSelected.getLatitude(), addressSelected.getLongitude());
				// get wifi locations
				controller.createGetLocationsTask(currentActivityContext).execute(myLatLng);
				clearResultOverlay();
				//TODO pushWifiLocation using myLatLng
			}
		});
		showResultOverlay();

	}
	
	/**
	 * helper method to show geocoded addresses on list view
	 * @author daniel
	 */
	private void showResultOverlay(){
		findViewById(R.id.search_background).setVisibility(View.VISIBLE);
		findViewById(R.id.search_result_list).setVisibility(View.VISIBLE);
		map.setMyLocationEnabled(false);
	}
	
	/**
	 * clear the list view
	 * @author daniel
	 */
	private void clearResultOverlay(){		
		if(!searchInput.getText().equals("")){
			searchInput.setText("");
		}
		findViewById(R.id.search_background).setVisibility(View.GONE);
		findViewById(R.id.search_result_list).setVisibility(View.GONE);
		map.setMyLocationEnabled(true);
	}
	
	/**
	 * determine if the list view containing geocoded addresses is currently visible
	 * @return true if the list view containing geocoded adresses is shown, false otherwise
	 * @author daniel
	 */
	private boolean isResultOverlayShown(){
		return !map.isMyLocationEnabled() && 
				(findViewById(R.id.search_background).getVisibility() == View.VISIBLE) && 
				(findViewById(R.id.search_result_list).getVisibility() == View.VISIBLE);
	}
	
}