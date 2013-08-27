package com.example.translinkmobile;



import java.util.ArrayList;
import java.util.HashMap;

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
	private HashMap<Marker, Stop> stopMarkersMap;
	
	public StopDataLoader(GoogleMap map) {
		isLoading = false;
		this.map = map;
		stopMarkers = new ArrayList<Marker>();
		stopMarkersMap = new HashMap<Marker, Stop>();
	}
	public void requestStopsNear(double lat, double lng, int radius) {
		
		String urlString = "http://deco3801-010.uqcloud.net/stopsnearby.php?lat="+lat+"&lng="+lng+"&rad="+radius;
		Log.d("urlString: ", urlString);
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
			//Add new stop markers to map
			stops = getStopsNear();
			ArrayList<String> usedParentIds = new ArrayList<String>();
			if (stops != null) {
				for (Stop stop: stops) {
					
					if (stop.hasParent()) {
						boolean isAlreadyUsed = false;
						for (String str: usedParentIds) {
							if (str.equals(stop.getParentId())){
								isAlreadyUsed = true;
								break;
							}
						}
						if (!isAlreadyUsed) {
							Marker m = map.addMarker(new MarkerOptions()
							.position(stop.getParentPosition())
							.title(stop.getParentId())
							.snippet("Click here for more"));
							stopMarkers.add(m);
							stopMarkersMap.put(m, stop);
							Log.d("Location", "!alreadyused");
						} else {
							Log.d("Location", "ISalreadyused");
						}
					} else {
						Marker m = map.addMarker(new MarkerOptions()
						.position(stop.getPosition())
						.title(stop.getDescription())
						.snippet("Click here for more"));
						stopMarkers.add(m);
						stopMarkersMap.put(m, stop);
						Log.d("Location", "no parent");
					}
					
				}
				
				//set the click events
				for (Marker stopMarker : stopMarkers) {
					//stopMarker.
				}
			} else {
				Log.d("Location", "stops was null");
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
			//try {
				/*
				JSONArray array = (JSONArray)((JSONObject)obj).get("Stops");
				for (int i=0; i<array.size(); i++) {
					JSONObject obj2 = (JSONObject)array.get(i);
					Log.d("RESULT=", obj2.toJSONString());
					JSONObject pos = (JSONObject)obj2.get("Position");
					output.add(new Stop((String)obj2.get("StopId"), ((Long)obj2.get("ServiceType")).toString(),
							new LatLng((Double)pos.get("Lat"), (Double)pos.get("Lng"))));
				}
				*/
				JSONArray array = (JSONArray)((JSONObject)obj).get("Stops");
				for (int i=0; i<array.size(); i++) {
					JSONObject obj2 = (JSONObject)array.get(i);
					Log.d("RESULT=", obj2.toJSONString());
					JSONObject pos = (JSONObject)obj2.get("Position");
					Stop stop = new Stop((String)obj2.get("StopId"), (String)obj2.get("Description"), ((Long)obj2.get("ServiceType")).toString(),
							new LatLng((Double)pos.get("Lat"), (Double)pos.get("Lng")));
					if ((Boolean)obj2.get("HasParentLocation")) {
						JSONObject parent = (JSONObject)obj2.get("ParentLocation");
						JSONObject parentPosJSON = (JSONObject)parent.get("Position");
						LatLng parentPos = new LatLng((Double)parentPosJSON.get("Lat"), (Double)parentPosJSON.get("Lng"));
						stop.setParentPosition((String)parent.get("Id"), parentPos);
					}
					JSONArray routes = (JSONArray)((JSONObject)obj2).get("Routes");
					for (int j=0; j<routes.size(); j++) {
						JSONObject route = (JSONObject)routes.get(j);
						stop.addRoute(new Route((String)route.get("Code"), (String)route.get("Name")));
					}
					output.add(stop);
				}
				return output;
			/*} catch (Exception e) {
				//No stops were found for the given location, or there was some network error
				Log.d("Location", e.toString());
				return null;
			}*/
		} else {
			return null;
		}
	}
	
	public Stop getIdOfMarker(Marker marker) {
		if (stopMarkersMap.containsKey(marker)) {
			return stopMarkersMap.get(marker);
		} else {
			return null;
		}
	}
}
