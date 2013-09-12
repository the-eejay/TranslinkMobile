package com.example.translinkmobile;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.graphics.Color;
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
	private ArrayList<Marker> stopMarkers;
	private HashMap<Marker, Stop> stopMarkersMap;
	private int[] markerIcons = {R.drawable.bus_geo, R.drawable.train_geo, R.drawable.ferry_geo};
	private Route route2;

	public RouteStopsLoader(GoogleMap map, ArrayList<Marker> stopMarkers, HashMap<Marker,Stop> stopMarkersMap) {
		isLoading = false;
		this.map = map;
		this.stopMarkers = stopMarkers;
		this.stopMarkersMap = stopMarkersMap;
		route2= null;
	}

	/**
     * A method to initialize the request to get the service route times
     * of each stop to the PHP web service. 
     *
     * @param stops ArrayList containing all the nearby stops.
     */
	public void requestRouteStops(Route route) {
		

		
		String urlString = "http://deco3801-010.uqcloud.net/routestops.php?route="
				+ route.getCode() + "&type=" + route.getType();

		JSONRequest request = new JSONRequest();
		request.setListener(this);
		request.execute(urlString);
		isLoading = true;
		state = State.STOPS;
		route2=route;

	}
	
public void requestRouteLine(Route route) {
		

		
		String urlString = "http://deco3801-010.uqcloud.net/routeline.php?route="
				+ route.getCode() + "&type=" + route.getType();

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
			String line = parseJSONToLine();
			addLineToMap(line);
		} else if (state == State.STOPS){ 
			ArrayList<Stop> stops = parseJSONToStops();
			addMarkersToMap(stops);
			requestRouteLine(route2);
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
	public ArrayList<Stop> parseJSONToStops() {
		ArrayList<Stop> output = new ArrayList<Stop>();
		/*Use JSON-simple to parse the result. The format (of parts used) is below:
			* {Stops: [StopId, Description, Position: {Lat, Lng}, HasParentLocation,
			* ParentLocation: {Id, Position: {Lat, Lng}}, Routes: [Code, Name]]}
			*/
			Object obj = JSONValue.parse(result);
			//try {
				Log.d("Location", "result="+result);
				JSONArray array = (JSONArray)((JSONObject)obj).get("Stops");
				for (int i=0; i<array.size(); i++) {
					JSONObject obj2 = (JSONObject)array.get(i);
					Log.d("RESULT=", obj2.toJSONString());
					JSONObject pos = (JSONObject)obj2.get("Position");
					Stop stop = new Stop((String)obj2.get("StopId"), (String)obj2.get("Description"), ((Long)obj2.get("ServiceType")).toString(),
							new LatLng((Double)pos.get("Lat"), (Double)pos.get("Lng")));
					/*if ((Boolean)obj2.get("HasParentLocation")) {
						JSONObject parent = (JSONObject)obj2.get("ParentLocation");
						JSONObject parentPosJSON = (JSONObject)parent.get("Position");
						LatLng parentPos = new LatLng((Double)parentPosJSON.get("Lat"), (Double)parentPosJSON.get("Lng"));
						stop.setParentPosition((String)parent.get("Id"), parentPos);
					}
					JSONArray routes = (JSONArray)((JSONObject)obj2).get("Routes");
					for (int j=0; j<routes.size(); j++) {
						JSONObject route = (JSONObject)routes.get(j);
						stop.addRoute(new Route((String)route.get("Code"), (String)route.get("Name"), (Integer)route.get("Vehicle")));
					}*/
					output.add(stop);
				}
				return output;
		//} catch (Exception e) {
			//return null;
		//}
	}

	/**
     * A method to format the departure time and add it to the list adapter.
     *
     */
	public void addMarkersToMap(ArrayList<Stop> stops) {
		// Remove old stops from map
		/*for (Marker stopMarker : stopMarkers) {
			Log.d("Location", "removing marker");
			stopMarker.setVisible(false);
			stopMarker.remove();

		}
		stopMarkers.clear();*/
		
		// Add new stop markers to map
		if (stops != null) {
			for (Stop stop : stops) {
				int serviceType = stop.getServiceType();
				Log.d("serviceType", "" + serviceType);
				
				
				Marker m = map.addMarker(new MarkerOptions()
						.position(stop.getPosition())
						.title(stop.getDescription())
						//.snippet(stop.getDescription())
						.icon(BitmapDescriptorFactory.fromResource(markerIcons[serviceType-1])));
				stopMarkers.add(m);
				stopMarkersMap.put(m, stop);
						
					
				

			}
		} 

	}
	
	public String parseJSONToLine() {
		
		Object obj = JSONValue.parse(result);
		String output = null;
		//try {
			
			JSONArray array = (JSONArray)((JSONObject)obj).get("Paths");
			JSONObject obj2 = (JSONObject)array.get(0);
			output = (String) (obj2.get("Path"));
		//} catch (Exception e) {
			//error loading data
		//}
		return output;
	}
	
	public void addLineToMap(String line) {
		PolylineOptions polylineOptions = new PolylineOptions();
		polylineOptions.width(5).color(Color.RED);
		polylineOptions.addAll(PolylineDecoder.decodePoly(line));
		map.addPolyline(polylineOptions);
		
		//Debug
		List<LatLng> test = PolylineDecoder.decodePoly(line);
		Log.d("JSONRequest", "line(0)=" + test.get(0).toString());
		Log.d("JSONRequest", "line(1)=" + test.get(1).toString());
		Log.d("JSONRequest", "line(2)=" + test.get(2).toString());
	}
}
