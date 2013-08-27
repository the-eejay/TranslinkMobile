package com.example.translinkmobile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.content.Intent;
import android.util.Log;
import android.widget.ArrayAdapter;

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
	//private HashMap<String, Route> routeMap;
	private ArrayList<StopRoute> stopRoutes;
	private ArrayAdapter<String> adapter;
	
	//public RouteDataLoader(List<String> list, HashMap<String, Route> routeMap) {
	public RouteDataLoader(List<String> list, ArrayAdapter<String> adapter) {
		isLoading = false;
		//this.stop = stop;
		this.list = list;
		//this.routeMap = routeMap;
		stopRoutes = new ArrayList<StopRoute>();
		this.adapter = adapter;
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
		
		setStopRouteTimes();	
		addTimesToList();
		//Log.d("Route",((String[])stopRoutes.toArray())[0]);
	}
	
	public boolean isLoading() {
		return isLoading;
	}
	
	public void setStopRouteTimes() {
		//ArrayList<String> output = new ArrayList<String>();
		ArrayList<Route> routes = stop.getRoutes();
			Object obj = JSONValue.parse(result);
			//try {
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
							try {
								StopRoute sr = new StopRoute(stop, r);
								//sr.addTime(ISO8601DateParser.parse(timestr));
								Log.d("Route", "longstr="+timestr.substring(6,18));
								sr.addTime(new Date(Long.parseLong(timestr.substring(6,18))));
								stopRoutes.add(sr);
								
							} catch (Exception e) {
								Log.d("Route", "error in parsing JSON date");
							}
						}
						//output.add(timestr);
					}
					
				}
				//return output;
			//} catch (Exception e) {
				//No stops were found for the given location, or there was some network error
				//Log.d("Location", e.toString()); 
				//return null;
			//}
		
	}
	
	public void addTimesToList() {
		/*
		for (StopRoute sr: stopRoutes) {
			if (sr.getStop() == stop) {
				Route r = sr.getRoute();
				String line = routeMap.get(r);
				//SimpleDateFormat df = new SimpleDateFormat( "HH:mm:ssz" );
				ArrayList<Date> times = sr.getTimes();
				
				//Find the closest scheduled time after the current time
				int minIndex = 0;
				Long min = 9999999999999l;
				Long currTime = System.currentTimeMillis()/10;
				for (int i=0; i<times.size(); i++) {
					Long time = times.get(i).getTime();
					Log.d("Route", "time="+time+" currTime="+currTime+" min="+min);
					if (time > currTime && time < min) {
						minIndex = i;
						min = time;
					}
				}
				line += " " + min +" minutes to arrival.";
				Log.d("Route", "line="+line);
				Log.d("Route", "line_IN_MAP="+routeMap.get(r));
			}
		}
		*/
		for (int i=0; i< list.size(); i++) {
			String line = list.get(i);
			ArrayList<StopRoute> matching = new ArrayList<StopRoute>();
			int minTimeIndex = 0;
			int minStopRouteIndex = 0;
			Long min = 9999999999999l;
			Long currTime = System.currentTimeMillis()/10;
			for (int k=0; k<stopRoutes.size();k++) {
				StopRoute sr = stopRoutes.get(k);
				if (line.contains(sr.getRoute().getCode())) {
				
			
					ArrayList<Date> times = sr.getTimes();
					//Find the closest scheduled time after the current time
					
					for (int j=0; j<times.size(); j++) {
						Long time = times.get(j).getTime();
						Log.d("Route", "time="+time+" currTime="+currTime+" min="+min);
						if (time > currTime && time < min) {
							minTimeIndex = j;
							minStopRouteIndex = k;
							min = time;
						}
					}
				}
			}
			
			//Add result to the line
			if (min < 9999999999999l) {
				long minutes = (min-currTime)/10000;
				list.set(i, line+"    "+ minutes + " minutes until arrival");
			} else {
				list.set(i, line+"    End Of Service");
			}
			Log.d("Route", "list("+i+")="+list.get(i));
			adapter.notifyDataSetChanged();
		}
	}

}
