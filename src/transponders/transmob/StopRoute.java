package transponders.transmob;

import java.util.ArrayList;
import java.util.Date;

/**
 * A class that represents a specific route at a specific stop.
 * The class have a list of Date object to represent the 
 * timetable of that specific route at a specific stop.
 * 
 * @author Transponders
 * @version 1.0
 */
public class StopRoute {
	private Stop stop;
	private Route route;
	private ArrayList<Date> times;
	//private HashMap<Date, String> tripIds;
	
	/*public StopRoute(Stop stop, Route route) {
		times = new ArrayList<Date>();
		this.stop = stop;
		this.route = route;
		//this.tripId = tripId;
	}*/

	/**
     * Getter method of the Stop object.
     *
     * @return Stop the specific stop of this object.
     */
	public Stop getStop() {
		return stop;
	}

	/**
     * Getter method of the route object.
     *
     * @return Route the specific route of this object.
     */
	public Route getRoute() {
		return route;
	}

	/**
     * Getter method of the times object.
     *
     * @return ArrayList<Date> the ArrayList containing all the dates.
     */
	public ArrayList<Date> getTimes() {
		return times;
	}

	/**
     * Add a Date object to the ArrayList as the timetable input.
     *
     * @param time The String result from the PHP service request.
     */
	public void addTime(Date time) {
		times.add(time);
	}
	
	/*public String getTripId() {
		return tripId;
	}*/
}
