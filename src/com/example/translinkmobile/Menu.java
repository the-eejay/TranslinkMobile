package com.example.translinkmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Menu extends Activity {
	
	/** The main menu: consists of a number of buttons to take the user
	 * to various functions such as the Journey Planner and Stops Nearby
	 * TODO: Splash screen, add background & logos
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		// Set layout
		setContentView(R.layout.menu);
		
		// Journey Planner button & listener
		Button jpButton = (Button) findViewById(R.id.jpButton);
		jpButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), JourneyPlanner.class));	
			}
		});
		
		// Stops Nearby button & listener
		Button nsButton = (Button) findViewById(R.id.nsButton);
		nsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(), NearbyStops.class));	
			}
		});
	}

}
