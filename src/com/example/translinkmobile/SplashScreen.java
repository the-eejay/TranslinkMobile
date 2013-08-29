package com.example.translinkmobile;

import android.os.Bundle;
import android.os.Handler;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

/**
 * The activity that will show the splash screen when the
 * application first loads.
 * 
 * @author Transponders
 * @version 1.0
 */
public class SplashScreen extends Activity {

	// The time that sets how long should the splash screen be displayed.
	private final int SPLASH_TIME_OUT = 3000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		
		// Hide the actionBar for the splash screen.
		ActionBar actionBar = getActionBar();
		actionBar.hide();
		
		new Handler().postDelayed(new Runnable() {
       	 
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(SplashScreen.this, NearbyStops.class);
                startActivity(i);
 
                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash_screen, menu);
		return true;
	}
	

}
