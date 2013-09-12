package com.example.translinkmobile;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * The Android activity that shows the map with the current location of the
 * device and shows the nearby stops as markers on the map. Currently, this is
 * also the home screen of the application.
 * 
 * @author Transponders
 * @version 1.0
 */
public class NearbyStops extends FragmentActivity {
	
	/**
	 * Set the default location in case the application cannot detect
	 * the current location of the device.
	 */
	private static final LatLng DEFAULT_LOCATION = new LatLng(-27.498037,
			153.017823);
	private final String TITLE = "Nearby Stops & Service ETA";

	// Map and markers
	private GoogleMap mMap;
	private Marker userPos;
	private Marker clickPos;
	private StopDataLoader stopLoader;
	private SupportMapFragment mapFrag;
	private boolean updatedOnce;
	private ArrayList<Stop> selectedStops;
	private Route selectedRoute;
	private LatLng userLatLng;

	// Navigation drawer
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	String[] menuList;
	

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Set the title and the content of the navigation drawer.
		mTitle = TITLE;
		mDrawerTitle = getTitle();
		menuList = getResources().getStringArray(R.array.menu_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_ns);
		mDrawerList = (ListView) findViewById(R.id.left_drawer_ns);

		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,GravityCompat.START);
		
		// set up the drawer's list view with items and click listener
		ArrayAdapter<String> adapter = new MenuAdapter(this, menuList);
		mDrawerList.setAdapter(adapter);	
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		int position = 0;
		mDrawerList.setItemChecked(position, true);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setTitle(TITLE);

		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		
		LatLng center = DEFAULT_LOCATION;
		
		mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		
		mMap = mapFrag.getMap();
		//mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
	
		while (mMap == null) {
			// The application is still unable to load the map.
		}
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15));

		stopLoader = new StopDataLoader(mMap);

		userPos = mMap.addMarker(new MarkerOptions()
				.position(DEFAULT_LOCATION)
				.title("Your Position")
				.visible(false)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_geo_border)));
		
		clickPos = mMap.addMarker(new MarkerOptions()
				.position(DEFAULT_LOCATION)
				.title("Your Selected Position")
				.visible(false)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.chosen_geo_border)));

		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker marker) {
				Stop stop = stopLoader.getIdOfMarker(marker);
				if (stop != null) {
					ArrayList<Stop> stops;
					if (stop.hasParent()) {
						stops = stopLoader.getStopsFromParent(stop);
					} else {
						stops = new ArrayList<Stop>();
						stops.add(stop);
					}
					setSelectedStops(stops);

					openTimetableFragment();
				}

			}

		});

		mMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng arg0) {
				clickPos.setVisible(true);
				clickPos.setPosition(arg0);
				locationChanged(arg0);
			}

		});
		updatedOnce = false;
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// Called when a new location is found by the network location
				// provider.
				userLatLng = new LatLng(location.getLatitude(),
						location.getLongitude());
				userPos.setVisible(true);
				userPos.setPosition(userLatLng);
				if (!updatedOnce) {
					locationChanged(userLatLng);
				}
				updatedOnce = true;
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		// Register the listener with the Location Manager to receive location updates
		// Check every 1 minute and only if location has changed by 50 meters.
		if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			Log.d("Location", "using network");
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 60000, 50,
					locationListener);
		} else if (locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Log.d("Location", "using gps");
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 60000, 50, locationListener);
		} else {
			// No location provider enabled. Use the default location for now
			Log.d("Location", "cannot find user location");
			locationChanged(new LatLng(-27.498037, 153.017823));
		}

	}

	/**
     * A method to move the camera when the user touch the map to set a new
     * location that will have the nearby stops generated around.
     *
     * @param location the latitude and longitude of the new location.
     */
	public void locationChanged(LatLng location) {
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
		stopLoader
				.requestStopsNear(location.latitude, location.longitude, 1000);
	}

	/**
     * A method to set the action bar. The action bar home/up action 
     * should open or close the drawer. ActionBarDrawerToggle will 
     * take care of this.
     *
     * @param item the MenuItem that is selected.
     */
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
     * A listener class that handles what will happen when the item inside
     * the navigation drawer is clicked.
     *
     * @author Transponders
     * @version 1.0
     */
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);

		}
	}

	/**
     * A method that defines what will happen when the user clicks
     * a specific menu item in the navigation drawer. The method
     * will start a new activity according to the selected menu.
     *
     * @param pos the position of the menu item that is clicked.
     */
	private void selectItem(int pos) {
		Fragment fragment = new Fragment();
		FragmentManager manager = getSupportFragmentManager();
		
		switch (pos) {
		case 0:
			manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			mDrawerList.setItemChecked(pos, true);
	        setTitle(TITLE);
	        mDrawerLayout.closeDrawer(mDrawerList);
			return;
		case 1:
			fragment = new JourneyPlanner();
			Bundle args = new Bundle();
			double[] userLoc = {userLatLng.latitude, userLatLng.longitude};
            args.putDoubleArray(JourneyPlanner.ARGS_USER_LOC, userLoc);
            fragment.setArguments(args);
			break;
		case 2:
			fragment = new MaintenanceNewsFragment();
			break;
		default:
			break;
		}
		
		FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
		transaction.addToBackStack(null);
        transaction.commit();

        //update selected item and title, then close the drawer
        mDrawerList.setItemChecked(pos, true);
        setTitle(menuList[pos]);
        mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
		
	@SuppressLint("NewApi")
	@Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }
	
	public void openTimetableFragment() {
		Fragment fragment = new Fragment();
		FragmentManager manager = getSupportFragmentManager();
		fragment = new DisplayRoutesFragment();
		
		FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
		transaction.addToBackStack(null);
        transaction.commit();

        //update selected item and title, then close the drawer
        setTitle("Timetable");       
	}
	
	/**
     * Getter method of the selected stops object.
     *
     * @return ArrayList<Stop> The list of selected stops.
     */
	public ArrayList<Stop> getSelectedStops() {
		return selectedStops;
	}
	
	/**
     * Setter method of the selected stops object.
     *
     * @param stops the ArrayList of selected stops.
     */
	public void setSelectedStops(ArrayList<Stop> stops) {
		this.selectedStops = stops;
	}
	
	public Route getSelectedRoute() {
		return selectedRoute;
	}
	
	public void setSelectedRoute(Route route) {
		selectedRoute = route;
	}
}
