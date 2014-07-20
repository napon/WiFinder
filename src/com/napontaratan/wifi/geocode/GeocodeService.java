package com.napontaratan.wifi.geocode;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.napontaratan.wifi.server.ServerConnection;
import com.napontaratan.wifi.server.ServerConnectionFailureException;

/**
 * Geocoding service using Client-side geocoding (Android Geocoder) and fall back on Server-side geocoding (Google Geocoding API) 
 * @author daniel
 * https://developers.google.com/maps/articles/geocodestrat
 */
public class GeocodeService {
	private Context context;
	private List<Address> addresses;
	private List<String> addressesStrings;
	private String GOOGLE_GEOCODE_ENDPOINT = "https://maps.googleapis.com/maps/api/geocode/json?";
	private String GOOGLE_API_KEY = ""; // TODO: your api key here
	private ServerConnection serverConnection;
	private String DEBUG_TAG = "GEOCODE SERVICE";
	private Boolean useGoogleFormattedAddress = false;
	
	public GeocodeService(Context context) {
		this.context = context;
		serverConnection = ServerConnection.getInstance();
	}
	
	/**
	 * Get the address from a location name using geocoding
	 * @param location name of the location to search for
	 * @return a list of addresses for the location searched using android's geocoder
	 */
	public List<Address> getAddresses(String location) {
		//TODO: if location is null throw exception to warn user
		//first try Android Geocoder
		//fall back to Google Geocoding API
		getAddressFromAndroidGeocoder(location);
		if(!hasAddress(addresses)) {
			getAddressFromGoogleGeocodingAPI(location);
		}
		return addresses;
	}
	
	/**
	 * Get list of possible address (0 or more) for a location using Android Geocoder,  stores list of address 
	 * @param location name of the location to search for
	 */
	private void getAddressFromAndroidGeocoder(String location) {
		useGoogleFormattedAddress = false;
		Geocoder geocoder = new Geocoder(context);
		addresses = new ArrayList<Address>();
		addressesStrings = new ArrayList<String>();
		try {
			addresses = geocoder.getFromLocationName(location, 15);
		} catch (IOException e) {
			Log.d(DEBUG_TAG, "Error making Geocode api call");
			e.printStackTrace();
		}
	}
	
	/**
	 * get list of possible address (0 or more) for a location using Google Geocoding API
	 * @param location  name of the location to search for
	 * https://developers.google.com/maps/documentation/geocoding/#GeocodingRequests
	 */
	private void getAddressFromGoogleGeocodingAPI(String location) {
		useGoogleFormattedAddress = true;
		addresses = new ArrayList<Address>();
		addressesStrings = new ArrayList<String>();
		// form url query 
		// sent request using makeJSONQuery in server connection class
		// parse query to get list of address
		String queryUrl;
		String response = null;
		try {
			queryUrl = GOOGLE_GEOCODE_ENDPOINT + "address=" + URLEncoder.encode(location, "UTF-8");
			response = serverConnection.makeJSONQuery(queryUrl);
			Log.d(DEBUG_TAG, response);
		} catch (UnsupportedEncodingException e) {
			Log.d(DEBUG_TAG,"fail to encode");
			e.printStackTrace();
		} catch (ServerConnectionFailureException e) {
			Log.d(DEBUG_TAG,"fail to make query to google geocode api");
			e.printStackTrace();
		}
		parseGeocodeApiResponse(response);										
	}
	
	/**
	 * Parse json response from google geocoding API, stores list of address and addressStrings 
	 * @param response
	 */
	private void parseGeocodeApiResponse(String response) {

		JSONTokener jsonTokener = new JSONTokener(response);
		try {
			JSONObject jsonResponse = new JSONObject(jsonTokener);
			JSONArray results = jsonResponse.getJSONArray("results");
			for(int index = 0; index < results.length(); index++) {
				// json address object
				JSONObject jsonAddressObject = results.getJSONObject(index);
				JSONArray addressComponentArray = jsonAddressObject.getJSONArray("address_components");
				String formattedAddress = jsonAddressObject.getString("formatted_address");
				Log.d(DEBUG_TAG, formattedAddress);
				addressesStrings.add(formattedAddress);
				JSONObject location = jsonAddressObject.getJSONObject("geometry").getJSONObject("location");
				// make android address object, storing properties for use later (lat.long, addressline)
				Address address = new Address(null);
				address.setLatitude(location.getDouble("lat"));
				address.setLongitude(location.getDouble("lng"));
				for(int innerIndex = 0; innerIndex < addressComponentArray.length(); innerIndex ++) {
					JSONObject jsonAddressComponentObject = addressComponentArray.getJSONObject(innerIndex);
					address.setAddressLine(innerIndex, jsonAddressComponentObject.getString("short_name"));
				}
				addresses.add(address);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.d(DEBUG_TAG, "json parsing error");
			e.printStackTrace();
		}
	}
	
	/**
	 * Format addresses into list of strings and stores them
	 */
	public List<String> formatAddressToStrings() {
		// 2) using google formatted address and the formatted address exist
		if(addresses == null || (useGoogleFormattedAddress && (addressesStrings.size() > 0)) ) {
			return addressesStrings;
		}
		// format each address into one-line String
		for(Address address: addresses) {
			int index = 0;
			String addressText = "";
			int addressLineLastIndex = address.getMaxAddressLineIndex();
			while(address.getAddressLine(index) != null) {
				addressText += (index != addressLineLastIndex) ? 
						(address.getAddressLine(index) + ", ") : (address.getAddressLine(index));
				index ++;
			}
			if(!addressText.equals("")) {
				addressesStrings.add(addressText);
			}
		}		
		return addressesStrings;
	}
	
	
	/**
	 * Check if the list contains address(es)
	 * @param addresses the list of address to check
	 * @return true if list contains address(es), false otherwise
	 */
	private boolean hasAddress(List<Address> addresses) {
		return (addresses != null) && (addresses.size() > 0);
	}
	

}
