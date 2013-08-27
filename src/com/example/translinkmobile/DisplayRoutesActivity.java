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
	private HashMap<String, Route> routeMap = new HashMap<String, Route>();
	private static final String DEBUG_TAG = "HttpExample";
	private ListView listView;
	private Stop stop;
	private RouteDataLoader routeLoader;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timetable);
		
		listView = (ListView) findViewById(R.id.listview);
		listView.setBackgroundColor(Color.BLACK);
		listView.setCacheColorHint(Color.TRANSPARENT);
		
		MainApplication app = (MainApplication)getApplicationContext();
		stop = app.getSelectedStop();
		
		routeLoader = new RouteDataLoader(lines, routeMap);
		routeLoader.requestRouteTimes(stop);
		makeLines();
		showLines();
	}
	
	
	
	
    public void showLines() 
    {
        final StableArrayAdapter adapter = new StableArrayAdapter(
        		getApplicationContext(), android.R.layout.simple_list_item_1, lines);
        
        listView.setAdapter(adapter);
    }
    
    private void makeLines()
    {
    	if (stop.hasParent()) {
    		//Will need to do something slightly different here. Not sure how to fix yet. Maybe use the StopParent class afterall?
    	} else {
    		ArrayList<Route> routes = stop.getRoutes();
    		for (int i=0; i<routes.size(); i++) {
    			lines.add(routes.get(i).getCode());
    		}
    	}
    
    }
	private class StableArrayAdapter extends ArrayAdapter<String> {

	    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

	    public StableArrayAdapter(Context context, int textViewResourceId,
	    			List<String> objects) {
	    	super(context, textViewResourceId, objects);
	    	for (int i = 0; i < objects.size(); ++i) {
	    		mIdMap.put(objects.get(i), i);
	    	}
	    }
	
	    @Override
	    public long getItemId(int position) {
	    	String item = getItem(position);
	    	return mIdMap.get(item);
	    }
	
	    @Override
	    public boolean hasStableIds() {
	    	return true;
	    }
	
	}
}
