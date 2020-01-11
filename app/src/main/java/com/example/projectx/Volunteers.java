package com.example.projectx;

public class Volunteers {

    private double latitude, longitude;

    public Volunteers(double latitude, double longitude) {

        this.latitude = latitude;
        this.longitude = longitude;
    }
    

    public Volunteers() {
    }


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
