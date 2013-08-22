package com.example.translinkmobile;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.os.AsyncTask;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity{
	
	private GoogleMap mMap;
	private Marker UQLakes;
	private Marker ChancellorsPlace;

	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		LatLng center = new LatLng(-27.498037,153.017823);
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15));
		
		StopDataLoader stopLoader = new StopDataLoader(mMap);
		stopLoader.requestStopsNear(-27.498037,153.017823, 1000);
		
		/*ArrayList<Stop> stops = stopLoader.getStopsNear();
		for (Stop stop : stops) {
			mMap.addMarker(new MarkerOptions()
			.position(stop.getPosition())
			.title(stop.getId()));
			
		}
		UQLakes = mMap.addMarker(new MarkerOptions()
        .position(new LatLng(-27.498029,153.017672))
        .title("UQ Lakes"));
		
		ChancellorsPlace = mMap.addMarker(new MarkerOptions()
        .position(new LatLng(-27.497974,153.011139))
        .title("UQ Chancellors Place"));
		*/
		mMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				Intent intent = new Intent(getApplicationContext(), RetreiveScheduleActivity.class);
				startActivity(intent);
				return true;
			}
			
		});
		
	}	
	
}