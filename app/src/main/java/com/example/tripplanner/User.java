package com.example.tripplanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class User {

    private String fname;
    private String lname;
    private String username;
    private String imageUrl;
    private String gender;
    private List<String> tripsAddedTo;

    public User() {
        this.tripsAddedTo = new ArrayList<>();
    }

    public User(String fname, String lname, String username, String imageUrl, String gender) {
        this.fname = fname;
        this.lname = lname;
        this.username = username;
        this.imageUrl = imageUrl;
        this.gender = gender;
        this.tripsAddedTo = new ArrayList<>();
    }


    public User(HashMap<String,Object> userMap) {
        this.fname = userMap.get("fname").toString();
        this.lname = userMap.get("lname").toString();
        this.username = userMap.get("username").toString();
        this.imageUrl = userMap.get("imageUrl").toString();
        this.gender = userMap.get("gender").toString();

        this.tripsAddedTo = new ArrayList<>();
        this.tripsAddedTo = (List<String>) userMap.get("tripsAddedTo");
    }

    public List<String> getTripsAddedTo() {
        return tripsAddedTo;
    }

   /* public void setTripsAddedTo(List<String> tripsAddedTo) {
        this.tripsAddedTo = tripsAddedTo;
    }*/

    public User addTrip(String tripName) {
        if(this.tripsAddedTo == null)
            tripsAddedTo = new ArrayList<>();
        this.tripsAddedTo.add(tripName);
        return this;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "User{" +
                "fname='" + fname + '\'' +
                ", lname='" + lname + '\'' +
                ", username='" + username + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }
}
