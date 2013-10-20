package transponders.transmob;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;

import transponders.transmob.NearbyStops.StackState;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class is the fragment that shows the service codes and estimated
 *	arrival time for each service in a stop.
 *
 * @author Transponders
 * @version 1.0
 */
public class DisplayRoutesFragment extends Fragment implements JSONRequest.NetworkListener {
	
	public final static String ARGS_SELECTED_STOPS = "SELECTED_STOPS";
	//private List<String> lines = new ArrayList<String>();
	//private HashMap<Route, String> routeMap = new HashMap<Route, String>();
	private ListView listView;
	private ArrayList<Stop> stops;
	private RouteDataLoader routeLoader;
	private ArrayAdapter<String> adapter; 
	//private HashMap<Integer, Route> positionRouteMap;
	private HashMap<Integer, Trip> positionTripMap;
	private FragmentManager manager;
	private DisplayRoutesFragment thisVar;
	private PullToRefreshScrollView pullToRefreshView;
	
	private CountDownLatch lock;
	
	private DisplayMetrics scale;
	private String selectedStopName;
	private int stopType;
	private TableLayout table;
	private Context tableContext;
	private List<String> services = new ArrayList<String>();
	private List<String> directions = new ArrayList<String>();
	
/*	private List<TextView> firstArrivalTexts = new ArrayList<TextView>();
	private List<TextView> secondArrivalTexts = new ArrayList<TextView>();*/
	
	private ArrayList<Route> availableRoutes = new ArrayList<Route>();
	private ArrayList<Trip> firstTrips = new ArrayList<Trip>();
	private HashMap<Route, Trip> secondTrips = new HashMap<Route, Trip>();
	private JSONRequest request;
	Calendar c = Calendar.getInstance();
	Long currTime = c.getTimeInMillis();
	
	@Override
	public void onCreate(Bundle bundle) {
		Log.d("DisplayRoutes", "DisplayRoutes: onCreate started");
		super.onCreate(bundle);
		//init();
		
		stops = ((NearbyStops) getActivity()).getSelectedStops();	
		stopType = stops.get(0).getServiceType();
		selectedStopName = getArguments().getString(NearbyStops.SELECTED_STOP_NAME);
		Log.d("DisplayRoutes", selectedStopName);
		thisVar = this;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) {
		Log.d("DisplayRoutes", "DisplayRoutes: onCreateView started");
		super.onCreate(savedInstanceState);
		
		getActivity().getActionBar().setTitle("Timetable");
		scale = getActivity().getResources().getDisplayMetrics();
		 manager = getActivity().getSupportFragmentManager();
		/*View view = inflater.inflate(R.layout.activity_timetable, container, false);
		listView = (ListView) view.findViewById(R.id.listview);
		listView.setBackgroundColor(Color.WHITE);
		listView.setCacheColorHint(Color.TRANSPARENT);*/
		 
		View view = inflater.inflate(R.layout.service_timetable, container, false);
		table = (TableLayout) view.findViewById(R.id.service_table);
        tableContext = table.getContext();
        
        TextView title = (TextView) view.findViewById(R.id.stop_name);
        String displayName = selectedStopName;
        
        String[] splittedByStop = selectedStopName.split(", stop ");
        String[] splittedByNear = selectedStopName.split(" near ");
      
        if(splittedByStop.length == 2)
        {
        	displayName = splittedByStop[0] + "\n Stop " + splittedByStop[1];
        }
        else if(splittedByNear.length == 2)
        {
        	displayName = splittedByNear[0] + "\n near " + splittedByNear[1];
        }
        title.setText(displayName);
        
        pullToRefreshView = (PullToRefreshScrollView) view.findViewById(R.id.timetable_scrollview);
        pullToRefreshView.setOnRefreshListener(new OnRefreshListener<ScrollView>(){

        	@Override
			public void onRefresh(final PullToRefreshBase<ScrollView> refreshView) {
        		//new GetDataTask().execute();
        		//init();
        		requestRouteTimes(stops);
			}
        });
		
        //showLines();
        //init();
        //populateTable();
        
        requestRouteTimes(stops);	
		return view;
	}	
	
