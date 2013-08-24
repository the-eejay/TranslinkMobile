package com.example.translinkmobile;

import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;

/**
 * This class probably won't be used!
 * 
 *
 */
public class StopParent {
	private String stopId;
	private int serviceType;
	private LatLng position;
	private ArrayList<Stop> stops;
	
	public StopParent (String stopId, String serviceType, LatLng position, ArrayList<Stop> stops) {
		this.stopId = stopId;
		this.serviceType = Integer.parseInt(serviceType);
		this.position = position;
		this.stops = stops;
	}
	
	public String getId() {
		return stopId;
	}
	
	public void addStop(Stop stop) {
		stops.add(stop);
	}
	
	public LatLng getPosition() {
		return position;
	}
}
