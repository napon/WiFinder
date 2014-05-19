package controller;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import model.WiFiPoint;

public class WiFiController {

	/**
	 * Buffer used to store unsent WiFiPoint objects.
	 */
	private List<WiFiPoint> wifiConnectionBuffer = new ArrayList<WiFiPoint>(); // Maybe a different container type?
	
	
	public WiFiController(Context context) {
		// DO SOMETHING
	}
	
	/**
	 * @return Buffer holding unsent WiFiPoint objects.
	 * 
	 * @author Kurt Ahn
	 */
	public List<WiFiPoint> getWiFiPointBuffer() {
		return wifiConnectionBuffer;
	}

}