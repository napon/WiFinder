package com.napontaratan.wifi.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import com.napontaratan.wifi.controller.ServerConnection;
import com.napontaratan.wifi.controller.ServerConnectionFailureException;
import com.napontaratan.wifi.model.WifiMarker;

public class MapActivity extends Activity {
	/*
	 * TODO FIX plotMarker() !!!!!
	 * TODO @PrestonChang: Fix the map around where user is in doInBackground()
	 */
	
	private static final LatLng VANCOUVER = new LatLng(49.22, -123.15);
	private GoogleMap map;
	private MapFragment mapFragment;
	private Location myLocation;
	private final Context currentActivityContext = this; // to be used when the context changes (eg. in an event handler)
	private EditText searchInput;
	private ImageButton searchButton;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		setUpMap();
		setUpSearch();
	}
	
	/**
	 * Manually handle back press event
	 * @author daniel
	 */
	@Override
	public void onBackPressed() {
		if(!searchInput.getText().equals("")){
			searchInput.setText("");
		}
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
		    
		    map.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {
				
				@Override
				public boolean onMyLocationButtonClick() {
					// TODO Sunny: implement location functionality 
					return false;
				}
			});
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
		map.addMarker(new MarkerOptions()
				.position(new LatLng(
				marker.getLocation().latitude,
				marker.getLocation().longitude))
				.title("SSID: " + marker.getSSID()));
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
			try {
				connection.parseJSONLocationData(
						connection.makeJSONQuery(url));
			} catch (ServerConnectionFailureException e) {
				e.printStackTrace();
			}
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
				searchInput.setText("");
			}
		});			
	} // END OF SET UP SEARCH 
	
	/**
	 * Geocode address using Google Geocoding API
	 * reference: http://wptrafficanalyzer.in/blog/android-geocoding-showing-user-input-location-on-google-map-android-api-v2/
	 * @author daniel
	 */
	private class GeocodeTask extends AsyncTask<String, Void, List<Address>> {

		private ProgressDialog dialog;
		
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
			Geocoder geocoder = new Geocoder(getApplicationContext());
			List<Address> addresses = new ArrayList<Address>();
			try {
				addresses = geocoder.getFromLocationName(locationName[0], 15);
			} catch (IOException e) {
				System.out.println("Error making Geocode api call");
				e.printStackTrace();
			}
			return addresses;
		}
		
		@Override
		protected void onPostExecute(List <Address> addresses) {
			if(addresses.size() == 0) {
				Toast.makeText(getApplicationContext(), "No location found", Toast.LENGTH_LONG).show();
			}
			
			System.out.println("number of address: " + addresses.size());
			// clear existing markers
			map.clear();
			
			List<String> addressesStrings = new ArrayList<String>();
			for(Address address: addresses) {
				
				LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
				int index = 0;
				String addressText = "";
				int addressLineLastIndex = address.getMaxAddressLineIndex();
				while(address.getAddressLine(index) != null) {
				
					addressText += (index != addressLineLastIndex) ? (address.getAddressLine(index) + ", ") 
								: (address.getAddressLine(index));
					index ++;
				}
				if(!addressText.equals("")) {
					addressesStrings.add(addressText);
				}
				System.out.println("address: " + addressText);
				System.out.println("latlng: " + address.getLatitude() + ", "  + address.getLongitude());
			}
			
			displayWifiSpot(addressesStrings);
			
			dialog.dismiss();
			
		}
		
	}// END OF GEOCODE TASK 
	
	
	// Helper class(es)/method(s)
	private class LocationArrayAdapter extends ArrayAdapter<String>{

		public LocationArrayAdapter(Context context, int resource) {
			super(context, resource);
			// TODO decide on the type of custom array adapter
		}
		
	}
	
	
	private void displayWifiSpot(List<String> searchResult) {
		// if only 1 result, go directly to make the api call for wifi spots around it 
		// else narrow it to 1 by displaying on list and letting user choose 

		displaySearchResult(searchResult);
	}
	
	
	private void displaySearchResult(List<String> resultsToDisplay) {
		ListView searchResultListView = (ListView) findViewById(R.id.search_result_list);
		searchResultListView.setAdapter(new ArrayAdapter<String>(currentActivityContext, R.layout.search_result_item, resultsToDisplay));
		// show/hide result layout (white background, listview, current location button) on back button, on search againsch
		showResultOverlay();
	}
	
	private void clearResultOverlay(){		
		findViewById(R.id.search_background).setVisibility(View.GONE);
		findViewById(R.id.search_result_list).setVisibility(View.GONE);
		map.setMyLocationEnabled(true);
	}
	
	private void showResultOverlay(){
		findViewById(R.id.search_background).setVisibility(View.VISIBLE);
		findViewById(R.id.search_result_list).setVisibility(View.VISIBLE);
		map.setMyLocationEnabled(false);
	}
	
	private boolean isResultOverlayShown(){
		return !map.isMyLocationEnabled() && (findViewById(R.id.search_background).getVisibility() == View.VISIBLE) && (findViewById(R.id.search_result_list).getVisibility() == View.VISIBLE);
	}
	
	// =============== END OF SEARCH ===========================================
	
	
	
	
	
	
}