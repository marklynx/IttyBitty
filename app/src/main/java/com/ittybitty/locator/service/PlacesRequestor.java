package com.ittybitty.locator.service;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.ittybitty.locator.R;
import com.ittybitty.locator.utils.Result;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by markgarab on 18/04/2016.
 */
public class PlacesRequestor {

    private static List<PlacesQuery> queryQueue;
    private static String placesAPIUrl;
    private static String routeAPIUrl;

    public interface PlaceRequestorListener<T>{
        void placesReady(Result<T> result);
    }

    private static final class PlacesQuery{
        private final LatLng center;
        private final PlaceRequestorListener listener;

        public PlacesQuery(LatLng centerPoint, PlaceRequestorListener listener){
            this.center = centerPoint;
            this.listener = listener;
        }
    }

    public static void requestRoute(Context context, final LatLng startPoint, final List<Place> via, final PlaceRequestorListener listener){
        if(routeAPIUrl == null){
            routeAPIUrl = context.getString(R.string.directions_api_url).replace("&amp;", "&").replace("{2}", context.getString(R.string.google_places_web_key));
        }

        new AsyncTask<Void, Void, Result<List<Place>>>(){

            @Override
            public Result<List<Place>> doInBackground(Void... params){
                try{
                    String url = routeAPIUrl.replace("{1}", startPoint.latitude + "," + startPoint.longitude);
                    StringBuffer sb = new StringBuffer();

                    for(Place place : via){
                        sb.append("%7C").append(place.getLocation().latitude + "," + place.getLocation().longitude);
                    }

                    url = url.replace("{3}", sb.toString());

                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                    int responseCode = connection.getResponseCode();
                    if(responseCode > 199 && responseCode < 300){
                        JSONObject response = readFromConnection(connection);

                        JSONArray order = response.optJSONArray("routes").getJSONObject(0).optJSONArray("waypoint_order");

                        Place yourLocation = new Place();
                        yourLocation.setAddress("");
                        yourLocation.setTitle("You are here");
                        yourLocation.setLocation(startPoint);

                        List<Place> route = new ArrayList<Place>();
                        route.add(yourLocation);
                        for(int i = 0; i < order.length(); ++i){
                            int index = order.optInt(i);
                            route.add(via.get(index));
                        }
                        route.add(yourLocation);

                        return new Result<>(route);
                    } else {
                        return new Result<>(new Exception("The Route query was not successful. Response code " + responseCode));
                    }
                }catch(Throwable th){
                    return new Result<>(th);
                }
            }

            @Override
            public void onPostExecute(Result<List<Place>> result){
                if(listener != null){
                    listener.placesReady(result);
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
    }

    public static void requestPlaces(Context context, LatLng centerPoint, final PlaceRequestorListener listener){
        if(placesAPIUrl == null){
            placesAPIUrl = context.getString(R.string.places_api_url).replace("&amp;", "&").replace("{2}", context.getString(R.string.google_places_web_key));
        }

        if(queryQueue == null){
            queryQueue = new ArrayList<PlacesQuery>();
        }

        synchronized (queryQueue){
            queryQueue.add(new PlacesQuery(centerPoint, listener));
        }

        nextQuery();
    }

    private static void nextQuery(){
        PlacesQuery query = null;

        synchronized (queryQueue){
            if(!queryQueue.isEmpty()){
                query = queryQueue.remove(0);
            }
        }

        if(query != null){
            performQuery(query);
        }
    }

    private static void performQuery(final PlacesQuery query){
        new AsyncTask<PlacesQuery, Void, Result<PlacesResults>>(){

            @Override
            public Result<PlacesResults> doInBackground(PlacesQuery... query){
                try{
                    String url = placesAPIUrl.replace("{1}", query[0].center.latitude + "," + query[0].center.longitude);

                    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

                    int responseCode = connection.getResponseCode();
                    if(responseCode > 199 && responseCode < 300){
                        JSONObject response = readFromConnection(connection);

                        PlacesResults results = PlacesResults.parseJSON(response.optJSONArray(PlacesResults.PARAM_RESULTS));

                        return new Result<>(results);

                    } else {
                        return new Result<>(new Exception("The Places query was not successful. Response code " + responseCode));
                    }
                }catch(Throwable th){
                    return new Result<>(th);
                }
            }

            @Override
            public void onPostExecute(Result<PlacesResults> result){
                if(query.listener != null){
                    query.listener.placesReady(result);
                }
            }

        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
    }

    private static JSONObject readFromConnection(HttpURLConnection connection) throws Exception{
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        StringBuffer sb = new StringBuffer();

        String line = null;
        while((line = br.readLine()) != null){
            sb.append(line);
        }

        br.close();
        connection.disconnect();

        JSONTokener tokener = new JSONTokener(sb.toString());
        JSONObject response = (JSONObject) tokener.nextValue();

        return response;
    }

}
