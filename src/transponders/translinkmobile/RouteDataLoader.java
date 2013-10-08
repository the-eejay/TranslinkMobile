package transponders.translinkmobile;

import java.util.ArrayList;
import java.util.Calendar;
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
	//private ArrayList<StopRoute> stopRoutes;
	private ArrayList<StopTrip> stopTrips;
	private ArrayAdapter<String> adapter; 
	//private HashMap<Integer, Route> positionRouteMap;
	private HashMap<Integer, Trip> positionTripMap;
	private ArrayList<Trip> trips;
	
	private List<TextView> firstArrivalTexts, secondArrivalTexts;
	private CountDownLatch lock; //to perform unit tests

	public RouteDataLoader(List<String> list, ArrayAdapter<String> adapter, HashMap<Integer,Trip> positionTripMap) {
		isLoading = false;
		this.list = list;
		stopTrips = new ArrayList<StopTrip>();
		trips = new ArrayList<Trip>();
		this.adapter = adapter;
		this.positionTripMap = positionTripMap;
	}
	
	public RouteDataLoader(List<String> list, ArrayAdapter<String> adapter, List<TextView> firsts, List<TextView> seconds, HashMap<Integer,Trip> positionRouteMap) {
		isLoading = false;
		this.list = list;
		stopTrips = new ArrayList<StopTrip>();
		this.adapter = adapter;
		this.positionTripMap = positionRouteMap;
		
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

		setStopTripTimes();
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
	public void setStopTripTimes() {

		/* Parse the JSON received from the server. The format is shown below for the parts used:
		 * {StopTimetables: [Trips:[DepartureTime, Route: {Code}]]}
		 */
		Object obj = JSONValue.parse(result);
		JSONArray timetables = (JSONArray) ((JSONObject) obj)
				.get("StopTimetables");
		//Log.d("Route", "Found "+timetables.size()+" timetables.");
		for (int h = 0; h < timetables.size(); h++) {
			JSONArray tripsJSON = (JSONArray) ((JSONObject) timetables.get(h))
					.get("Trips");
			//Log.d("Route", "Found "+trips.size()+" trips.");
			for (int i = 0; i < trips.size(); i++) {
				JSONObject time = (JSONObject) tripsJSON.get(i);
				String timestr = (String) time.get("DepartureTime");
				String routecode = (String) ((JSONObject) time.get("Route"))
						.get("Code");
				long direction = (((Long) ((JSONObject) time.get("Route"))
						.get("Direction")));
				String tripId = (String) ( time.get("TripId"));
				//Log.d("RESULT=", timestr);
				Trip trip = null; 
				//Create each StopRoute relationship and give them the times from the JSON
				Log.d("Route", "Found "+stops.size()+" stops.");
				for (Stop stop : stops) {
					ArrayList<Route> routes = stop.getRoutes();
					
					for (Route r : routes) {
						if (r.getCode().equals(routecode) && direction == r.getDirection()) {
							try {
								if (trip == null) {
									trip = new Trip(tripId, r);
								}
								StopTrip st = new StopTrip(stop, trip);
								//Log.d("Route",
								//		"longstr=" + timestr.substring(6, 18));
								Date date = new Date(Long.parseLong(timestr
										.substring(6, 18) )*10);
								st.setTime(date);
								Log.d("Route", "route="+r.getCode()+" date="+date+ " t="+timestr
										.substring(6, 18));
								stopTrips.add(st);
								
								//trip.addStopRoute(sr);
								

							} catch (Exception e) {
								Log.d("Route", "error in parsing JSON date");
							}
						}
					}
				}
				if (trip != null) {
					trips.add(trip);
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
			//int minTimeIndex = 0;
			//int minTimeIndex2 = 0;
			int minStopRouteIndex = 0;
			int minStopRouteIndex2 = 0;
			Long min = 9999999999999l;
			Long min2 = 9999999999999l;
			//Long currTime = System.currentTimeMillis() / 10;
			Calendar c = Calendar.getInstance();
			Long currTime = c.getTimeInMillis();
			Date date = new Date(currTime);
			Log.d("Route", "current time ="+date);
			for (int k = 0; k < stopTrips.size(); k++) {
				StopTrip st = stopTrips.get(k);
				//if (line.contains(sr.getRoute().getCode())) {
				if (positionTripMap.get(i).getRoute() == st.getTrip().getRoute()) {

					//Date time = st.getTime();
					
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
					//for (int j = 0; j < times.size(); j++) {
						Long time = st.getTime().getTime();
						if (time > currTime && time < min2) {
							if (time < min) {
								//minTimeIndex = j;
								minStopRouteIndex = k;
								min = time;
							} else {
								//minTimeIndex2 = j;
								minStopRouteIndex2 = k;
								min2 = time;
							}
							//Log.d("Route", "For ("+sr.getRoute().getCode() + ","+sr.getRoute().getDirection()+") min = "+min+" min2 = "+min2);
						}
					//}
				}
			}

			String str = "";
			// Add result to the line
			if (min < 9999999999999l) {
				long minutes = (min - currTime) / 60000;
				str =  line + "    " + minutes + " minutes until 1st. ";
				
				// Rounding, if seconds > 30, add another minute
				long remainingMilis1 = (min - currTime) %  60000;
				if(remainingMilis1 > 30000)
					minutes += 1;
				
				long remainingMins1 = minutes % 60;
				String minFormat1 = remainingMins1 > 1 ? " Mins" : " Min";
				
				String format = remainingMins1 + minFormat1;
				
				if(minutes >= 60)
				{
					long hour1 = minutes / 60;
					String hourFormat1 = hour1 > 1 ? " Hours " : " Hour ";
					
					format = hour1 + hourFormat1 + remainingMins1 + minFormat1;
				}
				
				firstArrivalTexts.get(i).setText(format);
				Log.d("RouteDataLoader", "first " + format);
				
				if (min2 < 9999999999999l) {
					long minutes2 = (min2 - currTime) / 60000;
					str += minutes2 + " minutes until 2nd.";
					
					// Rounding, if seconds > 30, add another minute
					long remainingMilis2 = (min2 - currTime) %  60000;
					if(remainingMilis2 > 30000)
						minutes2 += 1;
					
					long remainingMins2 = minutes2 % 60;
					String minFormat2 = remainingMins2 > 1 ? " Mins" : " Min";
					
					String format2 = remainingMins2 + minFormat2;
					
					if(minutes2 >= 60)
					{
						long hour2 = minutes2 / 60;
						String hourFormat2 = hour2 > 1 ? " Hours " : " Hour ";
						
						format2 = hour2 + hourFormat2 + remainingMins2 + minFormat2;
					}				
					
					secondArrivalTexts.get(i).setText(format2);
					Log.d("RouteDataLoader", "second " + format2);
				}
				else
				{
					secondArrivalTexts.get(i).setText("End of Service");
				}
			} else {
				str = line + "    End Of Service";
				firstArrivalTexts.get(i).setText("End of Service");
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

	public ArrayList<StopTrip> getStopTrips() {
		return stopTrips;
		
	}
	
	
	
	/*Test methods*/
	public void setCompletedAsyncTasksLatch(CountDownLatch lock) {
		this.lock =lock;
	}
}
