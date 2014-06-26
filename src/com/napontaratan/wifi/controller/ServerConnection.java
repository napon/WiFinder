package com.napontaratan.wifi.controller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import android.annotation.SuppressLint;
import android.net.Uri;
import android.net.http.HttpResponseCache;
import com.google.android.gms.maps.model.LatLng;
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
				WifiMarker marker = new WifiMarker(
								obj.getString("Name"), 
								"", 
								new LatLng(obj.getDouble("Latitude"), 
								obj.getDouble("Longitude")));
				markers.add(marker);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * PUSH
	 * Pushes a new WifiConnection to the web server
	 * 
	 * @param connection - WifiConnection to push
	 * @author Napon Taratan
	 * @throws ServerConnectionFailureException 
	 */
	public void pushNewConnection(WifiConnection connection) 
			throws ServerConnectionFailureException {
		String response = makeJSONQuery(
			WEBSERVER + "add_location.php?ssid=" + connection.ssid +
			"&signal=" + connection.strength +
			"&lat=" + connection.location.latitude +
			"&lon=" + connection.location.longitude +
			"&user=" + connection.clientId +
			"%date=" + connection.date
		);
		System.out.println(response);
	}
	
	/**
	 * Creates an HTTP request to the server and returns the server's response
	 * 
	 * @param server - url of request
	 * @author Napon Taratan
	 * @throws ServerConnectionFailureException 
	 */
	@SuppressLint("NewApi")
	public String makeJSONQuery(String server) 
			throws ServerConnectionFailureException {
		URL url = null;
		HttpURLConnection client = null;
		
		try {
			url = new URL(server); 
			System.out.println("make JSON query to server");
			client = (HttpURLConnection) url.openConnection();
			int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
			client.addRequestProperty("Cache-Control", "max-stale=" + maxStale); // try get cache response
//			System.out.println("cache request count: " + cache.getRequestCount());
//			System.out.println("cache hit count: " + cache.getHitCount() );
//			System.out.println("cache network count: "  + cache.getNetworkCount());
		} catch (MalformedURLException e) {
			throw new ServerConnectionFailureException(
					"Server address '" + server + "' is not valid.");
		} catch (IOException e) {
			throw new ServerConnectionFailureException(
					"Failed to connection to '" + server + "'.");
		}
		
		InputStream in = null;
		BufferedReader br = null;
		
		try { 
			in = client.getInputStream();
		} catch (IOException e) {
			// if no cache response or cache response fails
			try {
				client = (HttpURLConnection) url.openConnection();
				client.addRequestProperty("Cache-Control", "max-age=0");
				in = client.getInputStream();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
			
			e.printStackTrace();
		}
		
		try {
			
			br = new BufferedReader(
					new InputStreamReader(in));
			String current;
			StringBuilder r = new StringBuilder();
			System.out.println("Return: ");
			while ((current = br.readLine()) != null) { 
				System.out.println(current);
				r.append(current.trim());
			}
			return r.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		finally {
			client.disconnect();
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return "";
	}
}
