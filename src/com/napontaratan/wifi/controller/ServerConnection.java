package com.napontaratan.wifi.controller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.napontaratan.wifi.model.WifiConnection;
import com.napontaratan.wifi.model.WifiMarker;

/**
 * Establish connection with the remote server for
 * - fetch wifi points
 * - push new wifi points
 *  
 * @author Napon Taratan
 */
public class ServerConnection {
	private static ServerConnection instance = null;
	public final String WEBSERVER = "http://www.napontaratan.com/wifinder/";

	final List<WifiMarker> markers = new ArrayList<WifiMarker>();

	// allow only one instance of WiFinderServerConnection
	private ServerConnection() {}

	public static ServerConnection getInstance() {
		if(instance == null)
			instance = new ServerConnection();
		return instance;
	}

	public List<WifiMarker> getWifiMarkers(){
		return markers;
	}
	
	/**
	 * FETCH
	 * Parses the JSON response and populates the list
	 * 
	 * @param response
	 * @author Napon Taratan
	 */
	public void parseJSONLocationData(String response){
		try {
			JSONTokener raw 	= new JSONTokener(response);
			JSONArray jsArray	= new JSONArray(raw);
			for(int i = 0; i < jsArray.length(); i++) {
				JSONObject obj = (JSONObject) jsArray.get(i);
				WifiMarker marker = 
						new WifiMarker(
								obj.getString("Name"), 
								obj.getInt("SignalStrength"), 
								obj.getDouble("Latitude"), 
								obj.getDouble("Longitude"));
				markers.add(marker);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * PUSH
	 * Pushes a new Wifi location to the web server
	 * 
	 * @param wc - WifiConnection
	 * @author Napon Taratan
	 */
	public void pushNewLocation(WifiConnection wc){
		
		String ssid = wc.getSSID();
		int sigStrength = wc.getSignalStrength();
		String dateDiscovered = wc.getDate().toString();
		int user = wc.getClientId();
		double lat = wc.getLocation().latitude;
		double lon = wc.getLocation().longitude;
		
		String url = WEBSERVER + "add_location.php?ssid=" + ssid + 
												"&signal=" + sigStrength +
												"&lat=" + lat +
												"&lon=" + lon +
												"&user=" + user +
												"&date=" + dateDiscovered;
		
		String response = makeJSONQuery(url);
		System.out.println(response);
		
	}
	
	/**
	 * Creates an HTTP request to the server and returns the server's response
	 * 
	 * @param server - url of request
	 * @author Napon Taratan
	 */
	public String makeJSONQuery(String server) {
		try {
			System.out.println("make JSON query to server");
			URL url = new URL(server);
			HttpURLConnection client = (HttpURLConnection) url.openConnection();
			InputStream in = client.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String returnString = br.readLine();
			client.disconnect();
			System.out.println("return is " + returnString);
			return returnString;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
