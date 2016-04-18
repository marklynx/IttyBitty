package com.ittybitty.locator.service;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

/**
 * Created by markgarab on 18/04/2016.
 */
public class Place {

    public static final String PARAM_TITLE = "name";
    public static final String PARAM_ADDRESS = "vicinity";
    public static final String PARAM_LOCATION = "location";
    public static final String PARAM_GEOMETRY = "geometry";
    public static final String PARAM_LAT = "lat";
    public static final String PARAM_LNG = "lng";

    private String title;
    private String address;
    private LatLng location;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Place clone(){
        Place dupl = new Place();

        dupl.title = this.title;
        dupl.address = this.address;
        dupl.location = this.location;

        return dupl;
    }

    public static Place parseJSON(JSONObject object){
        if(object != null){
            Place place = new Place();

            place.title = object.optString(PARAM_TITLE);
            place.address = object.optString(PARAM_ADDRESS);

            JSONObject location = object.optJSONObject(PARAM_GEOMETRY);
            if(location != null){
                location = location.optJSONObject(PARAM_LOCATION);
                if(location != null){
                    place.location = new LatLng(location.optDouble(PARAM_LAT), location.optDouble(PARAM_LNG));
                }
            }

            return place;
        }

        return null;
    }
}
