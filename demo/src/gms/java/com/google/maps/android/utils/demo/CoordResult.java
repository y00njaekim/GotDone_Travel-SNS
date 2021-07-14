package com.google.maps.android.utils.demo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CoordResult {

    @SerializedName("coordinates")
    @Expose
    private List<String> coordinates;
    @SerializedName("lat")
    @Expose
    private List<String> lat;
    @SerializedName("lng")
    @Expose
    private List<String> lng;

    public CoordResult(List<String> coordinates, List<String> lat, List<String> lng){
        this.coordinates = coordinates;
        this.lat = lat;
        this.lng = lng;
    }

    public List<String> getCoordinates() { return coordinates; }
    public List<String> getLat() {
        if(lat.size() == 1) return lat;
        lat.remove(0);
        return lat;
    }
    public List<String> getLng() {
        if(lng.size() == 1) return lng;
        lng.remove(0);
        return lng;
    }


}
