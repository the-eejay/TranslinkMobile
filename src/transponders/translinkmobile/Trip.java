package transponders.translinkmobile;

import java.util.ArrayList;

public class Trip {
	private String tripId;
	private Route route;
	//private ArrayList<StopRoute> stopRoutes;
	
	public Trip(String tripId, Route route) {
		this.tripId = tripId;
		this.route = route;
		//this.stopRoutes = new ArrayList<StopRoute>();
	}
	
	public String getTripId() {
		return tripId;
	}
	public void setTripId(String tripId) {
		this.tripId = tripId;
	}
	public Route getRoute() {
		return route;
	}
	public void setRoute(Route route) {
		this.route = route;
	}
	/*public ArrayList<StopRoute> getStopRoutes() {
		return stopRoutes;
	}
	public void addStopRoute(StopRoute stopRoute) {
		stopRoutes.add(stopRoute);
	}*/
	
	
}
