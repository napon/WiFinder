package com.napontaratan.wifi.server;

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.location.Address;
import android.util.Log;

public class GeocodeServerConnection extends ServerConnection {
	private static final String TAG = 
			"com.napontaratan.wifi.server.GeocodeServerConnection";
	
	/**
	 * 
	 */
	public static final String GEOCODE_SCRIPT_URL = 
			"https://maps.googleapis.com/maps/api/geocode/json";
	
	/**
	 * 
	 */
	private GeocodeServerConnection() {}
	
	public static void getAddresses(String location,
			List<Address> addresses,
			List<String> formattedAddresses) {
		String response = null;
		
		try {
			response = sendGetRequest(GEOCODE_SCRIPT_URL,
					new Object[][] {{"address", location}});
			Log.d(TAG, "Response: " + response);
			
			JSONTokener jsonTokener = new JSONTokener(response);
			
			JSONObject jsonResponse = new JSONObject(jsonTokener);
			JSONArray jsonResults = jsonResponse.getJSONArray("results");
			for(int index = 0; index < jsonResults.length(); index++) {
				// json address object
				JSONObject jsonAddressObject = 
						jsonResults.getJSONObject(index);
				JSONArray addressComponentArray = 
						jsonAddressObject.getJSONArray(
								"address_components");
				String formattedAddress = jsonAddressObject.getString(
						"formatted_address");
				Log.d(TAG, "Address: " + formattedAddress);
				formattedAddresses.add(formattedAddress);
				JSONObject jsonLocation = 
						jsonAddressObject.getJSONObject("geometry").
						getJSONObject("location");
				// make android address object, storing properties for use later (lat.long, addressline)
				Address address = new Address(null);
				address.setLatitude(jsonLocation.getDouble("lat"));
				address.setLongitude(jsonLocation.getDouble("lng"));
				for(int innerIndex = 0; 
						innerIndex < addressComponentArray.length();
						++innerIndex) {
					JSONObject jsonAddressComponentObject = 
							addressComponentArray.
							getJSONObject(innerIndex);
					address.setAddressLine(innerIndex, 
							jsonAddressComponentObject.
							getString("short_name"));
				}
				addresses.add(address);
			}
		} catch (IOException e) {
			Log.e(TAG, "Failed HTTP request");
			Log.e(TAG, Log.getStackTraceString(e));
		} catch (JSONException e) {
			Log.e(TAG, "JSON parsing error");
			Log.e(TAG, Log.getStackTraceString(e));
		} 
	}
}
