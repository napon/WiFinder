package com.napontaratan.wifi.geocode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;

/**
 * Geocoding service using Google Geocoding API 
 * @author daniel
 * https://developers.google.com/maps/articles/geocodestrat
 */

public class GeocodeService {
	private Context context;
	private List<Address> addresses;
	
	public GeocodeService(Context context) {
		this.context = context;
	}
	
	/**
	 * Get the address from a location name using geocoding
	 * @param location name of the location to search for
	 * @return a list of addresses for the location searched using android's geocoder
	 */
	public List<Address> getAddresses(String location) {
		//first try Android Geocoder
		//fall back to Google Geocoding API
		getAddressFromAndroidGeocoder(location);
		if(!hasAddress(addresses)) {
			getAddressFromGoogleGeocodingAPI(location);
		}
		return addresses;
	}
	
	/**
	 * Get list of possible address (0 or more) for a location using Android Geocoder
	 * @param location name of the location to search for
	 */
	private void getAddressFromAndroidGeocoder(String location) {
		Geocoder geocoder = new Geocoder(context);
		addresses = new ArrayList<Address>();
		try {
			addresses = geocoder.getFromLocationName(location, 15);
		} catch (IOException e) {
			System.out.println("Error making Geocode api call");
			e.printStackTrace();
		}

	}
	
	/**
	 * get list of possible address (0 or more) for a location using Google Geocoding API
	 * @param location  name of the location to search for
	 * https://developers.google.com/maps/documentation/geocoding/#GeocodingRequests
	 */
	private void getAddressFromGoogleGeocodingAPI(String location) {
		addresses = new ArrayList<Address>();
		// form url query 
		// sent request using makeJSONQuery in server connection class
		// parse query to get list of address
	}
	
	/**
	 * Format addresses into list of strings
	 * @param addresses
	 * @return formatted address as a list of strings
	 */
	public List<String> formatAddressToStrings(List<Address> addresses) {
		List<String> addressesStrings = new ArrayList<String>(); 
		// format addresses into Strings
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
