package com.example.ridekeeper;

public class Vehicle {
	private String make="",
		model="",
		license="",
		status="",
		UID="",
		photoURI="";
	
	int year = 0;
	private double lat=0, lng=0;

	public String getPhotoURI(){
		return photoURI;
	}
	
	public void setPhotoURI(String uri){
		this.photoURI = uri;
	}
	
	public String getMake() {
		return make;
	}

	public void setMake(String make) {
		this.make = make;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public void setUID(String uid){
		this.UID = uid;
	}
	
	public String getUID(){
		return this.UID;
	}
	
	public void setLat(double latitute){
		lat = latitute;
	}
	
	public double getLat(){
		return lat;
	}
	
	public void setLng(double longitude){
		lat = longitude;
	}
	
	public double getLng(){
		return lng;
	}
}
