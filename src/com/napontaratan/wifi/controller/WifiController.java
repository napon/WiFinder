package com.napontaratan.wifi.controller;

import java.util.ArrayList;
import java.util.List;

import com.napontaratan.wifi.model.WifiMarker;

import android.content.Context;

public class WifiController {

	/**
	 * Buffer used to store unsent WifiConnection objects.
	 */
	private List<WifiMarker> wifiConnectionBuffer = 
			new ArrayList<WifiMarker>(); // Maybe a different container type?
	
	/**
	 * @return Buffer holding unsent WifiConnection objects.
	 * 
	 * @author Kurt Ahn
	 */
	public List<WifiMarker> getWifiConnectionBuffer() {
		return wifiConnectionBuffer;
	}
	
	public WifiController(Context context) {
		// DO SOMETHING
	}
}