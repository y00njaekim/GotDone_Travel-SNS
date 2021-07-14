package com.google.maps.android.utils.demo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MarkerResult {

    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("lat")
    @Expose
    private String lat;
    @SerializedName("lng")
    @Expose
    private String lng;

    public MarkerResult(String address, String lat, String lng){ ;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
    }

    public String getMarkerLng() {
        return lng;
    }

    public String getMarkerLat() {
        return lat;
    }

    public String getAddress() {
        return address;
    }
}
