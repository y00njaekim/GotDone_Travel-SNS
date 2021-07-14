package com.google.maps.android.utils.demo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetTravelResult{

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
    @SerializedName("shared")
    @Expose
    private List<String> shared;
    @SerializedName("like")
    @Expose
    private List<String> like;
    @SerializedName("lat")
    @Expose
    private List<String> lat;
    @SerializedName("lng")
    @Expose
    private List<String> lng;
    @SerializedName("likeids")
    @Expose
    private List<String> likeids;

    public GetTravelResult(List<String> journey_name, List<String> date, List<String> id, List<String> coordinates,  List<String> shared, List<String> like, List<String> likeids, List<String> lat, List<String> lng){ ;
        this.journey_name = journey_name;
        this.date = date;
        this.id = id;
        this.coordinates = coordinates;
        this.shared = shared;
        this.like = like;
        this.likeids = likeids;
        this.lat = lat;
        this.lng = lng;
    }

    public String getJourneyName() {
        return journey_name.get(0);
    }

    public String getDate() { return date.get(0); }

    public String getCompanions() {
        //if(id.size() == 0) return null;
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

    public String getShared() { return shared.get(0); }

    public String getLike() { return like.get(0); }

    public String getLat() {
        //if(lat.size() == 0) return null;
        String ret = lat.get(0);
        for(int i=1; i<lat.size(); i++){
            ret = ret + ", " + lat.get(i);
        }
        return ret;
    }

    public String getLng() {
        //if(lng.size() == 0) return null;
        String ret = lng.get(0);
        for(int i=1; i<lng.size(); i++){
            ret = ret + ", " + lng.get(i);
        }
        return ret;
    }

    public String getLikeIds() {
        //if(likeids.size() == 0) return null;
        String ret = likeids.get(0);
        for(int i=1; i<likeids.size(); i++){
            ret = ret + ", " + likeids.get(i);
        }
        return ret;
    }

}