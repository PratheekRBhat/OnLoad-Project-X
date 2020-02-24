package com.example.projectx;

public class Volunteers {

    private String key;
    private double latitude, longitude;

    public Volunteers(double latitude, double longitude) {

        this.latitude = latitude;
        this.longitude = longitude;
    }
    public Volunteers() {
    }

    public Volunteers(String key, double latitude, double longitude) {
        this.key = key;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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
