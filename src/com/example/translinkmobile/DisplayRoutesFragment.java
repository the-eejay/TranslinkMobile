package com.example.translinkmobile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * This class is the fragment that shows the service codes and estimated
 *	arrival time for each service in a stop.
 *
 * @author Transponders
 * @version 1.0
 */
public class DisplayRoutesFragment extends Fragment {
	
	public final static String ARGS_SELECTED_STOPS = "SELECTED_STOPS";
	private List<String> lines = new ArrayList<String>();
	//private HashMap<Route, String> routeMap = new HashMap<Route, String>();
	private ListView listView;
	private ArrayList<Stop> stops;
	private RouteDataLoader routeLoader;
	private ArrayAdapter<String> adapter; 
	private HashMap<Integer, String> positionRouteMap;
	FragmentManager manager;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 manager = getActivity().getSupportFragmentManager();
		View view = inflater.inflate(R.layout.activity_timetable, container, false);
		
		listView = (ListView) view.findViewById(R.id.listview);
		listView.setBackgroundColor(Color.WHITE);
		listView.setCacheColorHint(Color.TRANSPARENT);
		
		positionRouteMap = new HashMap<Integer, String>();
		stops = ((NearbyStops) getActivity()).getSelectedStops();	
		
		makeLines();
		showLines();
		
		routeLoader = new RouteDataLoader(lines, adapter);
		routeLoader.requestRouteTimes(stops);
		
		return view;
	}	
	
	/**
     * A method to set the the content of the list into the ListView. 
     *
     */
    public void showLines() 
    {
        adapter = new ArrayAdapter<String>(
        		getActivity().getApplicationContext(), R.layout.route_list_item, lines);
        
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
        		//Set selected route in NearbyStops then change view back to it
        		String routeCode = positionRouteMap.get(pos);
        		((NearbyStops)getActivity()).setSelectedRoute(routeCode);
        	}
        });
    }
    
    /**
     * A method to initialize the content of the list, that is the route code and
     * the estimated time of arrival for each route.
     * 
     */
    private void makeLines()
    {
    	if (stops.size()>1) {
    		//Looking at a group of stops
    		ArrayList<String> routeIdsAlready = new ArrayList<String>();
    		for (Stop stop: stops) {
    			ArrayList<Route> routes = stop.getRoutes();
    			for (Route route: routes) {
	    			boolean foundRouteIdMatch = false;
	    			for (String routeIdAlready: routeIdsAlready) {
	    				if (routeIdAlready.equals(route.getCode())) {
	    					foundRouteIdMatch = true;
	    					break;
	    				}
	    			}
	    			if (!foundRouteIdMatch) {
	    				routeIdsAlready.add(route.getCode());
	    				lines.add(route.getCode() + "\t\t");
	    				positionRouteMap.put(lines.size()-1, route.getCode());
	    			}
    			}
    		}
    	} else {
    		ArrayList<Route> routes = stops.get(0).getRoutes();
    		for (int i=0; i<routes.size(); i++) {
    			String code = routes.get(i).getCode();
    			lines.add(code + "\t\t");
    			positionRouteMap.put(lines.size()-1, code);
    			//routeMap.put(routes.get(i), code);
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
}
