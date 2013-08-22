package com.example.translinkmobile;

import com.google.android.gms.maps.model.LatLng;

public class Stop {
	private String stopId;
	private int serviceType;
	private LatLng position;
	
	public Stop (String stopId, String serviceType, LatLng position) {
		this.stopId = stopId;
		this.serviceType = Integer.parseInt(serviceType);
		this.position = position;
	}
	
	public String getId() {
		return stopId;
	}
	
	public LatLng getPosition() {
		return position;
	}
}
