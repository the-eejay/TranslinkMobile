package com.example.translinkmobile;

/**
 * The java class that represents a service route object. 
 * A Route can either be a bus service, ferry service, or a train service.
 * 
 * @author Transponders
 * @version 1.0
 */
public class Route {
	static class TransportType {
		public static final int BUS = 2;
		public static final int FERRY = 4;
		public static final int TRAIN = 8;
	};
	
	// A service route will have a code and a name.
	private String code;
	private String name;
	private long type;

	public Route(String code, String name, long type) {
		this.code = code;
		this.name = name;
		this.type = type;
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
	
	public long getType() {
		return type;
	}
}
