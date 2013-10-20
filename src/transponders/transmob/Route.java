package transponders.transmob;

import android.util.Log;

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
	private long direction;

	public Route(String code, String name, long type, long direction) {
		this.code = code;
		this.name = name;
		this.type = type;
		this.direction = direction;
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
	
	public long getDirection() {
		return direction;
	}
	
	public String getDirectionAsString() {
		String directionStr = "";
		if (direction == 0) directionStr="North";
		else if (direction == 1) directionStr = "South";
		else if (direction == 2) directionStr = "East";
		else if (direction == 3) directionStr = "West";
		else if (direction == 4) directionStr = "Inbound";
		else if (direction == 5) directionStr = "Outbound";
		else if (direction == 6) directionStr = "Inward";
		else if (direction == 7) directionStr = "Outward";
		else if (direction == 8) directionStr = "Upward";
		else if (direction == 9) directionStr = "Downward";
		else if (direction == 10) directionStr = "Clockwise";
		else if (direction == 11) directionStr = "Counterclockwise";
		else if (direction == 12) directionStr = "Direction1";
		else if (direction == 13) directionStr = "Direction2";
		else directionStr = "Null";
		Log.d("Route", "Direction:" + direction+ " = "+directionStr);
		return directionStr;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(!(obj instanceof Route))
			return false;
		
		Route route2 = (Route) obj;
		
		if(this.getCode().equals(route2.getCode()) && this.getDirection() == route2.getDirection())
			return true;
		else
			return false;
	}
	
	@Override
	public int hashCode()
	{
		int hash = 3;
		hash = 7 * hash + (int) this.getDirection();
		hash = 7 * hash + this.getCode().hashCode();
		
		return hash;
	}
}
