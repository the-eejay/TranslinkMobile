package transponders.transmob;

public class Trip implements Comparable<Trip> {
	private String tripId;
	private Route route;
	private Long departureTime;
	//private ArrayList<StopRoute> stopRoutes;
	
	public Trip() {}
	
	public Trip(String tripId, Route route) {
		this.tripId = tripId;
		this.route = route;
		//this.stopRoutes = new ArrayList<StopRoute>();
	}
	
	public Trip(String tripId, Route route, Long departureTime) {
		this.tripId = tripId;
		this.route = route;
		this.departureTime = departureTime;
		//this.stopRoutes = new ArrayList<StopRoute>();
	}
	
	public Long getDepartureTime() {
		return departureTime;
	}
	
	public void setDepartureTime(Long departureTime) {
		this.departureTime = departureTime;
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

	@Override
	public int compareTo(Trip trip2) 
	{	
		if(this.getDepartureTime() < trip2.getDepartureTime())
			return -1;
		else if(this.getDepartureTime() > trip2.getDepartureTime())
			return 1;
		else
			return 0;
	}
}
