package transponders.translinkmobile;

import java.util.ArrayList;
import java.util.Date;

import android.webkit.WebView;

public class StopTrip implements Comparable<StopTrip> {
	private Stop stop;
	private Trip trip;
	//private ArrayList<Date> times;
	private Date time;
	//private HashMap<Date, String> tripIds;
	
	public StopTrip(Stop stop, Trip trip) {
		//times = new ArrayList<Date>();
		this.stop = stop;
		this.trip = trip;
		//this.tripId = tripId;
	}

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
	public Trip getTrip() {
		return trip;
	}

	/**
     * Getter method of the times object.
     *
     * @return ArrayList<Date> the ArrayList containing all the dates.
     */
	public Date getTime() {
		return time;
	}

	/**
     * Add a Date object to the ArrayList as the timetable input.
     *
     * @param time The String result from the PHP service request.
     */
	public void setTime(Date time) {
		this.time=time;
	}

	@Override
	public int compareTo(StopTrip b) {
		if (b.getTime().getTime() < getTime().getTime()) {
			return 1;
		} else if (b.getTime().getTime() == getTime().getTime()) {
			return 0;
		} else {
			return -1;
		}
		
	}
	
	
	/*public String getTripId() {
		return tripId;
	}*/
}
