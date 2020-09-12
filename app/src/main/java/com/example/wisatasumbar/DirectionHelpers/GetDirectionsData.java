package com.example.wisatasumbar.DirectionHelpers;

import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

public class GetDirectionsData extends AsyncTask<Object,String,String> {

    GoogleMap mMap;
    String url;
    String googleDirectionData;
    public String distance,duration;
    LatLng latLng;

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap)objects[0];
        url = (String)objects[1];
        latLng =(LatLng)objects[2];

        DownloadUrl downloadUrl = new DownloadUrl();
        try{
            googleDirectionData = downloadUrl.readUrl(url);
        }catch (Exception e){
            e.printStackTrace();
        }
        return googleDirectionData;
    }
    public void onPostExecute(String s){
        HashMap<String,String> directionlist = null;
        DataParser parser = new DataParser();
        directionlist = parser.parseDirections(s);

        distance = directionlist.get("distance");
        duration = directionlist.get("duration");
        setDistance(distance);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Waktu : "+duration);
        markerOptions.snippet("Jarak : " +distance);
        markerOptions.icon((BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.addMarker(markerOptions);
    }

}
