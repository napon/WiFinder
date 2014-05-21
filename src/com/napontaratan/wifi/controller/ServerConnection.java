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

import com.napontaratan.wifi.model.WifiMarker;

public class ServerConnection {
	private static ServerConnection instance = null;
	public final String WEBSERVER = "http://www.napontaratan.com/wifinder/locations.php";

	final List<WifiMarker> markers = new ArrayList<WifiMarker>();

	// allow only one instance of WiFinderServerConnection
	private ServerConnection() {}

	public static ServerConnection getInstance() {
		if(instance == null)
			instance = new ServerConnection();
		return instance;
	}

	// parses JSON string and populates the list
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

	public List<WifiMarker> getWifiMarkers(){
		return markers;
	}

	// make http request and return the response string
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
