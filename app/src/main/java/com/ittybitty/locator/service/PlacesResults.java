package com.ittybitty.locator.service;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by markgarab on 18/04/2016.
 */
public class PlacesResults {

    public static final String PARAM_RESULTS = "results";

    private List<Place> places;

    public PlacesResults(List<Place> places){
        this.places = places;
    }

    public List<Place> getPlaces() {
        return places;
    }

    public void setPlaces(List<Place> places) {
        this.places = places;
    }

    public static PlacesResults parseJSON(JSONArray array){
        if(array != null){
            PlacesResults results = new PlacesResults(new ArrayList<Place>());

            for(int i = 0; i < array.length(); ++i){
                JSONObject object = array.optJSONObject(i);
                if(object != null){
                    Place place = Place.parseJSON(object);
                    if(place != null){
                        results.places.add(place);
                    }
                }
            }

            return results;
        }

        return null;
    }

}
