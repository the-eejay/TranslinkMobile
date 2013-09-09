package com.example.translinkmobile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.android.gms.maps.GoogleMap;

import android.util.Log;
import android.widget.ArrayAdapter;

public class RouteStopsLoader implements JSONRequest.NetworkListener{
	private enum State {
		STOPS, POLYLINE
	}
	private boolean isLoading; //isLoading - if currently performing async task
	private String result;
	private GoogleMap map;
	private State state;

	public RouteStopsLoader(GoogleMap map) {
		isLoading = false;
		this.map = map;
	}

	/**
     * A method to initialize the request to get the service route times
     * of each stop to the PHP web service. 
     *
     * @param stops ArrayList containing all the nearby stops.
     */
	public void requestRouteStops(String route) {
		

		
		String urlString = "http://deco3801-010.uqcloud.net/routestops.php?route="
				+ route;

		JSONRequest request = new JSONRequest();
		request.setListener(this);
		request.execute(urlString);
		isLoading = true;
		state = State.STOPS;

	}
	
public void requestRouteLine(String route) {
		

		
		String urlString = "http://deco3801-010.uqcloud.net/routeline.php?route="
				+ route;

		JSONRequest request = new JSONRequest();
		request.setListener(this);
		request.execute(urlString);
		isLoading = true;
		state = State.POLYLINE;

	}

	/**
     * A method that defines what should be done after the PHP
     * request is completed. Overrides the networkRequestCompleted
     * method of the JSONRequest.NetworkListener interface.
     *
     * @param result The String result from the PHP service request.
     */
	@Override
	public void networkRequestCompleted(String result) {
		isLoading = false;
		this.result = result;

		if (state == State.POLYLINE) {
			//get the line from JSON and add to map
		} else if (state == State.STOPS){ 
			parseRoute();
			addMarkersToMap();
		}
	}

	/**
	 * 
	 * @return boolean if RouteDataLoader is performing an asynchronous task
	 */
	public boolean isLoading() {
		return isLoading;
	}

	/**
     * A method to get the departure times of each service route.
     *
     */
	public void parseRoute() {

		/* Parse the JSON received from the server. The format is shown below for the parts used:
		 * {StopTimetables: [Trips:[DepartureTime, Route: {Code}]]}
		 */
		/*
		Object obj = JSONValue.parse(result);
		JSONArray timetables = (JSONArray) ((JSONObject) obj)
				.get("StopTimetables");
		
		for (int h = 0; h < timetables.size(); h++) {
			JSONArray trips = (JSONArray) ((JSONObject) timetables.get(h))
					.get("Trips");
			
			for (int i = 0; i < trips.size(); i++) {
				JSONObject time = (JSONObject) trips.get(i);
				String timestr = (String) time.get("DepartureTime");
				String routecode = (String) ((JSONObject) time.get("Route"))
						.get("Code");
				Log.d("RESULT=", timestr);

				//Create each StopRoute relationship and give them the times from the JSON
				for (Stop stop : stops) {
					ArrayList<Route> routes = stop.getRoutes();

					for (Route r : routes) {
						if (r.getCode().equals(routecode)) {
							try {
								StopRoute sr = new StopRoute(stop, r);
								Log.d("Route",
										"longstr=" + timestr.substring(6, 18));
								sr.addTime(new Date(Long.parseLong(timestr
										.substring(6, 18))));
								stopRoutes.add(sr);

							} catch (Exception e) {
								Log.d("Route", "error in parsing JSON date");
							}
						}
					}
				}
			}

		}*/
	}

	/**
     * A method to format the departure time and add it to the list adapter.
     *
     */
	public void addMarkersToMap() {
		
		
	}
}
