package com.example.translinkmobile;

import java.util.ArrayList;
import java.util.Date;

public class StopRoute {
	private Stop stop;
	private Route route;
	private ArrayList<Date> times;
	
	public StopRoute(Stop stop, Route route) {
		times = new ArrayList<Date>();
		this.stop = stop;
		this.route = route;
	}

	public Stop getStop() {
		return stop;
	}

	public Route getRoute() {
		return route;
	}

	public ArrayList<Date> getTimes() {
		return times;
	}

	public void addTime(Date time) {
		times.add(time);
	}
}
