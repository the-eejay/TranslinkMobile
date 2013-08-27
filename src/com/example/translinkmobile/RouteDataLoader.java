package com.example.translinkmobile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class RouteDataLoader implements JSONRequest.NetworkListener {

	
	private boolean isLoading;
	//private State state;
	private String result;
	private Stop stop;
	//private ArrayList<Stop> stops; //might need to move this and the marker arraylist into MainActivity.java
	//private ArrayList<Marker> stopMarkers;
	//private HashMap<Marker, Stop> stopMarkersMap;
	private List<String> list;
	private HashMap<String, Route> routeMap;
	
	public RouteDataLoader(List<String> list, HashMap<String, Route> routeMap) {
		isLoading = false;
		//this.stop = stop;
		this.list = list;
		this.routeMap = routeMap;
	}
	public void requestRouteTimes(Stop stop) {
		this.stop = stop;
		String urlString = "http://deco3801-010.uqcloud.net/routeschedule.php?stop=" + stop.getId();

		JSONRequest request = new JSONRequest();
		request.setListener(this);
		request.execute(urlString);
		//state=State.STOPS_NEAR;
		isLoading=true;
		
		
}
	@Override
	public void networkRequestCompleted(String result) {
		isLoading = false;
		this.result = result;
		
		setRouteTimes();	
		addTimesToList();
	}
	
	public boolean isLoading() {
		return isLoading;
	}
	
	public void setRouteTimes() {
		//ArrayList<String> output = new ArrayList<String>();
		ArrayList<Route> routes = stop.getRoutes();
			Object obj = JSONValue.parse(result);
			try {
				//Not sure if  the get(0) part will cause problems
				JSONArray trips = (JSONArray)((JSONObject)((JSONArray)((JSONObject)obj).get("StopTimetables")).get(0)).get("Trips");
				for (int i=0; i<trips.size(); i++) {
					JSONObject time = (JSONObject)trips.get(i);
					String timestr = (String)time.get("DepartureTime");
					String routecode = (String)((JSONObject)time.get("Route")).get("Code");
					Log.d("RESULT=", timestr);
					//find the route associated with this time
					for (Route r: routes) {
						if (r.getCode().equals(routecode)) {
							
								//r.setTime(ISO8601DateParser.parse(timestr));
							
						}
						//output.add(timestr);
					}
					
				}
				//return output;
			} catch (Exception e) {
				//No stops were found for the given location, or there was some network error
				Log.d("Location", e.toString());
				//return null;
			}
		
	}
	
	public void addTimesToList() {
		for (String str: list) {
			//SimpleDateFormat df = new SimpleDateFormat( "HH:mm:ssz" );
			Route r = routeMap.get(str);
			/*Long minutes = r.getTime().getTime() - System.currentTimeMillis();
			str += " " + minutes +" minutes to arrival.";*/
		}
	}

}
