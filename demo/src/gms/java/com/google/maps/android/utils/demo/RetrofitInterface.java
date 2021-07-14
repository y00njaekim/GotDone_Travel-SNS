package com.google.maps.android.utils.demo;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface RetrofitInterface {

    @POST("/login")
    Call<LoginResult> executeLogin(@Body HashMap<String, String> map);

    @POST("/signup")
    Call<Void> executeSignup (@Body HashMap<String, String> map);

    @POST("/checkuser")
    Call<Void> checkUser (@Body HashMap<String, String> map);

    @POST("/travel")
    Call<Void> addTravel (@Body HashMap<String, List<String>> map);

    @POST("/gettravel")
    Call<List<GetTravelResult>> getTravel (@Body HashMap<String, String> map);

    @POST("/removetravel")
    Call<Void> removeTravel (@Body HashMap<String, String> map);

    @POST("/updatetravel")
    Call<Void> updateTravel (@Body HashMap<String, List<String>> map);

    @POST("/getsharedtravel")
    Call<List<GetTravelResult>> getSharedTravel (@Body HashMap<String, String> map);

    @POST("/searchsharedtravel")
    Call<Void> searchSharedTravel (@Body HashMap<String, String> map);

    @POST("/showchat")
    Call<ChatResult> showChat (@Body HashMap<String, String> map);

    @POST("/updatecoordinates")
    Call<Void> updateCoordinates(@Body HashMap<String, List<String>> map);

    @POST("/getcoordinates")
    Call<CoordResult> getCoordinates(@Body HashMap<String, List<String>> map);


}