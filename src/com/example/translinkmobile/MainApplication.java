package com.example.translinkmobile;

import android.app.Application;

public class MainApplication extends Application{
	private Stop selectedStop;
	public Stop getSelectedStop() {
		return selectedStop;
	}
	
	public void setSelectedStop(Stop stop) {
		this.selectedStop = stop;
	}
}
