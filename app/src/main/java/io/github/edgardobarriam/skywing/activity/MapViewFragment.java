package io.github.edgardobarriam.skywing.activity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import io.github.edgardobarriam.skywing.R;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Edgardo Barría Melián on 08-02-2017.
 */

public class MapViewFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerClickListener,
        ResultCallback<Status> {
    MapView mMapView;
    private GoogleMap googleMap;
    private static final String TAG = MapViewFragment.class.getSimpleName();
    private final int REQ_PERMISSION = 999;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private  int updateInterval =5000; // Defined in milliseconds.
    private  int fastestInterval =4000;  // This numbers are extremely low, and should be used only for debug
    private Marker locationMarker;
    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        askPermission();
        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately

        createGoogleApi();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        //Get sharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("SkyWingPref",MODE_PRIVATE);
        updateInterval = (sharedPreferences.getInt("UpdateInterval",10)) *1000;
        Log.d(TAG, "getting UpdateInterval: " + updateInterval);
        fastestInterval = updateInterval - 100;
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    //PERMISSIONS
    /** Checks if the ACCESS_FINE_LOCATION permission is granted **/
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");

        return (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }

    /** Requests the ACCESS_FINE_LOCATION permission **/
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                getActivity(),
                new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                REQ_PERMISSION
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch ( requestCode ) {
            case REQ_PERMISSION: {
                if ( grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    // Permission granted
                    //getLastKnownLocation();
                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }

    /** Location permission denied, close the App **/
    private void permissionsDenied() {
        Log.w(TAG, "permissionsDenied()");
        getActivity().finish();
    }

    //MAP / LOCATION

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged ["+location+"]");
        lastLocation = location;
        //markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG,"onMapClick");
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMapReady(GoogleMap mMap) {
        Log.d(TAG,"onMapReady");
        googleMap = mMap;


        if(!checkPermission()) return;
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(-33.426168, -70.620234), 16);
        googleMap.animateCamera(cameraUpdate);

    }

    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        if ( checkPermission() ) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if ( lastLocation != null ) {
                Log.i(TAG, "LastKnown location. " +
                        "Lon: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                startLocationUpdates();
            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        }
        else askPermission();
    }

    private void startLocationUpdates(){
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(updateInterval)
                .setFastestInterval(fastestInterval);

        if ( checkPermission() )
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    private void markerLocation(LatLng latLng) {
        Log.i(TAG, "markerLocation("+latLng+")");
        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title);
        if ( googleMap!=null ) {
            if ( locationMarker != null )
                locationMarker.remove();
            locationMarker = googleMap.addMarker(markerOptions);
        }
    }

    //GOOGLE API CLIENT
    private void createGoogleApi(){

        if ( googleApiClient == null ) {
            Log.d(TAG, "createGoogleApi()");
            googleApiClient = new GoogleApiClient.Builder( getActivity() )
                    .addConnectionCallbacks( this )
                    .addOnConnectionFailedListener( this )
                    .addApi( LocationServices.API )
                    .build();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"Connecting GoogleApiClient");
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG,"Disconnecting GoogleApiClient");
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getLastKnownLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(@NonNull Status status) {

    }

}
