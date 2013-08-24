package com.example.translinkmobile;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class Stop {
	private String stopId;
	private int serviceType;
	private LatLng position;
	private String parentId;
	private LatLng parentPosition;
	private String description;
	private ArrayList<Route> routes;
	
	public Stop (String stopId, String decription, String serviceType, LatLng position) {
		this.stopId = stopId;
		this.serviceType = Integer.parseInt(serviceType);
		this.position = position;
		this.parentId = "";
		this.description = decription;
		routes = new ArrayList<Route>();
	}
	public void addRoute(Route route) {
		routes.add(route);
	}
	public ArrayList<Route> getRoutes() {
		return routes;
	}
	public String getId() {
		return stopId;
	}
	
	public LatLng getPosition() {
		return position;
	}
	
	public void setParentPosition(String parentId, LatLng parentPosition) {
		this.parentId = parentId;
		this.parentPosition = parentPosition;
	}
	
	public boolean hasParent() {
		if (parentId.equals("")) {
			return false;
		} else {
			return true;
		}
	}
	
	
	public LatLng getParentPosition() {
		if (hasParent()) {
			return parentPosition;
		} else {
			return null;
		}
	}
	
	public String getParentId() {
		if (hasParent()) {
			return parentId;
		} else {
			return null;
		}
	}
	
	public String getDescription() {
		return description;
	}
	
	/*
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
	public static final Parcelable.Creator<Stop> CREATOR
		    = new Parcelable.Creator<Stop>() {
		public Stop createFromParcel(Parcel in) {
		    return new Stop(in);
		}
		
		public Stop[] newArray(int size) {
		    return new Stop[size];
		}
	};
	
	private Stop(Parcel in) {
	         mData = in.readInt();
	}*/

}
