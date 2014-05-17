package ui;

import java.util.List;

import model.WiFiPoint;

import android.os.Bundle;
import android.app.Activity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.napontaratan.wifinder.R;

import controller.WiFinderServerConnection;

public class MapActivity extends Activity {

	private static final LatLng VANCOUVER = new LatLng(49.22, -123.15);
	private GoogleMap map;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		setUpMap();
	}

	/**
	 * Basic map view centered at Vancouver
	 * @author napontaratan
	 */
	private void setUpMap() {
		if(map == null) {
			System.out.println("map is null, setting it up");
			map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		}	

		map.moveCamera(CameraUpdateFactory.newLatLng(VANCOUVER));
		map.animateCamera(CameraUpdateFactory.zoomTo(11));
		
		WiFinderServerConnection connection = WiFinderServerConnection.getInstance();
		List<WiFiPoint> points = connection.getWiFiPoints();
		for(WiFiPoint wp : points){
			plotPoint(wp);
		}
	}

	/**
	 * Plot the WiFiPoint on the map view
	 * @author napontaratan
	 * @param wp Point to plot on the map
	 */
	private void plotPoint(WiFiPoint wp) {
		map.addMarker(new MarkerOptions()
			.position(new LatLng(wp.getLatitude(),wp.getLongitude()))
			.title(wp.getName())
			.snippet("Signal Strength: " + wp.getSignalStrength()));
	}
}