	private void init() {
		
		Log.d("DisplayRoutes", "Service type: " + stopType);
		
		positionTripMap = new HashMap<Integer, Trip>();
		//makeLines();
		requestRouteTimes(stops);
		/*adapter = new ArrayAdapter<String>(
        		getActivity().getApplicationContext(), R.layout.route_list_item, lines);*/
		
	}
	
	public void populateTable()
	{
		Log.d("populateTable", "POPULATE TABLE CALLED");
		//firstArrivalTexts.clear();
        //secondArrivalTexts.clear();
		
		Trip currentTrip;
		Route currentRoute;
		boolean endedServices = false;
		int numOfEndedServices = availableRoutes.size();
		int endedServiceIndex = 0;
		
        Log.d("populateTable", "Num of first trips: " + firstTrips.size());
		for(int i = 0; i < firstTrips.size() || endedServiceIndex < numOfEndedServices; i++)
		{
			if(i >= firstTrips.size())
				endedServices = true;
			
			TableRow newRow = new TableRow(tableContext);
        	Context rowContext = newRow.getContext();
        	int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, scale);
        	newRow.setMinimumHeight(height);
			
			if(endedServices)
			{
				currentTrip = new Trip(); // currentTrip doesn't matter anymore
				currentRoute = availableRoutes.get(endedServiceIndex);
				newRow.setOnClickListener(new RouteListener(firstTrips.size() + endedServiceIndex));
				endedServiceIndex++;		
			}
			else
			{
				currentTrip = firstTrips.get(i);
				currentRoute = currentTrip.getRoute();
				newRow.setOnClickListener(new RouteListener(i));
			}
			Log.d("populateTable", currentRoute.getDescription());
				
        	// Color bar (column 0)
        	TextView colorBar = new TextView(rowContext);
        	TableRow.LayoutParams param1 = new TableRow.LayoutParams();
            param1.column = 0;
            param1.span = 3;
            colorBar.setLayoutParams(param1);
            colorBar.setHeight(height);
            
            if(stopType == 1)
            	colorBar.setBackgroundResource(R.color.bus_green);
            else if(stopType == 2)
            	colorBar.setBackgroundResource(R.color.train_orange);
            else
            	colorBar.setBackgroundResource(R.color.ferry_blue);
            
            newRow.addView(colorBar);
            
            ///////////////////////////////////////////////////
            
            // Service code & direction (column 1)
            LinearLayout cell2 = new LinearLayout(rowContext);
            cell2.setOrientation(LinearLayout.VERTICAL);
            cell2.setMinimumHeight(height);
            cell2.setGravity(Gravity.CENTER_VERTICAL);
 
            TableRow.LayoutParams param2 = new TableRow.LayoutParams();
            param2.column = 1;
            param2.span = 30;
            param2.weight = 1;
            cell2.setLayoutParams(param2);
            
            TextView serviceCode = new TextView(rowContext);
            serviceCode.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            serviceCode.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
            serviceCode.setPadding(30, 0, -10, 0);            	
      
            TextView direction = new TextView(rowContext);
            direction.setPadding(30, 0, -10, 0);
            
            // Special code & direction for trains
            if(stopType == 2)
            {	
            	String originalName = currentRoute.getDescription();
				String[] getTo = originalName.split(" to ");
				String[] getFrom = getTo;
				
				String to = getTo[1];
				if(to.contains(" - "))
				{
					to = to.split(" - ")[0];
				}
				
				if(getFrom[0].contains(" via "))
				{
					getFrom = getFrom[0].split(" via ");
				}
				
				serviceCode.setText(to);
				serviceCode.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
				direction.setText("From " + getFrom[0]);
			}
            else
            {
            	serviceCode.setText(currentRoute.getCode());
            	direction.setText(currentRoute.getDirectionAsString());
            }

            cell2.addView(serviceCode);
            cell2.addView(direction);
            
            newRow.addView(cell2);
            
            ///////////////////////////////////////////////////
            
            // First & Second arrival (column 2)
            LinearLayout cell3 = new LinearLayout(rowContext);
            cell3.setOrientation(LinearLayout.VERTICAL);
            cell3.setMinimumHeight(height);
            cell3.setGravity(Gravity.CENTER_VERTICAL);
 
            TableRow.LayoutParams param3 = new TableRow.LayoutParams();
            param3.column = 2;
            param3.span = 50;
            param3.weight = 1;
            cell3.setLayoutParams(param3);
            
            TextView firstText = new TextView(rowContext);
            TextView secondText = new TextView(rowContext);
            firstText.setPadding(0, 0, 0, 0);
            secondText.setPadding(3, 0, 0, 0);
            
            firstText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            firstText.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
            
            if(stopType == 1)
            	firstText.setTextColor(getResources().getColor(R.color.bus_green));
            else if(stopType == 2)
            {
            	firstText.setTextColor(getResources().getColor(R.color.train_orange));
            	firstText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            	firstText.setPadding(40, 0, 0, 0);
                secondText.setPadding(43, 0, 0, 0);
            }
            else
            	firstText.setTextColor(getResources().getColor(R.color.ferry_blue));       
            
            firstText.setText(formatTime(currentTrip.getDepartureTime()));
            
            Trip currentSecondTrip = secondTrips.get(currentRoute);
            if(currentSecondTrip != null)
            	secondText.setText(formatTime(currentSecondTrip.getDepartureTime()));
            else
            	secondText.setText("End of Service");
            
            if(endedServices)
            	secondText.setText("N/A");
            
            cell3.addView(firstText);
            cell3.addView(secondText);
            //firstArrivalTexts.add(firstText);
            //secondArrivalTexts.add(secondText);
            
            newRow.addView(cell3);
            
            ////////////////////////////////////////////////////////////////////////
            
            // Mini arrow (column 3)
            LinearLayout cell4 = new LinearLayout(rowContext);
            cell4.setOrientation(LinearLayout.VERTICAL);
            cell4.setMinimumHeight(height);
            cell4.setGravity(Gravity.CENTER_VERTICAL);
            
            TableRow.LayoutParams param4 = new TableRow.LayoutParams();
            param4.column = 3;
            param4.span = 5;
            param4.weight = 1;
            cell4.setLayoutParams(param4);
            
            ImageView miniArrow = new ImageView(rowContext);
            miniArrow.setImageResource(R.drawable.mini_arrow);
            
            cell4.addView(miniArrow);
            newRow.addView(cell4);
            
            ////////////////////////////////////////////////////////////////////////
            
            View separatorLine = new View(tableContext);
            separatorLine.setBackgroundColor(getResources().getColor(R.color.separator_line));
            separatorLine.setPadding(0, 0, 0, 0);
            TableLayout.LayoutParams lineParam = new TableLayout.LayoutParams();
            lineParam.height = 2;
            separatorLine.setLayoutParams(lineParam);
            
            newRow.setBackgroundResource(R.drawable.selector);
            table.addView(newRow);
            table.addView(separatorLine);
            
            // If the route is still available, remove it from the list of all routes,
            // leaving the ended services in the list.
            if(!endedServices && availableRoutes.remove(currentRoute))
            	Log.d("populateTable", currentRoute.getCode() + " REMOVED");
            
            // After the last iteration, start processing the ended routes.
            if(i == firstTrips.size() - 1 && !endedServices)
            {
            	Log.d("populateTable", "FINISHED LOOPING FIRSTTRIPS");
            	
            	numOfEndedServices = availableRoutes.size();
            	i -= numOfEndedServices;
            	endedServices = true;
            }
		}
		
