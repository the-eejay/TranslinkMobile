package com.example.translinkmobile;


public class Route {
	private String code;
	private String name;
	//private Date time;
	
	
	public Route(String code, String name) {
		this.code = code;
		this.name = name;
		
	}

	public String getCode() {
		return code;
	}
	
	public String getDescription() {
		return name;
	}
	
	/*public void getTime(Date time) {
		this.time = time;
	}
	
	public Date getTime() {
		return time;
	}

	*/
	
	
}
