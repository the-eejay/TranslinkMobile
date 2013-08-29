package com.example.translinkmobile;

import java.util.ArrayList;

import com.google.android.gms.maps.model.LatLng;

/**
 * The java class that represents a Stop object. 
 * A stop can either be a bus stop, ferry stop, or train stop.
 * 
 * @author Transponders
 * @version 1.0
 */
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
	
	/**
     * Add a Route object to the stop (representing 1 service code).
     *
     * @param route add one service route to the stop
     */
	public void addRoute(Route route) {
		routes.add(route);
	}
	
	/**
     * Getter method of the route list.
     *
     * @return ArrayList<Route> the ArrayList containing all the routes this stop.
     */
	public ArrayList<Route> getRoutes() {
		return routes;
	}
	
	/**
     * Getter method of the stop id.
     *
     * @return String the String representing the ID of the stop.
     */
	public String getId() {
		return stopId;
	}
	
	/**
     * Getter method of the stop location.
     *
     * @return LatLng the latitude longitude of the position of the stop.
     */
	public LatLng getPosition() {
		return position;
	}
	
	/**
     * Set the position of the parent stop if the stop is grouped together.
     *
     * @param parentId the stop ID of the parent 
     * @param parentPosition the latitude longitude coordinate of the parent stop.
     */
	public void setParentPosition(String parentId, LatLng parentPosition) {
		this.parentId = parentId;
		this.parentPosition = parentPosition;
	}
	
	/**
     * A method to check whether a stop is grouped together (has a parent).
     *
     * @return boolean true if the stop has a parent stop.
     */
	public boolean hasParent() {
		if (parentId.equals("")) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
     * Getter method of the parent stop's position.
     *
     * @return LatLng the latitude longitude of the parent stop's position.
     */
	public LatLng getParentPosition() {
		if (hasParent()) {
			return parentPosition;
		} else {
			return null;
		}
	}
	
	/**
     * Getter method of the parent's stop ID
     *
     * @return String the parent's stop ID
     */
	public String getParentId() {
		if (hasParent()) {
			return parentId;
		} else {
			return null;
		}
	}
	
	/**
     * Getter method description of this stop.
     *
     * @return String the description of this stop.
     */
	public String getDescription() {
		return description;
	}
}
