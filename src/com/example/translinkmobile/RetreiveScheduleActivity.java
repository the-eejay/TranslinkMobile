package com.example.translinkmobile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
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
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class RetreiveScheduleActivity extends Activity {
	private List<String> lines = new ArrayList<String>();
	private static final String DEBUG_TAG = "HttpExample";
	private ListView listView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timetable);
		
		listView = (ListView) findViewById(R.id.listview);
		listView.setBackgroundColor(Color.BLACK);
		listView.setCacheColorHint(Color.TRANSPARENT);
		
		checkConnection();
	}
	
	public void checkConnection() {
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		String url = "http://translink.com.au/resources/about-translink/reporting-and-publications/open-data/gtfs/seq.zip";
		
		if (networkInfo != null && networkInfo.isConnected())
        {
        	new DownloadWebpageTask().execute(url);

        }
        else 
        {
        	Context context = getApplicationContext();
			Toast toast = Toast.makeText(context, "No network connection!", Toast.LENGTH_LONG);
			toast.show();
        }
	}
	
	private class DownloadWebpageTask extends AsyncTask<String, Void, String> 
    {
        @Override
        protected String doInBackground(String... urls) 
        {
              
            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) 
        {
            final StableArrayAdapter adapter = new StableArrayAdapter(
            		getApplicationContext(), android.R.layout.simple_list_item_1, lines);
            
            listView.setAdapter(adapter);
        }
        
        private String downloadUrl(String myurl) throws IOException
        {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;
                
            try {
                //URL url = new URL(myurl);
                //HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //conn.setReadTimeout(10000 /* milliseconds */);
                //conn.setConnectTimeout(15000 /* milliseconds */);
                //conn.setRequestMethod("GET");
                //conn.setDoInput(true);
                // Starts the query
                //conn.connect();
                //int response = conn.getResponseCode();
                //Log.d(DEBUG_TAG, "The response is: " + response);
                //is = conn.getInputStream();

                // Convert the InputStream into a string
                //String contentAsString = readIt(is, len);
                //return contentAsString;
            	
            	File root = Environment.getExternalStorageDirectory();
            	URL u = new URL(myurl);
            	HttpURLConnection c = (HttpURLConnection) u.openConnection();
            	c.setRequestMethod("GET");
            	c.setDoOutput(true);
            	c.connect();
            	
            	int lengthOfFile = c.getContentLength();
            	FileOutputStream f= new FileOutputStream(new File(root + "/download/", "gtfs_seq.zip"));

                is = c.getInputStream();

                byte[] buffer = new byte[1024];
                int len1 = 0;
                long total = 0;

                while ((len1 = is.read(buffer)) > 0) {
                    total += len1; //total = total + len1
                    //publishProgress("" + (int)((total*100)/lenghtOfFile));
                    f.write(buffer, 0, len1);
                }
                f.close();
                
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
            } 
            finally 
            {
                if (is != null) {
                    is.close();
                } 
            }
            return "";
        }
        
        public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
            Reader reader = null;
            reader = new InputStreamReader(stream, "UTF-8");        
            //char[] buffer = new char[len];
            //reader.read(buffer);
            
            BufferedReader buf = new BufferedReader(reader);
            String line;            
            StringBuilder result = new StringBuilder();
            
            while((line  = buf.readLine()) != null)
            {
            	result.append(line + "\n");
            	lines.add(line);
            }
            
            //return new String(buffer);
            return result.toString();
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
