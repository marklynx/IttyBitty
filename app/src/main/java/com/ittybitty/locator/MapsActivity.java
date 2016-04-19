package com.ittybitty.locator;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ittybitty.locator.service.Place;
import com.ittybitty.locator.service.PlacesRequestor;
import com.ittybitty.locator.service.PlacesResults;
import com.ittybitty.locator.utils.Result;

import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {

    public static final int PERMISSION_REQUEST_FINE_LOCATION = 3745;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LatLng melbourne = new LatLng(-37.8243651, 144.9626861);


    private GoogleApiClient mGoogleApiClient;

    private LocationListener locationListener = new LocationListener(){

        @Override
        public void onLocationChanged(Location location) {
            exploreLocation(new LatLng(location.getLatitude(), location.getLongitude()), true);
            undregisterLoactionService();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mGoogleApiClient = new GoogleApiClient
            .Builder(this)
            .addApi(Places.GEO_DATA_API)
            .addApi(Places.PLACE_DETECTION_API)
            .enableAutoManage(this, this)
            .build();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Rats, the connection has failed
    }


    // I'd rather create a utility class that hides away the ugly logic of the location services,
    // however the API is tightly coupled with THE ACTIVITY, any attempt to abstract will end in an ugly API
    // Google could have done a better job here using context instead of Activity. ://///


    /**
     * API callback. Fires when google maps is ready for use.
     * For Synchronization simplicity request device location here
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;


        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                findViewById(R.id.pleaseWait).setVisibility(View.GONE);
            }
        });

        // center the map to a default location...
        mMap.moveCamera(CameraUpdateFactory.newLatLng(melbourne));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));

        // then poll for the device location
        if(PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            //Request permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                //User has previously denied permission.
                //Explain why right now the permission is necessary or else skys will fall

                new AlertDialog.Builder(this)
                        .setTitle(R.string.need_loc_title)
                        .setMessage(R.string.need_loc_descr)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},  PERMISSION_REQUEST_FINE_LOCATION);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Okay sweetie, you clearly should not have installed this app in the first place. Bye!
                                // Go walk around Southbank
                                exploreLocation(melbourne, true);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},  PERMISSION_REQUEST_FINE_LOCATION);
            }
        } else {
            requestLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLocation();
                } else {
                    // Okay sweetie, you clearly should not have installed this app in the first place. Bye!
                    // Go walk around Southbank
                    exploreLocation(melbourne, true);
                }
                return;
            }
        }
    }

    private void exploreLocation(LatLng latlng, boolean yourLocation){
        if(yourLocation){
            //your location will be true if this method is called from within GPS location detection callbacks
            mMap.addMarker(new MarkerOptions().position(latlng).title("You are here"));
        } else {
            // your location will be false if this method is called as a result of the user zooming or panning the map
            // in this case no device location pin is necessary. Leave empty
            // this scenario is out of scope. So won't be considered at this stage
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));

        this.findPlaces(latlng, null);
    }


    private void findPlaces(final LatLng latlng, final PlacesResults updateResults){
        PlacesRequestor.requestPlaces(this, latlng, updateResults, new PlacesRequestor.PlaceRequestorListener<PlacesResults>() {
            @Override
            public void placesReady(Result<PlacesResults> result) {
                if(result.errors != null && !result.errors.isEmpty()){
                    // Something has gone wrong while querying the google services.
                    // Unfortunately my UX guy was too lazy to provide me with proper UI responses.
                    // So silly Mark Garab decided to just leave this here. Oh maybe a Toast?
                    safeUIToast(result.errors.get(0).getMessage(), Toast.LENGTH_LONG);
                } else {
                    // Hooray! Someone will have a great coffee soon!

                    //The commented code below is the logical implementation of accumulating 100 places, however the google api is capable of considering up to 23 waypoints only.
                    //Effectively our queries and results are limited to 20.
                    /*if(result.value.getPlaces().size() >= 100){
                        for(com.ittybitty.locator.service.Place place : result.value.getPlaces()){
                            mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_cafe)).position(place.getLocation()).title(place.getTitle()).snippet(place.getAddress()));
                        }

                        plotRoute(latlng, result.value.getPlaces());
                    } else {
                        findPlaces(latlng, result.value);
                    }*/

                    //This is the actual limit of the places and waypoints that can be operational in any given time
                    for(com.ittybitty.locator.service.Place place : result.value.getPlaces()){
                        mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_cafe)).position(place.getLocation()).title(place.getTitle()).snippet(place.getAddress()));
                    }

                    plotRoute(latlng, result.value.getPlaces());
                }
            }
        });
    }

    private void plotRoute(LatLng startPoint, List<Place> places){
        PlacesRequestor.requestRoute(this, startPoint, places, new PlacesRequestor.PlaceRequestorListener<List<Place>>() {
            @Override
            public void placesReady(Result<List<Place>> result) {
                if(result.errors != null && !result.errors.isEmpty()){
                    // Something has gone wrong while querying the google services.
                    // Unfortunately my UX guy was too lazy to provide me with proper UI responses.
                    // So silly Mark Garab decided to just leave this here. Oh maybe a Toast?
                    safeUIToast(result.errors.get(0).getMessage(), Toast.LENGTH_LONG);
                } else {
                    // Now you have your postman's route to try all of the coffees you may like.
                    List<LatLng> route = new ArrayList<LatLng>();
                    for(com.ittybitty.locator.service.Place place : result.value){
                        route.add(place.getLocation());
                    }

                    Polyline line = mMap.addPolyline(new PolylineOptions()
                        .addAll(route)
                        .width(12)
                        .color(Color.parseColor("#00BB00"))
                        .geodesic(true)
                    );
                }
            }
        });
    }


    private void safeUIToast(final String message, final int duration){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MapsActivity.this, message, duration).show();
            }
        });
    }

    private void requestLocation(){
        try{
            this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }catch(SecurityException exp){
            Log.e("OUT", "Somehow you've managed to turn off permission within 20 milliseconds of granting it!", exp);
        }
    }

    private void undregisterLoactionService(){
        try{
            locationManager.removeUpdates(locationListener);
        }catch(SecurityException exp){
            Log.e("OUT", "Somehow you've managed to turn off permission within 20 milliseconds of granting it!", exp);
        }
    }
}
