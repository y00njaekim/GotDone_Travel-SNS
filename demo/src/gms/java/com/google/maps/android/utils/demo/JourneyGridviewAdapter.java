package com.google.maps.android.utils.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class JourneyGridviewAdapter extends BaseAdapter {
    private Context context;
    private List<GetTravelResult> travelresults;

    public JourneyGridviewAdapter(List<GetTravelResult> travelresults, Context context){
        this.travelresults = travelresults;
        this.context = context;
    }

    public String getItemJourneyName(int position) {
        return travelresults.get(position).getJourneyName();
    }

    public String getItemDate(int position) {
        return travelresults.get(position).getDate();
    }

    public String getItemCompanionIds(int position) {
        return travelresults.get(position).getCompanions();
    }

    public String getItemUserId(int position) {
        return travelresults.get(position).getId();
    }

    public String getItemCoordinates(int position){
        return travelresults.get(position).getCoordinates();
    }

    public String getItemShared(int position){
        return travelresults.get(position).getShared();
    }

    public String getItemLike(int position){
        return travelresults.get(position).getLike();
    }

    public String getItemLat(int position){
        return travelresults.get(position).getLat();
    }

    public String getItemLng(int position){
        return travelresults.get(position).getLng();
    }

    public String getItemLikeIds(int position){
        return travelresults.get(position).getLikeIds();
    }

    public int getCount() {
        return travelresults.size();
    }

    public Object getItem(int position) {
        return travelresults.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.griditem_journey, viewGroup, false);

        TextView gv_jny_name = view.findViewById(R.id.gv_jny_name);
        TextView gv_date = view.findViewById(R.id.gv_date);
        TextView gv_cpns = view.findViewById(R.id.gv_cpns);
        TextView like_num = view.findViewById(R.id.like_num);

        ImageView like = view.findViewById(R.id.img_like);
        ImageView share = view.findViewById(R.id.img_share);

        final GetTravelResult thisTravelResult = travelresults.get(i);

        String s_jny_name = thisTravelResult.getJourneyName();
        String s_date = thisTravelResult.getDate();
        String s_cpns = thisTravelResult.getCompanions();
        String id = thisTravelResult.getId();
        String shared = thisTravelResult.getShared();
        String liked = thisTravelResult.getLike();

        gv_jny_name.setText(s_jny_name);
        gv_date.setText(s_date);
        gv_cpns.setText(s_cpns);

        if(shared.equals("true")) { share.setVisibility(view.VISIBLE); }
        else { share.setVisibility(view.INVISIBLE); }

        if(!liked.equals("0")) {
            like.setVisibility(view.VISIBLE);
            like_num.setText(liked);
        }
        else { like.setVisibility(view.INVISIBLE); }

//        if(shared.equals("true")) { gv_jny_name.setTextColor(Color.rgb(0,0,200)); }
//        else {gv_jny_name.setTextColor(Color.rgb(200,200,200)); }

        return view;

    }
}
