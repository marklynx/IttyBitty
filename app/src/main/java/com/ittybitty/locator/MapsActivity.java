package com.ittybitty.locator;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final int PERMISSION_REQUEST_FINE_LOCATION = 3745;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LatLng melbourne = new LatLng(-37.8243651, 144.9626861);

    private LocationListener locationListener = new LocationListener(){

        @Override
        public void onLocationChanged(Location location) {
            exploreLocation(new LatLng(location.getLatitude(), location.getLongitude()));
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
                                exploreLocation(melbourne);
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
                    exploreLocation(melbourne);
                }
                return;
            }
        }
    }

    private void exploreLocation(LatLng latlng){
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        mMap.addMarker(new MarkerOptions().position(melbourne).title("You are here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
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
