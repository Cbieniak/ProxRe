package com.BienProgramming.proxre;
/**Class for each device that was found in the search
 * 
 * @author Christian
 *
 */
public class Device {
	String id;
	String name;
	public  Device(String devId, String devName){
		this.id=devId;
		this.name=devName;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String toString(){
		return name;
	}
}
