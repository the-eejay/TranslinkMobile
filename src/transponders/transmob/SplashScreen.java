package transponders.transmob;

import android.os.Bundle;
import android.os.Handler;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

/**
 * The activity that will show the splash screen when the
 * application first loads.
 * 
 * @author Transponders
 * @version 1.0
 */
public class SplashScreen extends Fragment {

	// The time that sets how long should the splash screen be displayed.
	private final int SPLASH_TIME_OUT = 3000;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View view = inflater.inflate(R.layout.activity_splash_screen, container, false);
		
		
		
		return view;
	}

	
	

}
