package com.napontaratan.wifi.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.annotation.SuppressLint;

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
	@SuppressLint("NewApi")
	public String makeJSONQuery(String url) 
			throws ServerConnectionFailureException {
		
		String responseString = null;
        
        try {
            System.out.println("make JSON query to server \n " + url);
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response = httpClient.execute(new HttpGet(url));
            responseString = new BasicResponseHandler().handleResponse(response);
            System.out.println(responseString);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServerConnectionFailureException();
        }

        return responseString;
//			throws ServerConnectionFailureException {
//		URL url = null;
//		HttpURLConnection client = null;
//		
//		try {
//			url = new URL(server); 
//			System.out.println("make JSON query to server");
//			client = (HttpURLConnection) url.openConnection();
//			int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
//			client.addRequestProperty("Cache-Control", "max-stale=" + maxStale); // try get cache response
////			System.out.println("cache request count: " + cache.getRequestCount());
////			System.out.println("cache hit count: " + cache.getHitCount() );
////			System.out.println("cache network count: "  + cache.getNetworkCount());
//		} catch (MalformedURLException e) {
//			throw new ServerConnectionFailureException(
//					"Server address '" + server + "' is not valid.");
//		} catch (IOException e) {
//			throw new ServerConnectionFailureException(
//					"Failed to connection to '" + server + "'.");
//		}
//		
//		InputStream in = null;
//		BufferedReader br = null;
//		
//		try { 
//			in = client.getInputStream();
//		} catch (IOException e) {
//			// if no cache response or cache response fails
//			try {
//				client = (HttpURLConnection) url.openConnection();
//				client.addRequestProperty("Cache-Control", "max-age=0");
//				in = client.getInputStream();
//			} catch (IOException ioe) {
//				ioe.printStackTrace();
//			}
//			
//			e.printStackTrace();
//		}
//		
//		try {
//			
//			br = new BufferedReader(
//					new InputStreamReader(in));
//			String current;
//			StringBuilder r = new StringBuilder();
//			System.out.println("Return: ");
//			while ((current = br.readLine()) != null) { 
//				System.out.println(current);
//				r.append(current.trim());
//			}
//			return r.toString();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} 
//		finally {
//			client.disconnect();
//			try {
//				br.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		return "";
	}
}
