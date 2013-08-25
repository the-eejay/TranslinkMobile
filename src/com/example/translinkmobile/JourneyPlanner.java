package com.example.translinkmobile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class JourneyPlanner extends Activity implements JSONRequest.NetworkListener {
	/** Gets the location IDs from translink's resolve API and sends them to JourneyMap class */
	
	private EditText fromText;
	private EditText destText;
	
	private LocationManager locManager;
	private Location loc;
	private List<String> idList = new ArrayList<String>();
	int requests = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.journey_planner);
		
		fromText = (EditText) findViewById(R.id.fromLocation);
		destText = (EditText) findViewById(R.id.toLocation);
		
		Button button = (Button) findViewById(R.id.sendDestButton);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				getLocIds();
				
			}
		});
	}
	
	private void getLocIds() {
		getLocationId(fromText.getText().toString());
		getLocationId(destText.getText().toString());
	}
	
	private void getLocationId(String loc) {
		
		String url = "http://deco3801-010.uqcloud.net/resolve.php?input=" + Uri.encode(loc);
		JSONRequest request = new JSONRequest();
		request.setListener(this);
		request.execute(url);
	}


	@Override
	public void networkRequestCompleted(String result) {
		idList.add(result);
		if (idList.size() == 2) {
			Intent intent = new Intent(getApplicationContext(), JourneyMap.class);
			intent.putExtra("locs", idList.toArray());
			startActivity(intent);
		}
	}
}
