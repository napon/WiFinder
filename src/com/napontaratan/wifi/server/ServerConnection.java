package com.napontaratan.wifi.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.annotation.TargetApi;
import android.net.http.HttpResponseCache;
import android.os.Build;

import com.google.android.gms.maps.model.LatLng;
import com.napontaratan.wifi.model.WifiConnection;
import com.napontaratan.wifi.model.WifiMarker;
import com.napontaratan.wifi.model.WifiRecord;

/**
 * Establish connection with the remote server for
 * - fetch wifi points
 * - push new wifi points
 *  
 * @author Napon Taratan
 */
public class ServerConnection {
	/**
	 * Singleton instance.
	 */
	private static ServerConnection instance = null;
	
	/**
	 * URL of the web server.
	 */
	public static final String WEBSERVER = 
			"http://www.napontaratan.com/wifinder/";

	/**
	 * Name of the script used to fetch data from server.
	 */
	public static final String GET_SCRIPT = "location.php";
	
	/**
	 * Name of the script used to send data to server.
	 */
	public static final String ADD_SCRIPT = "add_location.php";
	
	/**
	 * Name of the database.
	 */
	public static final String DATABASE = "napontar_wifinder";
	
	/**
	 * Name of the table.
	 */
	public static final String TABLE = "WIFI_LOCATIONS";
	
	/**
	 * Name of the PHP and SQL field corresponding 
	 * to <code>ssid</code> of <code>WifiConnection</code>.
	 * 
	 * @see WifiConnection
	 */
	public static final String SSID = "ssid";
	
	/**
	 * Name of the PHP and SQL field corresponding
	 * to <code>signalStrength</code> in <code>WifiConnection</code>.
	 * 
	 * @see WifiConnection
	 */
	public static final String SIGNAL_STRENGTH = "str";
	
	/**
	 * Name of the PHP and SQL field corresponding 
	 * to <code>location.latitude</code> of <code>WifiConnection</code>.
	 * 
	 * @see WifiConnection
	 */
	public static final String LATITUDE = "lat";
	
	/**
	 * Name of the PHP and SQL field corresponding 
	 * to <code>location.longitude</code> of <code>WifiConnection</code>.
	 * 
	 * @see WifiConnection
	 */
	public static final String LONGITUDE = "lon";
	
	/**
	 * Name of the PHP and SQL field corresponding 
	 * to <code>timeDiscovered</code> of <code>WifiConnection</code>.
	 * 
	 * @see WifiConnection
	 */
	public static final String TIME_DISCOVERED = "time";
	
	/**
	 * Name of the PHP and SQL field corresponding 
	 * to <code>userId</code> of <code>WifiConnection</code>.
	 * 
	 * @see WifiConnection
	 */
	public static final String USER_ID = "user";
	
	/**
	 * Name of the PHP field used to specify the radius around
	 * a specific point when fetching data from the server.
	 */
	public static final String RADIUS = "rad";
	
	/**
	 * Disable direct instantiation of singleton class.
	 */
	private ServerConnection() {}

	/**
	 * @return Singleton instance of <code>ServerConnection</code>.
	 */
	public static ServerConnection getInstance() {
		if(instance == null)
			instance = new ServerConnection();
		return instance;
	}
	
	/**
	 * Gets a WifiMarker object from the server.
	 * 
	 * @param location Location used for the query.
	 * @throws ServerConnectionFailureException
	 * @author Napon Taratan
	 * @author Kurt Ahn
	 */
	public WifiMarker getWifiMarker(LatLng location) 
			throws ServerConnectionFailureException {
		String response = makeJSONQuery(
				WEBSERVER + GET_SCRIPT + "?" +
				LATITUDE + "=" + location.latitude + "&" +
				LONGITUDE + "=" + location.longitude + "&" +
				RADIUS + "=" + 0.100);
		
		WifiMarker marker = new WifiMarker(location);
		
		try {
			JSONTokener raw 	= new JSONTokener(response);
			JSONArray jsArray	= new JSONArray(raw);
			
			for(int i = 0; i < jsArray.length(); i++) {
				JSONObject obj = (JSONObject) jsArray.get(i);
				
				WifiRecord record = new WifiRecord(marker,
						obj.getString(SSID),
						new LatLng(
								obj.getDouble(LATITUDE),
								obj.getDouble(LONGITUDE)),
						obj.getInt(SIGNAL_STRENGTH));
				
				marker.addRecord(record);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return marker;
	}

	/**
	 * Add a new WifiConnection to the web server
	 * 
	 * @param connection - WifiConnection to push
	 * @author Napon Taratan
	 * @throws ServerConnectionFailureException 
	 */
	public void addWifiConnection(WifiConnection connection) 
			throws ServerConnectionFailureException {
		makeJSONQuery(
				WEBSERVER + ADD_SCRIPT + "?" +
				SSID + "=" + encode(connection.ssid) + "&" +
				SIGNAL_STRENGTH + "=" + connection.signalStrength + "&" +
				LATITUDE + "=" + connection.location.latitude + "&" + 
				LONGITUDE + "=" + connection.location.longitude + "&" +
				USER_ID + "=" + connection.userId + "&" + 
				TIME_DISCOVERED + "=" + connection.timeDiscovered);
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
		/*
		 * TODO
		 * '+' is probably a valid character for SSID's.
		 * Consider maybe ASCII encoding:
		 * ' ' --> '%20'
		 * '%' --> '%25' etc.
		 */
		return url.replace(' ', '+');
	}

	/**
	 * Creates an HTTP request to the server and returns the server's response
	 * 
	 * @param url URL of request
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
