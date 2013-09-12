package com.example.translinkmobile;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * The class that will load the nearby stops data from Translink OPIA API. It
 * implements the JSONRequest.NetworkListener to establish the connection to
 * Translink OPIA API.
 * 
 * @author Transponders
 * @version 1.0
 */
public class StopDataLoader implements JSONRequest.NetworkListener {

	private enum State {
		STOPS_NEAR
	}

	private boolean isLoading;
	private State state;
	private String result;
	private GoogleMap map;
	private ArrayList<Stop> stops;
	private ArrayList<Marker> stopMarkers;
	private HashMap<Marker, Stop> stopMarkersMap;
	private int[] markerIcons = {R.drawable.bus_geo_border, R.drawable.train_geo_border, R.drawable.ferry_geo_border};

	public StopDataLoader(GoogleMap map, ArrayList<Marker> stopMarkers, HashMap<Marker,Stop> stopMarkersMap) {
		isLoading = false;
		this.map = map;
		this.stopMarkers = stopMarkers;
		this.stopMarkersMap = stopMarkersMap;
	}

	/**
	 * A method to initialize the request to get the nearby stops the PHP web
	 * service.
	 * 
	 * @param lat the latitude parameter of the device's current position
	 * @param lng the longitude parameter of the device's current position
	 * @param radius the radius of area from the current position that will have
	 * 		  the nearby stops generated.
	 */
	public void requestStopsNear(double lat, double lng, int radius) {

		String urlString = "http://deco3801-010.uqcloud.net/stopsnearby.php?lat="
				+ lat + "&lng=" + lng + "&rad=" + radius;
		Log.d("urlString: ", urlString);
		JSONRequest request = new JSONRequest();
		request.setListener(this);
		request.execute(urlString);
		state = State.STOPS_NEAR;
		isLoading = true;

	}

	/**
     * A method that defines what should be done after the PHP
     * request is completed. Overrides the networkRequestCompleted
     * method of the JSONRequest.NetworkListener interface.
     * The method will add markers representing the nearby stops
     * to the map.
     *
     * @param result The String result from the PHP service request.
     */
	@Override
	public void networkRequestCompleted(String result) {
		isLoading = false;
		this.result = result;

		if (state == State.STOPS_NEAR) {
			
			// Remove old stops from map
			for (Marker stopMarker : stopMarkers) {
				Log.d("Location", "removing marker");
				stopMarker.setVisible(false);
				stopMarker.remove();

			}
			stopMarkers.clear();
			
			// Add new stop markers to map
			stops = getStopsNear();
			ArrayList<String> usedParentIds = new ArrayList<String>();
			if (stops != null) {
				for (Stop stop : stops) {
					int serviceType = stop.getServiceType();
					
					if (stop.hasParent()) {
						//stops with parent must only be added as markers if not
						//there's not already a stop added for that position
						boolean isAlreadyUsed = false;
						for (String str : usedParentIds) {
							if (str.equals(stop.getParentId())) {
								isAlreadyUsed = true;
								break;
							}
						}
						
						if (!isAlreadyUsed) {
							
							// Get just the parent stop name, without the "LM:" thing
							String stopName = stop.getParentId().split(":")[2];
							
							Marker m = map.addMarker(new MarkerOptions()
									.position(stop.getParentPosition())
									.title(stopName)
									.snippet("Click here for more")
									.icon(BitmapDescriptorFactory.fromResource(markerIcons[serviceType-1])));
							stopMarkers.add(m);
							stopMarkersMap.put(m, stop);
							
						}
					} else {
						
						//the stop doesn't have parent. Simply add to map.
						Marker m = map.addMarker(new MarkerOptions()
								.position(stop.getPosition())
								.title(stop.getDescription())
								.snippet("Click here for more")
								.icon(BitmapDescriptorFactory.fromResource(markerIcons[serviceType-1])));
						stopMarkers.add(m);
						stopMarkersMap.put(m, stop);
						
					}

				}
			} 
		}

	}

	/**
	* @return if waiting for response from server
	**/
	public boolean isLoading() {
		return isLoading;
	}

	/**
     * A method to process the received stops nearby data. 
     * The method create a new Stop object for each nearby stop
     * and put it in an ArrayList.
     *
     * @return ArrayList<Stop> ArrayList containing all the nearby stops.
     */
	public ArrayList<Stop> getStopsNear() {
		ArrayList<Stop> output = new ArrayList<Stop>();
		//check the correct request was sent in the first place
		if (state == State.STOPS_NEAR) {
	
			/*Use JSON-simple to parse the result. The format (of parts used) is below:
			* {Stops: [StopId, Description, Position: {Lat, Lng}, HasParentLocation,
			* ParentLocation: {Id, Position: {Lat, Lng}}, Routes: [Code, Name]]}
			*/
			Object obj = JSONValue.parse(result);
			//try {
				
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
						stop.addRoute(new Route((String)route.get("Code"), (String)route.get("Name"), (Long)route.get("Vehicle")));
					}
					output.add(stop);
				}
				return output;
			//} catch (Exception e) {
				//No stops were found for the given location, or there was some network error
				//Log.d("Location", e.toString());
				//return null;
			//}/
		} else {
			return null;
		}
	}

	/**
     * A method to get the stop information of a specific marker on the map.
     *
     * @return Stop the Stop object that is represented by the marker.
     */
	public Stop getIdOfMarker(Marker marker) {
		if (stopMarkersMap.containsKey(marker)) {
			return stopMarkersMap.get(marker);
		} else {
			return null;
		}
	}

	/**
     * A method to get all the platforms that is grouped together as 1 stop.
     * The platform is represented with a Stop object that has another
     * stop as the parent.
     * 
     * @return ArrayList<Stop> ArrayList containing all the nearby stops.
     */
	public ArrayList<Stop> getStopsFromParent(Stop stop) {
		ArrayList<Stop> output = new ArrayList<Stop>();
		String pId = stop.getParentId();
		for (Stop s : stops) {
			if (s.hasParent()) {
				if (s.getParentId().equals(pId)) {
					output.add(s);
				}
			}
		}
		return output;
	}
}
