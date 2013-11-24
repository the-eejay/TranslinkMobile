package transponders.transmob;

import java.util.Date;
import java.util.List;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

/**
 * A class representing an estimated position between two points.
 * The two points and hence the EstimatedBus are given a time. 
 * Optionally there is a line defining path between points
 * 
 *
 */
public class EstimatedBus {
	
	//LINE_TO_END_POINT_TOLERANCE = Metres tolerance for which to snap a line to the startPosition and endPosition
	private static final double LINE_TO_END_POINT_TOLERANCE = 40;
	
	private LatLng position;
	private LatLng startPosition;
	private LatLng endPosition;
	private long startTime;
	private long endTime;
	private boolean isActive; //isActive should be true only when curent time is between startTime and endTime
	
	/**
	 * 
	 * @param startPosition the position at which the EstimatedBus will be at startTime
	 * @param endPosition the position at which the EstimatedBus will be at endTime
	 * @param startTime the time at which the EstimatedBus will be at startPosition
	 * @param endTime the time at which the EstimatedBus will be at endPosition
	 */
	public EstimatedBus(LatLng startPosition, LatLng endPosition, Date startTime, Date endTime) {
		this.startPosition = startPosition;
		this.startTime = startTime.getTime();
		this.endTime = endTime.getTime();
		this.endPosition = endPosition;
		this.position = startPosition;
		isActive = false;
		
	}
	
	/**
	 * The method to call to move the bus linearly between the give start and end points
	 * @param currentTime the current time to determine current position between points
	 */
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
	
	/**
	 * The method to call to update the EstimatedBus position between start and end points based
	 * on a given Polyline.
	 * @param currentTime the current time to determine position
	 * @param line the Polyline to determine path
	 */
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
	
	/**
	 * Called by updateOnLine() to do the calculations
	 * @param line the Polyline to determine path
	 * @param percentage percentage between two times to find position for
	 * @return true if the EstimatedBus still exists for the given time
	 */
	public boolean snapToClosestPositionOnLine(Polyline line, double percentage) {
		
		boolean foundPosition = false;
		
		List<LatLng> points = line.getPoints();
		
		//find section on line list corresponding to this EstimatedBus
		double tolerance = LINE_TO_END_POINT_TOLERANCE; // metres to check for connecting points
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
		
		//find final position along the line segments 
		distanceTotal = 0;
		lastPos = points.get(startIndex);
		for (int i=startIndex+1; i<=endIndex; i++) {
			float[] results = new float[1];
			Location.distanceBetween(points.get(i).latitude, points.get(i).longitude, lastPos.latitude, lastPos.longitude, results);
			float distanceOfThisSegment = Math.abs(results[0]);
			distanceTotal += distanceOfThisSegment;
			if (distanceTotal >= distanceFromStart) {
				double percentageAlongThisSegment = (distanceFromStart - (distanceTotal - distanceOfThisSegment)) / distanceOfThisSegment;
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
	
	/**
	 * 
	 * @return position of EstimatedBus. Requires update call
	 */
	public LatLng getPosition() {
		return position;
	}
	
	/**
	 * 
	 * @return true if EstimatedBus is still within its timeFrame. Requires update call
	 */
	public boolean isActive() {
		return isActive;
	}
}
