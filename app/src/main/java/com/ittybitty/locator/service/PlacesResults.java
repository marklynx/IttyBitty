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
    public static final String PARAM_NEXT_PAGE_TOKEN = "next_page_token";

    private List<Place> places;

    private String nextPageToken;

    public PlacesResults(List<Place> places){
        this.places = places;
    }

    public List<Place> getPlaces() {
        return places;
    }

    public void setPlaces(List<Place> places) {
        this.places = places;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void update(PlacesResults newResults, boolean clearOld){
        if(newResults != null){
            if(clearOld){
                this.places = newResults.places;
            } else {
                this.places.addAll(newResults.places);
            }
            this.nextPageToken = newResults.nextPageToken;
        }
    }

    public static PlacesResults parseJSON(JSONObject object){
        if(object != null){
            PlacesResults results = new PlacesResults(new ArrayList<Place>());

            results.nextPageToken = object.optString(PARAM_NEXT_PAGE_TOKEN);

            JSONArray array = object.optJSONArray(PARAM_RESULTS);
            if(array != null){
                for(int i = 0; i < array.length(); ++i){
                    JSONObject pObject = array.optJSONObject(i);
                    if(pObject != null){
                        Place place = Place.parseJSON(pObject);
                        if(place != null){
                            results.places.add(place);
                        }
                    }
                }

                return results;
            }
        }
        return null;
    }
}
