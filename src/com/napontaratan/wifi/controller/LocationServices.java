package com.napontaratan.wifi.controller;

import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;

public class LocationServices {
	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	private Activity activity;
	private LocationClient locationClient;

	ConnectionCallbacks connectionCallbacks = new ConnectionCallbacks() {
		@Override
		public void onDisconnected() {
			Toast.makeText(activity, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();;
		}
		@Override
		public void onConnected(Bundle arg0) {
			Toast.makeText(activity, "Connected to Location Services", Toast.LENGTH_SHORT).show();
		}
	};

	OnConnectionFailedListener connectionFailedListener = new OnConnectionFailedListener() {
		@Override
		public void onConnectionFailed(ConnectionResult connectionResult) {
			if (connectionResult.hasResolution()) {
				// Attempt to resolve automatically if possible
				try {
					connectionResult.startResolutionForResult(activity,
							CONNECTION_FAILURE_RESOLUTION_REQUEST);
				} catch (IntentSender.SendIntentException e) {
					e.printStackTrace();
				}
			} else {
				// TODO Auto-generated method stub
				// Show an error to the user?
			}
		}
	};
	
	public LocationServices(Activity activity) {
		this.activity = activity;

		locationClient = new LocationClient(activity, connectionCallbacks,
				connectionFailedListener);
		locationClient.connect();
	}

	public Location getLocation() {
		return locationClient.getLastLocation();
	}
}
