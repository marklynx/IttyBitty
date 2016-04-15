package com.ittybitty.locator;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * API callback. Fires when google maps is ready for use.
     * For Synchronization simplicity request device location here
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // center the map to a default location...
        LatLng melbourne = new LatLng(-37.8243651, 144.9626861);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(melbourne));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        //mMap.addMarker(new MarkerOptions().position(melbourne).title("Southbank"));

        // then poll for the device location
    }
}
