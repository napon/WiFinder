package controller;

import model.WifiConnection;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

/**
 * BroadcastReceiver object triggered when a requested
 * wifi scan is complete.
 * 
 * @author Kurt Ahn
 */
public class WifiProcessor extends BroadcastReceiver {
	/**
	 * Process wifi scan results to produce WifiConnection objects,
	 * and then push the objects either to the context's buffer or
	 * to the server.
	 *
	 * @author Kurt Ahn
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		List<ScanResult> scans =
				((WifiManager) context.
						getSystemService(Context.WIFI_SERVICE)).
						getScanResults();
		
		List<WifiConnection> buffer =
				((MapActivity) context).getWifiConnectionBuffer();
		
		// **We need location data. I'm assuming it's been
		// taken care of somewhere else and is available.
		
		// Create WifiLocation object.
		
		{	// IF currently connected to a network
			// AND succeeded to connect to server,
			
			for (ScanResult s : scans) {
				// Create WifiConnection object based on scan.
				// Send data directly to server.
			}
			
			{	// WHILE buffer is not empty,
				
				// Send WifiConnection object to server.
				// Remove object from buffer.
			}
		} {	// ELSE,
			
			for (ScanResult s : scans) {
				// Create WifiConnection object based on scan.
				// Push object to buffer.
			}
		}
	}
}
