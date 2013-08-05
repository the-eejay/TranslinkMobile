package com.example.translinkmobile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
 
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
 
import android.os.AsyncTask;
import android.util.Log;
 
public class HttpRequest extends AsyncTask<String, String, String>{
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

	    URL url;
	    try {
	        url = new URL(urlstrings[0]);

	        HttpURLConnection urlConnection = (HttpURLConnection) url
	                .openConnection();

	        InputStream in = urlConnection.getInputStream();

	        InputStreamReader isw = new InputStreamReader(in);

	        int data = isw.read();
	        int i = 0;
	        while (data != -1 && i < 10) {
	            char current = (char) data;
	            data = isw.read();
	            str = str + current;
	            i++;
	        }
	    } catch (Exception e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
        return str;
    }
 
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
 
        if(_listener != null) {
        	_listener.networkRequestCompleted(result);
        }
    }
}