package com.example.translinkmobile;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class JourneyMap extends Activity implements JSONRequest.NetworkListener {
	/** Draw a polyline on the map marking the from > to destination
	 * Include the user's position on this map
	 */
	
	
	/* Placeholder for now. */
	private static final LatLng DEFAULT_LOCATION = new LatLng(-27.498037,153.017823);
	
	private GoogleMap map;
	private Marker userPos; // Mark the user's position
	private String result; // To store the result
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.journey_map);
		LatLng center = DEFAULT_LOCATION;
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15));
		Intent intent = getIntent();
		String[] ids = intent.getStringArrayExtra("locs");
		String fromId = ids[0];
		String destId = ids[1];
		String date = ids[2];
		requestPlan(fromId, destId, date);
	}

	private void requestPlan(String fromId, String destId, String date) {
		
		/* Call our php code on web zone */
		
		String urlString = "http://deco3801-010.uqcloud.net/journeyplan.php?fromLocId=" + Uri.encode(fromId) + "&destLocId=" + Uri.encode(destId) + "&date=" + Uri.encode(date);
		Log.d("JourneyMap request: ", urlString);
		JSONRequest request = new JSONRequest();
		request.setListener(this);
		request.execute(urlString);
	}
	@Override
	public void networkRequestCompleted(String result) {
		this.result = result;
		Log.d("Journeymap got result: ", result);
		parseResult();
	}

	private void parseResult() {
		;
	}
}
