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
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends Activity{
	
	private static final LatLng DEFAULT_LOCATION = new LatLng(-27.498037,153.017823);
	
	private GoogleMap mMap;
	private Marker userPos;
	private Marker clickPos;
	private StopDataLoader stopLoader;
	private boolean updatedOnce;

	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		LatLng center = DEFAULT_LOCATION;
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15));
		
		stopLoader = new StopDataLoader(mMap);
		//stopLoader.requestStopsNear(-27.498037,153.017823, 1000);
		
		/*ArrayList<Stop> stops = stopLoader.getStopsNear();
		for (Stop stop : stops) {
			mMap.addMarker(new MarkerOptions()
			.position(stop.getPosition())
			.title(stop.getId()));
			
		}
		*/
		userPos = mMap.addMarker(new MarkerOptions()
			.position(DEFAULT_LOCATION)
			.title("Your Position")
			.visible(false)
			.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
		clickPos = mMap.addMarker(new MarkerOptions()
        	.position(DEFAULT_LOCATION)
        	.title("Your Selected Position")
        	.visible(false)
        	.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
		
		
		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			
			@Override
			public void onInfoWindowClick(Marker marker) {
				Stop stop = stopLoader.getIdOfMarker(marker);
				if (stop != null) {
					MainApplication app = (MainApplication)getApplicationContext();
					app.setSelectedStop(stop);
					Intent intent = new Intent(getApplicationContext(), DisplayRoutesActivity.class);
					startActivity(intent);
				}
				
			}
			
		});
		
		mMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng arg0) {
				clickPos.setVisible(true);
				clickPos.setPosition(arg0);
				locationChanged(arg0);
				
			}
			
		});
		updatedOnce = false;
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		      // Called when a new location is found by the network location provider.
		    	LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
		    	userPos.setVisible(true);
		    	userPos.setPosition(userLatLng);
		    	if (!updatedOnce) {
		    		locationChanged(userLatLng);
		    	}
		      updatedOnce = true;
		    }

		    public void onStatusChanged(String provider, int status, Bundle extras) {}

		    public void onProviderEnabled(String provider) {}

		    public void onProviderDisabled(String provider) {}
		  };

		  // Register the listener with the Location Manager to receive location updates
		  // check every 1minutes and only if location has changed by 50 metres
			  
		  if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			  Log.d("Location", "using network");
			  locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 50, locationListener);
		  } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			  Log.d("Location", "using gps");
			  locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 50, locationListener);
		  } else {
			  //No location provider enabled. Use the default location for now
			  Log.d("Location", "cannot find user location");
			  locationChanged(new LatLng(-27.498037,153.017823));
		  }
		
	}	
	
	public void locationChanged(LatLng location) {
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
		stopLoader.requestStopsNear(location.latitude, location.longitude, 1000);
		
		
	}
	
}