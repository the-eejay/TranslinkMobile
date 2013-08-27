package com.example.translinkmobile;

import java.util.ArrayList;

import android.app.Application;

public class MainApplication extends Application{
	/*private Stop selectedStop;
	public Stop getSelectedStop() {
		return selectedStop;
	}
	
	public void setSelectedStop(Stop stop) {
		this.selectedStop = stop;
	}*/
	private ArrayList<Stop> selectedStops;
	
	public ArrayList<Stop> getSelectedStops() {
		return selectedStops;
	}
	
	public void setSelectedStops(ArrayList<Stop> stops) {
		this.selectedStops = stops;
	}
}
