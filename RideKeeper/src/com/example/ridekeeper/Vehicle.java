package com.example.ridekeeper;

public class Vehicle {
	private String make=null,
		model=null,
		year=null,
		license=null,
		status=null,
		photoURI=null;

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

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
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
	
}
