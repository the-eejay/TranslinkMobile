package com.example.translinkmobile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.example.translinkmobile.HttpRequest.NetworkListener;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity1 extends Activity implements NetworkListener{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 
        String urlString = new String("http://www.google.com");

        //Create and make HTTP request
        HttpRequest request = new HttpRequest();
		request.setListener(this);
		request.execute(urlString);
	}

	public void networkRequestCompleted(String result) {
		if (result == null) {
			return;
		}
		
		TextView tv = new TextView(this);
		Toast.makeText(this, result, Toast.LENGTH_LONG).show();
		tv.setText(result);
		
	}
}