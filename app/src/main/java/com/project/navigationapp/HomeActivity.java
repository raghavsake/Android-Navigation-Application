package com.project.navigationapp;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mmi.services.api.directions.DirectionsCriteria;
import com.project.navigationapp.extras.DirectionPolylinePlugin;
import com.project.navigationapp.extras.Storage;

public class HomeActivity extends MainActivity implements LocationEngineListener {

    private DirectionPolylinePlugin directionPolylinePlugin;
    MapboxMap mapboxMap;
    private LocationEngine locationEngine;
    private String profile = DirectionsCriteria.PROFILE_DRIVING;
    private String resource = DirectionsCriteria.RESOURCE_ROUTE;

    @Override
    public void onAppMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        LocationComponentOptions options = LocationComponentOptions.builder(this)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(this, R.color.purple_500))
                .build();
        LocationComponent locationComponent = mapboxMap.getLocationComponent();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationComponent.activateLocationComponent(this, options);
        locationComponent.setLocationComponentEnabled(true);
        locationEngine = locationComponent.getLocationEngine();

        locationEngine.addLocationEngineListener(this);
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.setRenderMode(RenderMode.COMPASS);

    }

    @Override
    public void onAutofillRowSelected(final double latitude, final double longitude) {
        Storage.end_lat=latitude;
        Storage.end_long=longitude;
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Storage.start_lat=location.getLatitude();
                            Storage.start_long=location.getLongitude();
                        }
                    }
                });
        //getDirections(start[0].getLongitude(), start[0].getLatitude(), end.getLongitude(), end.getLatitude());
        Intent i=new Intent(HomeActivity.this,DirectionActivity.class);
        startActivity(i);

    }

    @Override
    public void onConnected() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locationEngine != null) {
            locationEngine.removeLocationEngineListener(this);
            locationEngine.addLocationEngineListener(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationEngine != null)
            locationEngine.removeLocationEngineListener(this);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (locationEngine != null) {
            locationEngine.removeLocationEngineListener(this);
            locationEngine.removeLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationEngine != null) {
            locationEngine.deactivate();
        }
    }

    @Override
    public void onTouched(int position) {

    }
}





