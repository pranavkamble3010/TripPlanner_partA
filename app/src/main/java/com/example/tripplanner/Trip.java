package com.example.tripplanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Trip {

    private String title;
    private String location;
    private List<String> members;
    private String imageUrl;
    private String createdBy;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }



    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Trip(){
        this.members = new ArrayList<String>();

    }

    public Trip(HashMap<String,Object> TripMap) {
        this.title = TripMap.get("title").toString();
        this.location = TripMap.get("location").toString();
        this.createdBy = TripMap.get("createdBy").toString();
        this.imageUrl = TripMap.get("imageUrl").toString();
        this.members = new ArrayList<>();
        this.members = (List<String>) TripMap.get("members");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public void addMember(String member) {
        this.members.add(member);
    }

    @Override
    public String toString() {
        return "Trip{" +
                "title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", members=" + members +
                ", imageUrl='" + imageUrl + '\'' +
                ", createdBy='" + createdBy + '\'' +
                '}';
    }
}
