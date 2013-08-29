package com.example.translinkmobile;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

public class JSONRequest extends AsyncTask<String, Void, String>{
	/**
	 * An object oriented attempt at making requests to our web server
	 */
	
		public interface NetworkListener {
			void networkRequestCompleted(String result);
		}
	 
		private NetworkListener _listener;
	 
		public void setListener(NetworkListener listener) {
			_listener = listener;
		}
	 
	    @Override
	    protected String doInBackground(String... urlstrings) {
	    	String str = new String();

		    //URL url;
		    String urlStr = urlstrings[0];
		    Log.d("JSONRequest", "received URL: " + urlStr);
		    HttpClient client = new DefaultHttpClient();
		    HttpGet get = new HttpGet(urlStr);
		    try {
		    	Log.d("JSONRequest", "making request");
		        HttpResponse response = client.execute(get);
		        
		        StatusLine statusLine = response.getStatusLine();
		        int statusCode = statusLine.getStatusCode();

		        if (statusCode == 200) {
		        	Log.d("JSONRequest", "status200");
		        	BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		        	String line ="";
		        	while ((line = rd.readLine()) != null) {
		        		str += line;
		        	}
		        }
		    } catch (Exception e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
		    Log.d("JSONRequest", "Output = "+str);
	        return str;
	    }
	 
	    @Override
	    protected void onPostExecute(String result) {
	    	Log.d("JSONRequest", "postexecture");
	        super.onPostExecute(result);
	 
	        if(_listener != null) {
	        	_listener.networkRequestCompleted(result);
	        }
	    }
	}