package com.example.translinkmobile;

/**
 * The java class that represents a service route object. 
 * A Route can either be a bus service, ferry service, or a train service.
 * 
 * @author Transponders
 * @version 1.0
 */
public class Route {
	
	// A service route will have a code and a name.
	private String code;
	private String name;

	public Route(String code, String name) {
		this.code = code;
		this.name = name;		
	}

	/**
     * Getter method of the route code.
     *
     * @return String the String representing the code of the route.
     */
	public String getCode() {
		return code;
	}
	
	/**
     * Getter method of the route description (route name).
     *
     * @return String the String representing the name of the route.
     */
	public String getDescription() {
		return name;
	}
}
