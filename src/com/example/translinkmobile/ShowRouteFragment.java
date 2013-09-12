package com.example.translinkmobile;

import java.util.ArrayList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ShowRouteFragment extends Fragment {
		private static final LatLng DEFAULT_LOCATION = new LatLng(-27.498037,
				153.017823);
		private final String TITLE = "Nearby Stops & Service ETA";

		// Map and markers
		private GoogleMap mMap2;
		private Marker userPos;
		private Marker clickPos;
		private RouteStopsLoader routeStopsLoader;
		private SupportMapFragment mapFrag;

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			/*mapFrag = (SupportMapFragment) (getActivity().getSupportFragmentManager().findFragmentById(R.id.map2));
			
			mMap2 = mapFrag.getMap();
			//mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		
			while (mMap2 == null) {
				// The application is still unable to load the map.
			}*/
			Log.d("Drawer", "End onCreate");
		}
		
		@SuppressLint("NewApi")
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
		        Bundle savedInstanceState) {
			//super.onCreate(savedInstanceState);
			Log.d("Drawer", "Starting onCreateView");
			View view = inflater.inflate(R.layout.route_fragment, container, false);
			
			Log.d("Drawer", "Inflation Completed");
			if (mapFrag == null) {
				mapFrag = (SupportMapFragment) (getActivity().getSupportFragmentManager().findFragmentById(R.id.map2));
			//if (mapFrag.)	
				mMap2 = mapFrag.getMap();
				//mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
			
				while (mMap2 == null) {
					// The application is still unable to load the map.
				}
			}

			LatLng center = DEFAULT_LOCATION;
			
			
			mMap2.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 15));

			//routeStopsLoader = new RouteStopsLoader(mMap2);

			userPos = mMap2.addMarker(new MarkerOptions()
					.position(DEFAULT_LOCATION)
					.title("Your Position")
					.visible(false));
			
			clickPos = mMap2.addMarker(new MarkerOptions()
					.position(DEFAULT_LOCATION)
					.title("Your Selected Position")
					.visible(false)
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

			mMap2.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
				
				@Override
				public void onInfoWindowClick(Marker marker) {
					/*Stop stop = stopLoader.getIdOfMarker(marker);
					if (stop != null) {
						MainApplication app = (MainApplication) getApplicationContext();
						ArrayList<Stop> stops;
						if (stop.hasParent()) {
							stops = stopLoader.getStopsFromParent(stop);
						} else {
							stops = new ArrayList<Stop>();
							stops.add(stop);
						}
						setSelectedStops(stops);

						openTimetableFragment();
					}*/

				}

			});

			mMap2.setOnMapClickListener(new OnMapClickListener() {

				@Override
				public void onMapClick(LatLng arg0) {
					/*clickPos.setVisible(true);
					clickPos.setPosition(arg0);
					locationChanged(arg0);*/
				}

			});
			
			routeStopsLoader.requestRouteStops(((NearbyStops)getActivity()).getSelectedRoute());
			
			
			return view;
		
	}

}
