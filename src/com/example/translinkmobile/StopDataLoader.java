package com.example.translinkmobile;



import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.util.Log;

import com.example.translinkmobile.HttpRequest.NetworkListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class StopDataLoader implements JSONRequest.NetworkListener{
	private enum State {
		STOPS_NEAR
	}
	private boolean isLoading;
	private State state;
	private String result;
	private GoogleMap map;
	private ArrayList<Stop> stops; //might need to move this and the marker arraylist into MainActivity.java
	private ArrayList<Marker> stopMarkers;
	
	public StopDataLoader(GoogleMap map) {
		isLoading = false;
		this.map = map;
		stopMarkers = new ArrayList<Marker>();
	}
	public void requestStopsNear(double lat, double lng, int radius) {
		
		String urlString = "http://deco3801-010.uqcloud.net/stopsnearby.php?lat="+lat+"&lng="+lng+"&rad="+radius;
		
		//String urlString = "http://pataflafla.sg/transponders/dummyschedule.txt";
		JSONRequest request = new JSONRequest();
		request.setListener(this);
		request.execute(urlString);
		state=State.STOPS_NEAR;
		isLoading=true;
		
}
	@Override
	public void networkRequestCompleted(String result) {
		isLoading = false;
		this.result = result;
		
		if (state == State.STOPS_NEAR) {
			//Remove old stops from map
			for (Marker stopMarker : stopMarkers) {
				Log.d("Location", "removing marker");
				stopMarker.setVisible(false);
				stopMarker.remove();
				
			}
			stopMarkers.clear();
			//Add new stops to map
			stops = getStopsNear();
			if (stops != null) {
				for (int i=0; i<stops.size(); i++) {
					stopMarkers.add(map.addMarker(new MarkerOptions()
					.position(stops.get(i).getPosition())
					.title(stops.get(i).getId())));
					
					
				}
			}
		}
		
	}
	
	public boolean isLoading() {
		return isLoading;
	}
	
	public ArrayList<Stop> getStopsNear() {
		ArrayList<Stop> output = new ArrayList<Stop>();
		if (state == State.STOPS_NEAR) {
			Object obj = JSONValue.parse(result);
			try {
				JSONArray array = (JSONArray)((JSONObject)obj).get("Stops");
				for (int i=0; i<array.size(); i++) {
					JSONObject obj2 = (JSONObject)array.get(i);
					Log.d("RESULT=", obj2.toJSONString());
					JSONObject pos = (JSONObject)obj2.get("Position");
					output.add(new Stop((String)obj2.get("StopId"), ((Long)obj2.get("ServiceType")).toString(),
							new LatLng((Double)pos.get("Lat"), (Double)pos.get("Lng"))));
				}
				return output;
			} catch (Exception e) {
				//No stops were found for the given location, or there was some network error
				return null;
			}
		} else {
			return null;
		}
	}
	
}