		//routeLoader = new RouteDataLoader(lines, adapter, positionRouteMap);

		/*routeLoader = new RouteDataLoader(firstArrivalTexts, secondArrivalTexts, positionTripMap);
		routeLoader.requestRouteTimes(stops);
		
		if (lock != null) {
			lock.countDown();
		}*/
	}
	
	private class RouteListener implements OnClickListener
    {		
		int pos;
		
		public RouteListener(int selectedPosition)
		{
			pos = selectedPosition;
		}
		
    	public void onClick(View v) 
		{
    		NearbyStops nearbyStops = (NearbyStops) getActivity();
    		
    		if(pos < firstTrips.size())
    		{
    			Log.d("RouteListener", "Pos: " + pos + " firstTrips.size(): " + firstTrips.size());
        		Trip trip1 = firstTrips.get(pos);
        		Trip trip2 = secondTrips.get(trip1.getRoute());
        		nearbyStops.setSelectedTrip(trip1, trip2);
        		
        		// Should move these steps to NearbyStops itself and add check whether need to refresh or not
                nearbyStops.removeAllMarkers();
                nearbyStops.showTrip();
    		}
    		else
    		{
    			Log.d("RouteListener", "Pos: " + pos + " firstTrips.size(): " + firstTrips.size());
    			nearbyStops.setSelectedRoute(availableRoutes.get(pos - firstTrips.size()));
    			nearbyStops.removeAllMarkers();
    			nearbyStops.showRoute();
    		}
    		
    		nearbyStops.addStateToStack(StackState.ShowRoute);  
     		FragmentTransaction transaction = manager.beginTransaction();
     		transaction.remove(thisVar);
            transaction.addToBackStack(null);
     		transaction.commit();
		}
    }
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	/**
     * A method to set the the content of the list into the ListView. 
     *
     */
    public void showLines() 
    {
    	listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
        		//Set selected route in NearbyStops then change view back to it
        		Trip trip = positionTripMap.get(pos);
        		NearbyStops act = (NearbyStops)getActivity();
        		act.setSelectedTrip(trip);
        		//Fragment fragment = null;
        		/*if (act.getMap2Fragment() == null) {
        			Log.d("Drawer", "map2 is NULL");
        			fragment = new ShowRouteFragment();
        			act.setMap2Fragment((ShowRouteFragment)fragment);
        		} else {
        			Log.d("Drawer", "map2 already exists");
        			fragment = act.getMap2Fragment();
        		}*/
        		
        		 NearbyStops nearbyStops =  (NearbyStops)getActivity();
                 // Should move these steps to NearbyStops itself and add check whether need to refresh or not
                 nearbyStops.removeAllMarkers();
                 nearbyStops.showTrip();
                 nearbyStops.addStateToStack(StackState.ShowRoute);
                 
        		FragmentTransaction transaction = manager.beginTransaction();
        		 transaction.remove(thisVar);
                transaction.addToBackStack(null);
        		transaction.commit();
                
                
        		/*
        		FragmentTransaction transaction = manager.beginTransaction();
        		transaction.remove(manager.findFragmentById(R.id.content_frame));
        		transaction.commit();
        		manager.executePendingTransactions();
        		transaction = manager.beginTransaction();
        		transaction.add(R.id.content_frame, fragment);
        		transaction.addToBackStack(null);
        		transaction.commit();*/
        	}
        });
    	adapter.notifyDataSetChanged();
    }
    
    /**
     * A method to initialize the content of the list, that is the route code and
     * the estimated time of arrival for each route.
     * 
     */
    private void makeLines()
    {
    	Log.d("Route", "Making Lines.  stops.size()="+stops.size());
    	services.clear();
    	directions.clear();
    	
    	if (stops.size()>1) {
    		//Looking at a group of stops
    		//ArrayList<String> routeIdsAlready = new ArrayList<String>();
    		for (Stop stop: stops) {
    			
    			ArrayList<Route> routes = stop.getRoutes();
    			Log.d("Route", "stop "+stop.getDescription()+" has routes.size()=" + routes.size());
    			for (Route route: routes) {
	    			/*boolean foundRouteIdMatch = false;
	    			for (String routeIdAlready: routeIdsAlready) {
	    				if (routeIdAlready.equals(route.getCode())) {
	    					foundRouteIdMatch = true;
	    					break;
	    				}
	    			}
	    			if (!foundRouteIdMatch) {
	    				routeIdsAlready.add(route.getCode());
	    				lines.add(route.getCode() + "\t\t");
	    				positionRouteMap.put(lines.size()-1, route);

	    			}*/

    				//lines.add(route.getCode()+"\t"+directionStr);
    				//positionRouteMap.put(lines.size()-1, route);
    							
    				// Special route code and direction for trains
    				if(stopType == 2)
    				{
    					String name = route.getDescription();
    					String[] getTo = name.split(" to ");
    					String[] getFrom = getTo;
    					
    					String to = getTo[1];
    					if(to.contains(" - "))
    					{
    						to = to.split(" - ")[0];
    					}
    					
    					if(getFrom[0].contains(" via "))
    					{
    						getFrom = getFrom[0].split(" via ");
    					}
    					
    					services.add(to);
    					directions.add("From " + getFrom[0]);
    				}
    				else
    				{
    					String directionStr = route.getDirectionAsString();
    					services.add(route.getCode());
        				directions.add(directionStr);
    				}
    				
    				//positionTripMap.put(services.size()-1, new Trip("NOT_AN_ACTUAL_TRIP", route));
    			}
    		}
    	} else {
    		ArrayList<Route> routes = stops.get(0).getRoutes();
    		Log.d("Route", "stop "+stops.get(0).getDescription()+" has routes.size()=" + routes.size());
    		for (int i=0; i<routes.size(); i++) {
    			String code = routes.get(i).getCode();
    			String directionStr = routes.get(i).getDirectionAsString();

    			//lines.add(code + "\t"+directionStr);
    			//positionRouteMap.put(lines.size()-1, routes.get(i));
    			//routeMap.put(routes.get(i), code);
    			
    			services.add(code);
				directions.add(directionStr);
				//positionTripMap.put(services.size()-1, new Trip("NOT_AN_ACTUAL_TRIP", routes.get(i)));
    		}
    	}
    }
    
    /**
     * The ArrayAdapter class for the container of the route and estimated
     * time of arrival data.
     *
     * @author Transponders
     * @version 1.0
     */
	public class CustomArrayAdapter extends ArrayAdapter<String> {

	    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

	    public CustomArrayAdapter(Context context, int textViewResourceId,
	    			List<String> objects) {
	    	super(context, textViewResourceId, objects);
	    	for (int i = 0; i < objects.size(); ++i) {
	    		mIdMap.put(objects.get(i), i);
	    	}
	    }
	
	    @Override
	    public long getItemId(int position) {
	    	Log.d("AdapterPosition", Integer.toString(position));
	    	String item = getItem(position);
	    	Log.d("AdapterPosition", "mIdMap="+mIdMap);
	    	Log.d("AdapterPosition", "item="+item);
	    	return mIdMap.get(item);
	    }
	
	    @Override
	    public boolean hasStableIds() {
	    	return false;
	    }
	
	}
	
	/*Testing methods*/
	public ArrayAdapter<String> getAdapter() {
		return adapter;
	}
	public RouteDataLoader getRouteDataLoader() {
		return routeLoader;
	}
	public void setCompletedAsyncTasksLatch(CountDownLatch lock) {
		this.lock =lock;
	}
	
	public void requestRouteTimes(ArrayList<Stop> stops) {
		//Build the retrieve route schedule URL
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

		request = new JSONRequest();
		request.setListener(this);
		request.execute(urlString);
	}
	
	private String formatTime(Long timeInMillis)
	{
		if(timeInMillis == null) 
			return "End of Service";
		
		long minutes = (timeInMillis - currTime) / 60000;
		
		// Rounding, if seconds > 30, add another minute
		long remainingMilis1 = (timeInMillis - currTime) %  60000;
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
		
		return format;
	}

	@Override
	public void networkRequestCompleted(String result) 
	{	
		try
		{
			loadSortedTripTimes(result);
			table.removeAllViews();
			populateTable();
		}
		catch(NullPointerException e)
		{
			Toast.makeText(getActivity(), "No internet connection!", Toast.LENGTH_SHORT).show();
		}
		finally
		{
			pullToRefreshView.onRefreshComplete();
		}
	}
	
	public void loadSortedTripTimes(String result) {
		/* Parse the JSON received from the server. The format is shown below for the parts used:
		 * {StopTimetables: [Trips:[DepartureTime, Route: {Code}]]}
		 */
		
		c = Calendar.getInstance();
		currTime = c.getTimeInMillis();
		
		availableRoutes.clear();
		firstTrips.clear();
		secondTrips.clear();
		
		Object obj = JSONValue.parse(result);
		JSONArray timetables = (JSONArray) ((JSONObject) obj)
				.get("StopTimetables");
		Log.d("setStopTripTimes", "Found "+timetables.size()+" timetables.");
		for (int h = 0; h < timetables.size(); h++) {
			JSONArray tripsJSON = (JSONArray) ((JSONObject) timetables.get(h))
					.get("Trips");
			
			JSONArray routes = (JSONArray) ((JSONObject) timetables.get(h))
					.get("Routes");
			
			for(int i = 0; i < routes.size(); i++)
			{
				String code = (String) ((JSONObject) routes.get(i)).get("Code");
				String name = (String) ((JSONObject) routes.get(i)).get("Name");
				Long type = (Long) ((JSONObject) routes.get(i)).get("Vehicle");
				Long direction = (Long) ((JSONObject) routes.get(i)).get("Direction");
				
				availableRoutes.add(new Route(code, name, type, direction));
			}
		
			HashMap<String, Integer> tripCount = new HashMap<String, Integer>();
			//Log.d("Route", "Found "+trips.size()+" trips.");
			for (int i = 0; i < tripsJSON.size(); i++) {
				JSONObject time = (JSONObject) tripsJSON.get(i);
				String timestr = (String) time.get("DepartureTime");
				String routecode = (String) ((JSONObject) time.get("Route"))
						.get("Code");
				String routeName = (String) ((JSONObject) time.get("Route"))
						.get("Name");
				Long type = (Long) ((JSONObject) time.get("Route"))
						.get("Vehicle");
				long direction = (((Long) ((JSONObject) time.get("Route"))
						.get("Direction")));
				String tripId = (String) ( time.get("TripId"));
				
				Date date1 = new Date(Long.parseLong(timestr
						.substring(6, 18)) * 10);
				
				long departureTime = date1.getTime();
				if(departureTime > currTime)
				{
					if(!tripCount.containsKey(routecode))
						tripCount.put(routecode, 1);
					else
						tripCount.put(routecode, tripCount.get(routecode) + 1);
					
					Route route = new Route(routecode, routeName, type, direction);
					if(tripCount.get(routecode) == 1)
					{
						Log.d("loadSortedTripTimes", routecode + " " + direction + " " + departureTime + " arrival " + tripCount.get(routecode));
						firstTrips.add(new Trip(tripId, route, departureTime));
					}
					if(tripCount.get(routecode) == 2)
					{
						Log.d("loadSortedTripTimes", routecode + " " + direction + " " + departureTime + " arrival " + tripCount.get(routecode));
						secondTrips.put(route, new Trip(tripId, route, departureTime));
					}
				}
			}
			
			if(stops.size() > 1)
			{
				Log.d("loadSortedTripTimes", "GROUPED STOPS, DO ADDITIONAL SORTING");
				Collections.sort(firstTrips);
			}
		}
	}
	
	@Override
	public void onStop() 
	{
		// Prevents crash when user hits back before the asynctask is finished
	    if (this.request != null && this.request.getStatus() == Status.RUNNING) 
	    	this.request.cancel(true);
	    
	    super.onStop();
	}
}
