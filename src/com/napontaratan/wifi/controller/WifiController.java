package com.napontaratan.wifi.controller;

import java.util.ArrayList;
import java.util.List;

import com.napontaratan.wifi.model.WifiConnection;

import android.content.Context;

public class WifiController {

	/**
	 * Buffer used to store unsent WiFiPoint objects.
	 */
	private List<WifiConnection> wifiConnectionBuffer = 
			new ArrayList<WifiConnection>(); // Maybe a different container type?
	
	
	public WifiController(Context context) {
		// DO SOMETHING
	}
	
	/**
	 * @return Buffer holding unsent WiFiPoint objects.
	 * 
	 * @author Kurt Ahn
	 */
	public List<WifiConnection> getWifiConnectionBuffer() {
		return wifiConnectionBuffer;
	}

}