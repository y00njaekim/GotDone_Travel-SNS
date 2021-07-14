package com.google.maps.android.utils.demo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TravelResult {

    @SerializedName("journey_name")
    @Expose
    private List<String> journey_name;
    @SerializedName("date")
    @Expose
    private List<String> date;
    @SerializedName("id")
    @Expose
    private List<String> id;
    @SerializedName("coordinates")
    @Expose
    private List<String> coordinates;


    public TravelResult(List<String> journey_name, List<String> date, List<String> id, List<String> coordinates){
        this.journey_name = journey_name;
        this.date = date;
        this.id = id;
        this.coordinates = coordinates;
    }

    public String getJourneyName() {
        return journey_name.get(0);
    }

    public String getDate() { return date.get(0); }

    public String getCompanions() {
        String ret = id.get(0);
        for(int i=1; i<id.size(); i++){
            ret = ret + ", " + id.get(i);
        }
        return ret;
    }

    public String getId() {
        return id.get(0);
    }

    public String getCoordinates() {
        return coordinates.get(0) + ", " + coordinates.get(1);
    }

}