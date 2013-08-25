package com.example.translinkmobile;

import java.util.ArrayList;

import android.net.Uri;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

public class JourneyDataLoader implements JSONRequest.NetworkListener {
	
	private GoogleMap map;
	private String result;
	private Polyline polyline;
	
	public JourneyDataLoader(GoogleMap map) {
		this.map = map;
		
	}
	
	public void requestPlan(String fromId, String destId) {
		String urlString = "http://deco3801-010.uqcloud.net/journeyplan.php?fromId=" + Uri.encode(fromId) + "destId=" + Uri.encode(destId);
		
		JSONRequest request = new JSONRequest();
		request.setListener(this);
		request.execute(urlString);
	}
	
	public Polyline getPolyLine() {
		return polyline;
	}
	
	@Override
	public void networkRequestCompleted(String result) {
		this.result = result;
		
	}
}
