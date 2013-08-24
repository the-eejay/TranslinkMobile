package com.example.translinkmobile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class JourneyPlanner extends Activity {
	
	private EditText toLocation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.journey_planner);
		
		toLocation = (EditText) findViewById(R.id.toLocation);
		toLocation.setOnEditorActionListener(new OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        boolean handled = false;
		        if (actionId == EditorInfo.IME_ACTION_SEND) {
		            requestPlan();
		            handled = true;
		        }
		        return handled;
		    }
		});
	}
	
	private void requestPlan() {
		Location loc = getCurrentLoc();
		if (loc == null) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Your location could not be established.  " +
					"Would you like to go to your GPS Settings?")
				   .setPositiveButton("GPS Settings", new OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
						
					}
					   
				   })
				   .setNegativeButton("Cancel", new OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
							finish();
						}
				   });
			final AlertDialog alert = builder.create();
			alert.show();
		} else {
			String fromLocationId = getLocationId(loc);
			String destinationId = getLocationId(toLocation.getText().toString());
		}
	}
	
	private Location getCurrentLoc() {
		/** Loops through all possible providers to get a location.
		 * Returns a null location if location was not found
		 */
		LocationManager mgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Location loc = null;
		
		for (String provider : mgr.getProviders(true)) {
			loc = mgr.getLastKnownLocation(provider);
		}
		return loc;
	}
	
	private String getLocationId(Location loc) {
		double lat = loc.getLatitude();
		double lng = loc.getLongitude();
		String input = "" + lat + "," + lng;
		try {
			URL url = new URL("http://deco3801-010.zones.eait.uq.edu.au/resolve.php?str=" + input);
			URLConnection conn = url.openConnection();
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = "";
			while ((line += rd.readLine()) != null) {
				System.out.println(line);
			}
			rd.close();
			return line;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	private String getLocationId(String loc) {
		try {
			URL url = new URL("http://deco3801-010.zones.eait.uq.edu.au/resolve.php?str=" + loc);
			URLConnection conn = url.openConnection();
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = "";
			while ((line += rd.readLine()) != null) {
				System.out.println(line);
			}
			rd.close();
			return line;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
}
