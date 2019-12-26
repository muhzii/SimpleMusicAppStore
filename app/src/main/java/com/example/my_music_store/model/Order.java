package com.example.my_music_store.model;

import java.sql.Timestamp;
import java.util.ArrayList;

public class Order {
    private User user;
    private String ts;
    private ArrayList<String> songNames;
    private Location location;

    public Order() {

    }

    public Order(User user, Timestamp ts, ArrayList<String> songNames, Location location) {
        this.songNames = songNames;
        this.user = user;
        this.ts = ts.toString();
        this.location = location;
    }

    public User getUser() {
        return user;
    }

    public String getTs() {
        return ts.toString();
    }

    public ArrayList<String> getSongNames() {
        return songNames;
    }

    public Location getLocation() {
        return location;
    }

    public void setSongNames(ArrayList<String> songNames) {
        this.songNames = songNames;
    }

    public void setTs(String ts) {
        this.ts = ts;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}