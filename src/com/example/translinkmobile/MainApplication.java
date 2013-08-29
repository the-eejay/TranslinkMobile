package com.example.translinkmobile;

import java.util.ArrayList;

import android.app.Application;

/**
 * The Android Application that is used to store and pass 
 * Stops data between activities.
 * 
 * @author Transponders
 * @version 1.0
 */
public class MainApplication extends Application{

	// The selected stops that want to have the routes generated.
	private ArrayList<Stop> selectedStops;
	
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
}
