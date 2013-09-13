package transponders.translinkmobile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.util.Log;
import android.widget.ArrayAdapter;

/**
 * The class that will load the route data from Translink OPIA API. It
 * implements the JSONRequest.NetworkListener to establish the connection to
 * Translink OPIA API.
 * 
 * @author Transponders
 * @version 1.0
 */
public class RouteDataLoader implements JSONRequest.NetworkListener {

	private boolean isLoading; //isLoading - if currently performing async task
	private String result;
	private ArrayList<Stop> stops;
	private List<String> list; //list - a list of Strings for use by the ArrayAdapter
	private ArrayList<StopRoute> stopRoutes;
	private ArrayAdapter<String> adapter; 

	public RouteDataLoader(List<String> list, ArrayAdapter<String> adapter) {
		isLoading = false;
		this.list = list;
		stopRoutes = new ArrayList<StopRoute>();
		this.adapter = adapter;
	}

	/**
     * A method to initialize the request to get the service route times
     * of each stop to the PHP web service. 
     *
     * @param stops ArrayList containing all the nearby stops.
     */
	public void requestRouteTimes(ArrayList<Stop> stops) {
		
		//Build the retreive route schedule URL
		this.stops = stops;
		String stopString = "";
		boolean isFirst = true;
		for (Stop stop : stops) {
			if (isFirst) {
				stopString = stopString + stop.getId();
				isFirst = false;
			} else {
				stopString = stopString + "%2C" + stop.getId();
			}
		}
		
		String urlString = "http://deco3801-010.uqcloud.net/routeschedule.php?stop="
				+ stopString;

		JSONRequest request = new JSONRequest();
		request.setListener(this);
		request.execute(urlString);
		isLoading = true;

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

		setStopRouteTimes();
		addTimesToList();
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
	public void setStopRouteTimes() {

		/* Parse the JSON received from the server. The format is shown below for the parts used:
		 * {StopTimetables: [Trips:[DepartureTime, Route: {Code}]]}
		 */
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
				//Log.d("RESULT=", timestr);

				//Create each StopRoute relationship and give them the times from the JSON
				for (Stop stop : stops) {
					ArrayList<Route> routes = stop.getRoutes();

					for (Route r : routes) {
						if (r.getCode().equals(routecode)) {
							try {
								StopRoute sr = new StopRoute(stop, r);
								//Log.d("Route",
								//		"longstr=" + timestr.substring(6, 18));
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

		}
	}

	/**
     * A method to format the departure time and add it to the list adapter.
     *
     */
	public void addTimesToList() {
		
		for (int i = 0; i < list.size(); i++) {
			String line = list.get(i);
			ArrayList<StopRoute> matching = new ArrayList<StopRoute>();
			int minTimeIndex = 0;
			int minStopRouteIndex = 0;
			Long min = 9999999999999l;
			Long currTime = System.currentTimeMillis() / 10;
			for (int k = 0; k < stopRoutes.size(); k++) {
				StopRoute sr = stopRoutes.get(k);
				if (line.contains(sr.getRoute().getCode())) {

					ArrayList<Date> times = sr.getTimes();
					
					// Find the closest scheduled time after the current time
					for (int j = 0; j < times.size(); j++) {
						Long time = times.get(j).getTime();
						//Log.d("Route", "time=" + time + " currTime=" + currTime
						//		+ " min=" + min);
						if (time > currTime && time < min) {
							minTimeIndex = j;
							minStopRouteIndex = k;
							min = time;
						}
					}
				}
			}

			// Add result to the line
			if (min < 9999999999999l) {
				long minutes = (min - currTime) / 10000;
				list.set(i, line + "    " + minutes + " minutes until arrival");
			} else {
				list.set(i, line + "    End Of Service");
			}
			Log.d("Route", "list(" + i + ")=" + list.get(i));
			adapter.notifyDataSetChanged();
		}
	}

}
