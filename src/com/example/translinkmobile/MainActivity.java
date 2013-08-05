package com.example.translinkmobile;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
	
	private GoogleMap mMap;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		LatLng center = new LatLng(-27.498037,153.017823);
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15));
		
		Marker platformA = mMap.addMarker(new MarkerOptions()
        .position(new LatLng(-27.498029,153.017672))
        .title("UQ Lakes Platform A"));
		
		Marker platformB = mMap.addMarker(new MarkerOptions()
        .position(new LatLng(-27.498037,153.017823))
        .title("UQ Lakes Platform B"));
		
		
	}
}