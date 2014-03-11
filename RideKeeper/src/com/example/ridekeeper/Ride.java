package com.example.ridekeeper;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Ride {
    public static final SimpleDateFormat rideDateFormat = new SimpleDateFormat("MM-dd-yyyy");
    public static final SimpleDateFormat prettyRideDateFormat = new SimpleDateFormat("MMMM d, yyyy");
    List<LatLng> points;
    Date startDate;
    Date endDate;
    double distance;

    public void setPoints(List<LatLng> points) {
        this.points = points;
    }
    public List<LatLng> getPoints() {
        return points;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    public Date getStartDate() {
        return startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    public Date getEndDate() {
        return endDate;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
    public double getDistance() {
        return distance;
    }

    public static float calculateDistance(List<LatLng> points) {
        float totalDistance = 0;
        float[] distance = new float[3];
        LatLng lastPoint;
        if (points.size() > 1) {
            lastPoint = points.get(0);
            for (LatLng point : points) {
                Location.distanceBetween(lastPoint.latitude, lastPoint.longitude, point.latitude, point.longitude, distance);
                totalDistance += distance[0];
                lastPoint = point;
            }
        }
        return totalDistance;
    }
}
