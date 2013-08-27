package com.example.translinkmobile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class DisplayRoutesActivity extends Activity {
	
	private List<String> lines = new ArrayList<String>();
	//private HashMap<Route, String> routeMap = new HashMap<Route, String>();
	private static final String DEBUG_TAG = "HttpExample";
	private ListView listView;
	private ArrayList<Stop> stops;
	private RouteDataLoader routeLoader;
	private ArrayAdapter<String> adapter; 
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timetable);
		
		listView = (ListView) findViewById(R.id.listview);
		listView.setBackgroundColor(Color.BLACK);
		listView.setCacheColorHint(Color.TRANSPARENT);
		
		MainApplication app = (MainApplication)getApplicationContext();
		stops = app.getSelectedStops();
		
		
		makeLines();
		showLines();
		
		routeLoader = new RouteDataLoader(lines, adapter);
		routeLoader.requestRouteTimes(stops);
	}
	
	
	
	
    public void showLines() 
    {
        adapter = new ArrayAdapter<String>(
        		getApplicationContext(), android.R.layout.simple_list_item_1, lines);
        
        listView.setAdapter(adapter);
    }
    
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
	    			}
    			}
    		}
    	} else {
    		ArrayList<Route> routes = stops.get(0).getRoutes();
    		for (int i=0; i<routes.size(); i++) {
    			String code = routes.get(i).getCode();
    			lines.add(code + "\t\t");
    			//routeMap.put(routes.get(i), code);
    		}
    	}
    
    }
	public class CustomArrayAdapter extends ArrayAdapter<String> {

	    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

	    public CustomArrayAdapter(Context context, int textViewResourceId,
	    			List<String> objects) {
	    	super(context, textViewResourceId, objects);
	    	//this.setNotifyOnChange(true);
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
