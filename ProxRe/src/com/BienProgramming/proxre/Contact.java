package com.BienProgramming.proxre;
/**
 * A class for each device that the user wants to remember
 * @author Christian
 *
 */
public class Contact {
	int id;
	String device_id;
	String phone_name;
	String name;
	public Contact(){
		
	}
	/**
	 * Simple constructor
	 * @param devId-Device BT id(Unique)
	 * @param devName-Bluetooth name(Owner defined)
	 * @param givenName-Name the user gives to the device
	 */
	public Contact(String devId, String devName, String givenName){
	
		this.device_id=devId;
		this.phone_name=devName;
		this.name=givenName;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDevice_id() {
		return device_id;
	}
	public void setDevice_id(String device_id) {
		this.device_id = device_id;
	}
	public String getPhone_name() {
		return phone_name;
	}
	public void setPhone_name(String phone_name) {
		this.phone_name = phone_name;
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
