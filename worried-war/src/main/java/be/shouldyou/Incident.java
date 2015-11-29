package be.shouldyou;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import twitter4j.Status;
import be.shouldyou.geocode.json.Location;

import com.google.gson.Gson;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

@Entity
public class Incident {
    @Id
    public Long id;

    @Index
    public Long tweetId;
    @Index
    public Date createdAt;

    
    public String name;
    @Index
    public String screenName;
    
    public String text;
    private List<Location> locations;
    private List<String> addresses;

    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public Incident() {
        // need the no-arg constructor
    }

    public Incident(Status status) {
        this.tweetId = status.getId();
        this.createdAt = status.getCreatedAt();
        this.name = status.getUser().getName();
        this.screenName = status.getUser().getScreenName();
        this.text = status.getText();
    }

    public String getUrl() {
        return "https://twitter.com/" + this.screenName + "/status/" + this.tweetId;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public void addLocation(Location lo) {
        if (this.locations == null) {
            this.locations = new ArrayList<>();
        }
        this.locations.add(lo);
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }
    
    public void addAddress(String address){
        if(this.addresses == null){
            this.addresses = new ArrayList<>();
        }
        this.addresses.add(address);
    }

}
