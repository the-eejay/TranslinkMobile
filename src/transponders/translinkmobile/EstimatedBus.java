package transponders.translinkmobile;

import java.util.Date;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;


public class EstimatedBus {
	private LatLng position;
	private LatLng startPosition;
	private LatLng endPosition;
	private long startTime;
	private long endTime;
	private boolean isActive;
	
	public EstimatedBus(LatLng startPosition, LatLng endPosition, Date startTime, Date endTime) {
		this.startPosition = startPosition;
		this.startTime = startTime.getTime();
		this.endTime = endTime.getTime();
		this.endPosition = endPosition;
		this.position = startPosition;
		isActive = false;
		
	}
	
	public void update(Date currentTime) {
		long currentTimeLong = currentTime.getTime();
		if (currentTimeLong > startTime && currentTimeLong < endTime) {
			Log.d("Bus", "currTime="+currentTimeLong+" startTime="+startTime+" endTime="+endTime);
			double percentage = ((double)(currentTimeLong - startTime)) / (endTime - startTime);
			
			LatLng directionVector = new LatLng(endPosition.latitude-startPosition.latitude, endPosition.longitude-startPosition.longitude);
			LatLng directionVectorReduced = new LatLng(directionVector.latitude*percentage, directionVector.longitude*percentage);
			Log.d("Bus", "percentage="+percentage+" directionVector="+directionVector+ " directionVectorReduced="+directionVectorReduced);
			position = new LatLng(startPosition.latitude + directionVectorReduced.latitude, startPosition.longitude + directionVectorReduced.longitude);
			isActive = true;
		} else {
			isActive = false;
		}
	}
	
	public void snapToClosestPositionOnLine(Polyline line) {
		//Will do later
	}
	
	public LatLng getPosition() {
		return position;
	}
	
	public boolean isActive() {
		return isActive;
	}
}
