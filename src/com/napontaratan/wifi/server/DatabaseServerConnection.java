package com.napontaratan.wifi.server;

import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.android.gms.maps.model.LatLng;
import com.napontaratan.wifi.model.WifiConnection;
import com.napontaratan.wifi.model.WifiMarker;
import com.napontaratan.wifi.model.WifiRecord;

public class DatabaseServerConnection extends ServerConnection {
	/**
	 * URL of the web server.
	 */
	public static final String SERVER = 
			"http://www.napontaratan.com/wifinder/";

	/**
	 * Name of the script used to fetch data from server.
	 */
	public static final String GET_SCRIPT = "get.php";
	
	/**
	 * Name of the script used to send data to server.
	 */
	public static final String ADD_SCRIPT = "add.php";
	
	/**
	 * Name of the database.
	 */
	public static final String DATABASE = "wifi";
	
	/**
	 * Name of the table.
	 */
	public static final String TABLE = "records";
	
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
	 * Gets a WifiMarker object from the server.
	 * 
	 * @param location Location used for the query.
	 * @throws ServerConnectionFailureException
	 * @author Napon Taratan
	 * @author Kurt Ahn
	 */
	public static WifiMarker getWifiMarker(LatLng location) {
		try {
			String response = sendGetRequest(
					SERVER + GET_SCRIPT,
					new Object[][] {
							{LATITUDE, location.latitude},
							{LONGITUDE, location.longitude},
							{RADIUS, 0.100}
					});
			WifiMarker marker = new WifiMarker(location);
			JSONArray array = new JSONArray(response);
			
			for (int i = 0; i < array.length(); ++i) {
				JSONObject object = (JSONObject) array.get(i);
				WifiRecord record = new WifiRecord(marker,
						object.getString(SSID),
						new LatLng(object.getDouble(LATITUDE),
								object.getDouble(LONGITUDE)),
						object.getInt(SIGNAL_STRENGTH));
				marker.addRecord(record);
			}
			
			return marker;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * Add a new WifiConnection to the web server
	 * 
	 * @param connection - WifiConnection to push
	 * @author Napon Taratan
	 * @throws ServerConnectionFailureException 
	 * @throws IOException 
	 */
	public static void addWifiConnection(WifiConnection connection) 
			throws IOException {
		if (connection == null)
			return;
		sendPostRequest(
				SERVER + ADD_SCRIPT,
				new Object[][] {
						{SSID, connection.ssid},
						{SIGNAL_STRENGTH, connection.signalStrength},
						{LATITUDE, connection.location.latitude},
						{LONGITUDE, connection.location.longitude},
						{USER_ID, connection.userId},
						{TIME_DISCOVERED, connection.timeDiscovered.getTime()}
				});
	}
	
	/**
	 * Maps dBm signal strength to a readable 1-5 scale
	 * source: http://www.anandtech.com/show/3821/iphone-4-redux-analyzing-apples-ios-41-signal-fix
	 * @param strength - dBm value
	 * @return int[1,5]
	 * @author Napon Taratan
	 */
	private static String convertToBarLevel(int strength) {
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
}
