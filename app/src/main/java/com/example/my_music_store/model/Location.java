package com.example.my_music_store.model;

public class Location {
    private double latitude;
    private double longitude;
    private String locale;

    public Location() {

    }

    public Location(double latitude, double longitude, String locale) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.locale = locale;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getLocale() {
        return locale;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}