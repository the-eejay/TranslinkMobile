package transponders.translinkmobile;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
	private HashMap<Integer, Route> positionRouteMap;
	
	private List<TextView> firstArrivalTexts, secondArrivalTexts;
	private CountDownLatch lock; //to perform unit tests

	public RouteDataLoader(List<String> list, ArrayAdapter<String> adapter, HashMap<Integer,Route> positionRouteMap) {
		isLoading = false;
		this.list = list;
		stopRoutes = new ArrayList<StopRoute>();
		this.adapter = adapter;
		this.positionRouteMap = positionRouteMap;
	}
	
	public RouteDataLoader(List<String> list, ArrayAdapter<String> adapter, List<TextView> firsts, List<TextView> seconds, HashMap<Integer,Route> positionRouteMap) {
		isLoading = false;
		this.list = list;
		stopRoutes = new ArrayList<StopRoute>();
		this.adapter = adapter;
		this.positionRouteMap = positionRouteMap;
		
		firstArrivalTexts = firsts;
		secondArrivalTexts = seconds;
	}

	/**
     * A method to initialize the request to get the service route times
     * of each stop to the PHP web service. 
     *
     * @param stops ArrayList containing all the nearby stops.
     */
	public void requestRouteTimes(ArrayList<Stop> stops) {
		isLoading = true;
		//Build the retrieve route schedule URL
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
		
		this.result = result;

		setStopRouteTimes();
		addTimesToList();
		isLoading = false;
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
		//Log.d("Route", "Found "+timetables.size()+" timetables.");
		for (int h = 0; h < timetables.size(); h++) {
			JSONArray trips = (JSONArray) ((JSONObject) timetables.get(h))
					.get("Trips");
			//Log.d("Route", "Found "+trips.size()+" trips.");
			for (int i = 0; i < trips.size(); i++) {
				JSONObject time = (JSONObject) trips.get(i);
				String timestr = (String) time.get("DepartureTime");
				String routecode = (String) ((JSONObject) time.get("Route"))
						.get("Code");
				long direction = (((Long) ((JSONObject) time.get("Route"))
						.get("Direction")));
				//Log.d("RESULT=", timestr);

				//Create each StopRoute relationship and give them the times from the JSON
				Log.d("Route", "Found "+stops.size()+" stops.");
				for (Stop stop : stops) {
					ArrayList<Route> routes = stop.getRoutes();
					
					for (Route r : routes) {
						if (r.getCode().equals(routecode) && direction == r.getDirection()) {
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
		Log.d("Route", "adding times to list");
		for (int i = 0; i < list.size(); i++) {
			String line = list.get(i);
			ArrayList<StopRoute> matching = new ArrayList<StopRoute>();
			int minTimeIndex = 0;
			int minTimeIndex2 = 0;
			int minStopRouteIndex = 0;
			int minStopRouteIndex2 = 0;
			Long min = 9999999999999l;
			Long min2 = 9999999999999l;
			Long currTime = System.currentTimeMillis() / 10;
			for (int k = 0; k < stopRoutes.size(); k++) {
				StopRoute sr = stopRoutes.get(k);
				//if (line.contains(sr.getRoute().getCode())) {
				if (positionRouteMap.get(i) == sr.getRoute()) {

					ArrayList<Date> times = sr.getTimes();
					
					// Find the closest scheduled time after the current time
					/*for (int j = 0; j < times.size(); j++) {
						Long time = times.get(j).getTime();
						//Log.d("Route", "time=" + time + " currTime=" + currTime
						//		+ " min=" + min);
						if (time > currTime && time < min) {
							minTimeIndex = j;
							minStopRouteIndex = k;
							min = time;
						}
					}*/
					for (int j = 0; j < times.size(); j++) {
						Long time = times.get(j).getTime();
						if (time > currTime && time < min2) {
							if (time < min) {
								minTimeIndex = j;
								minStopRouteIndex = k;
								min = time;
							} else {
								minTimeIndex2 = j;
								minStopRouteIndex2 = k;
								min2 = time;
							}
							Log.d("Route", "For ("+sr.getRoute().getCode() + ","+sr.getRoute().getDirection()+") min = "+min+" min2 = "+min2);
						}
					}
				}
			}

			String str = "";
			// Add result to the line
			if (min < 9999999999999l) {
				long minutes = (min - currTime) / 10000;
				str =  line + "    " + minutes + " minutes until 1st. ";
				
				String format = minutes + " Mins";
				if(minutes >= 60)
				{
					long hour = minutes / 60;
					long remainingMins = minutes % 60;
					format = hour + " Hour " + remainingMins + " Mins";
				}
				
				firstArrivalTexts.get(i).setText(format);
				Log.d("RouteDataLoader", "setting first textview");
				if (min2 < 9999999999999l) {
					long minutes2 = (min2 - currTime) / 10000;
					str += minutes2 + " minutes until 2nd.";
					
					String format2 = minutes2 + " Mins";
					if(minutes2 >= 60)
					{
						long hour = minutes2 / 60;
						long remainingMins = minutes2 % 60;
						format2 = hour + " Hour " + remainingMins + " Mins";
					}				
					
					secondArrivalTexts.get(i).setText(format2);
					Log.d("RouteDataLoader", "setting second textview");
				}
			} else {
				str = line + "    End Of Service";
				firstArrivalTexts.get(i).setText("End of Service.");
				secondArrivalTexts.get(i).setText("N/A");
			}
			list.set(i, str);
			Log.d("Route", "list(" + i + ")=" + list.get(i));
			adapter.notifyDataSetChanged();
			
		}
		if (lock != null) {
			lock.countDown();
		}
	}

	/*Test methods*/
	public void setCompletedAsyncTasksLatch(CountDownLatch lock) {
		this.lock =lock;
	}
}
