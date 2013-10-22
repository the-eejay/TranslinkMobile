package transponders.transmob;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.location.Location;
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
	
	public void updateOnLine(Date currentTime, Polyline line) {
		long currentTimeLong = currentTime.getTime();
		if (currentTimeLong > startTime && currentTimeLong < endTime) {
			double percentage = ((double)(currentTimeLong - startTime)) / (endTime - startTime);
			if ( snapToClosestPositionOnLine(line, percentage) ) {
				isActive = true;
			} else {
				isActive = false;
			}
		} else {
			isActive = false;
		}
	}
	
	public boolean snapToClosestPositionOnLine(Polyline line, double percentage) {
		
		boolean foundPosition = false;
		
		List<LatLng> points = line.getPoints();
		//find section on line list corresponding to this EstimatedBus
		double tolerance = 40; // metres to check for connecting points
		int startIndex =0;
		int endIndex=0;
		
		for (int i=0; i<points.size(); i++) {
			Log.d("EstBus", ""+points.get(i)+" startPos="+startPosition+" endPos="+endPosition);
			LatLng point = points.get(i);
			float[] results = new float[1];
			Location.distanceBetween(point.latitude, point.longitude, startPosition.latitude, startPosition.longitude, results);
			Log.d("EstBus", "results[0]="+results[0]);
			if (Math.abs(results[0]) < tolerance) {
				Log.d("EstBus", "START!");
				startIndex = i;
			}
			results = new float[1];
			Location.distanceBetween(point.latitude, point.longitude, endPosition.latitude, endPosition.longitude, results);
			if (Math.abs(results[0]) < tolerance) {
				Log.d("EstBus", "END!");
				endIndex = i;
			}
		}
		
		//get distance along line segments
		LatLng lastPos = points.get(startIndex);
		double distanceTotal = 0;
		for (int i=startIndex+1; i<=endIndex; i++) {
			float[] results = new float[1];
			Location.distanceBetween(points.get(i).latitude, points.get(i).longitude, lastPos.latitude, lastPos.longitude, results);
			distanceTotal += Math.abs(results[0]);
			lastPos = points.get(i);
		}
		double distanceFromStart = distanceTotal*percentage;
		Log.d("EstBus", "distance between start and end points ="+distanceTotal+" distanceFromStart="+distanceFromStart);
		distanceTotal = 0;
		int lineIndex = -1;
		lastPos = points.get(startIndex);
		LatLng output;
		for (int i=startIndex+1; i<=endIndex; i++) {
			float[] results = new float[1];
			Location.distanceBetween(points.get(i).latitude, points.get(i).longitude, lastPos.latitude, lastPos.longitude, results);
			float distanceOfThisSegment = Math.abs(results[0]);
			distanceTotal += distanceOfThisSegment;
			if (distanceTotal >= distanceFromStart) {
				Log.d("EstBus", "Found segment i="+i);
				double percentageAlongThisSegment = (distanceFromStart - (distanceTotal - distanceOfThisSegment)) / distanceOfThisSegment;
				Log.d("EstBus", "percentageAlongThisSegment="+percentageAlongThisSegment);
				Log.d("EstBus", "points["+i+"]="+points.get(i)+" points["+(i-1)+"]="+points.get(i-1));
				LatLng directionVector = new LatLng(points.get(i).latitude-points.get(i-1).latitude, points.get(i).longitude-points.get(i-1).longitude);
				LatLng directionVectorReduced = new LatLng(directionVector.latitude*percentageAlongThisSegment, directionVector.longitude*percentageAlongThisSegment);
				//Log.d("EstBus", "directionvector"
				position = new LatLng(points.get(i-1).latitude + directionVectorReduced.latitude, points.get(i-1).longitude + directionVectorReduced.longitude);
				foundPosition = true;
				break;
			}
			lastPos = points.get(i);
			
		}
		
		return foundPosition;
		
		
		
		
		
	}
	
	public LatLng getPosition() {
		return position;
	}
	
	public boolean isActive() {
		return isActive;
	}
}
