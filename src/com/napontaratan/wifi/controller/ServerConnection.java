package com.napontaratan.wifi.controller;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpConnection;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.net.Uri;
import android.net.http.HttpResponseCache;
import android.os.Build;
import android.util.Log;
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
			WEBSERVER + "add_location.php?ssid=" + encode(connection.ssid) +
			"&signal=" + convertToBarLevel(connection.strength) +
			"&lat=" + connection.location.latitude +
			"&lon=" + connection.location.longitude +
			"&user=" + connection.clientId +
			"&date=" + connection.date);
	}
	
	/**
	 * Maps dBm signal strength to a readable 1-5 scale
	 * source: http://www.anandtech.com/show/3821/iphone-4-redux-analyzing-apples-ios-41-signal-fix
	 * @param strength - dBm value
	 * @return int[1,5]
	 * @author Napon Taratan
	 */
	private String convertToBarLevel(int strength) {
		if(strength >= -91) {
			return "5";
		} else if(strength >= -101) {
			return "4";
		} else if(strength >= -103) {
			return "3";
		} else if(strength >= -107) {
			return "2";
		} else return "1";
	}
	
	/**
	 * Encodes url to be database friendly
	 * @param url - raw url
	 * @return url with spaces replaced with +
	 * @author Napon Taratan
	 */
	private String encode(String url) {
		return url.replace(' ', '+');
	}

	/**
	 * Creates an HTTP request to the server and returns the server's response
	 * 
	 * @param server - url of request
	 * @author Napon Taratan
	 * @throws ServerConnectionFailureException 
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public String makeJSONQuery(String server) throws ServerConnectionFailureException {
//		String responseString = null;
		HttpURLConnection urlConnection = null;
		StringBuilder response = new StringBuilder();
		
		
		try {
			URL url = new URL(server);
			urlConnection =  (HttpURLConnection) url.openConnection();
			urlConnection.setUseCaches(true);
			int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
			urlConnection.addRequestProperty("Cache-Control", "max-stale=" + maxStale);
			System.out.println("response message: " + urlConnection.getResponseMessage());
			System.out.println("header field: " + urlConnection.getHeaderFields());
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				HttpResponseCache cache = HttpResponseCache.getInstalled();
				System.out.println("cache request count: " + cache.getRequestCount());
				System.out.println("cache hit count: " + cache.getHitCount() );
				System.out.println("cache network count: "  + cache.getNetworkCount());
			}			
			BufferedReader r = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			String line;
			while ((line = r.readLine()) != null) {
				response.append(line);
			}
			r.close();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			urlConnection.disconnect();;
		}
		return response.toString();
        
//        try {
//            System.out.println("make JSON query to server \n " + server);
//            HttpClient httpClient = new DefaultHttpClient();
//            HttpGet httpGet = new HttpGet(server);
//            HttpResponse response = httpClient.execute(new HttpGet(server));
//            responseString = new BasicResponseHandler().handleResponse(response);
//            System.out.println(responseString);
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new ServerConnectionFailureException();
//        }

//        return responseString;

	}
}
