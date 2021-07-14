package com.google.maps.android.utils.demo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ChatResult {

    @SerializedName("id")
    @Expose
    private List<String> id;
    @SerializedName("message")
    @Expose
    private List<String> message;

    public ChatResult(List<String> id, List<String> message){
        this.id = id;
        this.message = message;
    }

    public List<String> getId() { return id; }
    public List<String> getMessage() { return message; }

}
