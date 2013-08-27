package com.example.translinkmobile;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.support.SlideHolder;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class NearbyStops extends Activity{
private static final LatLng DEFAULT_LOCATION = new LatLng(-27.498037,153.017823);
	
	private GoogleMap mMap;
	private Marker userPos;
	private Marker clickPos;
	private StopDataLoader stopLoader;
	private boolean updatedOnce;

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
		mTitle = "Nearby Stops & Service ETA";
		mDrawerTitle = "Translink Mobile";
		String[] temp = {"Nearby Stops", "Journey Planner"};
		menuList = temp;
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_ns);
        mDrawerList = (ListView) findViewById(R.id.left_drawer_ns);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, menuList));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
        int position = 0;
        mDrawerList.setItemChecked(position, true);
	    setTitle(mTitle);
	    
	    getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
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
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		/*try {
			MapsInitializer.initialize(this);
		} catch (GooglePlayServicesNotAvailableException e) {
			// TODO Auto-generated catch block
			Log.d("Location", e.toString());
		}*/
		while (mMap == null) {
			; //this is not good and will need to change it later
		}
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15));
		
		stopLoader = new StopDataLoader(mMap);
		//stopLoader.requestStopsNear(-27.498037,153.017823, 1000);
		
		/*ArrayList<Stop> stops = stopLoader.getStopsNear();
		for (Stop stop : stops) {
			mMap.addMarker(new MarkerOptions()
			.position(stop.getPosition())
			.title(stop.getId()));
			
		}
		*/
		userPos = mMap.addMarker(new MarkerOptions()
			.position(DEFAULT_LOCATION)
			.title("Your Position")
			.visible(false)
			.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
		clickPos = mMap.addMarker(new MarkerOptions()
        	.position(DEFAULT_LOCATION)
        	.title("Your Selected Position")
        	.visible(false)
        	.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
		
		
		mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			
			@Override
			public void onInfoWindowClick(Marker marker) {
				Stop stop = stopLoader.getIdOfMarker(marker);
				if (stop != null) {
					MainApplication app = (MainApplication)getApplicationContext();
					ArrayList<Stop> stops;
					if (stop.hasParent()) {
						stops = stopLoader.getStopsFromParent(stop);
					} else {
						stops = new ArrayList<Stop>();
						stops.add(stop);
					}
					app.setSelectedStops(stops);
					
					Intent intent = new Intent(getApplicationContext(), DisplayRoutesActivity.class);
					startActivity(intent);
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
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		      // Called when a new location is found by the network location provider.
		    	LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
		    	userPos.setVisible(true);
		    	userPos.setPosition(userLatLng);
		    	if (!updatedOnce) {
		    		locationChanged(userLatLng);
		    	}
		      updatedOnce = true;
		    }

		    public void onStatusChanged(String provider, int status, Bundle extras) {}

		    public void onProviderEnabled(String provider) {}

		    public void onProviderDisabled(String provider) {}
		  };

		  // Register the listener with the Location Manager to receive location updates
		  // check every 1minutes and only if location has changed by 50 metres
			  
		  if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			  Log.d("Location", "using network");
			  locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 50, locationListener);
		  } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			  Log.d("Location", "using gps");
			  locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 50, locationListener);
		  } else {
			  //No location provider enabled. Use the default location for now
			  Log.d("Location", "cannot find user location");
			  locationChanged(new LatLng(-27.498037,153.017823));
		  }
		
	}	
	
	public void locationChanged(LatLng location) {
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
		stopLoader.requestStopsNear(location.latitude, location.longitude, 1000);
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
       if (mDrawerToggle.onOptionsItemSelected(item)) {
           return true;
       }
       return super.onOptionsItemSelected(item);
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
	    @Override
	    public void onItemClick(AdapterView parent, View view, int position, long id) {
	    	Log.d("onItemClick() from NS", "" + position);
	        selectItem(position);
	        
	    }    
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int pos) {
	    // Create a new fragment and specify the planet to show based on position
		Log.d("selectItem() from NS","" + pos);
	    
	    switch(pos)
	    {
	    	case 0:
	    		break;
	    	case 1:
	    		startActivity(new Intent(getApplicationContext(), JourneyPlanner.class));
	    		break;
	    	default:
	    		break;
	    }

	    // Highlight the selected item, update the title, and close the drawer
	    //mDrawerList.setItemChecked(pos, true);
	    //setTitle(menuList[pos]);
	   // mDrawerLayout.closeDrawer(mDrawerList);
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

}
