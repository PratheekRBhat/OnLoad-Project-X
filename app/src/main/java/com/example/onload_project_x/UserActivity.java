package com.example.onload_project_x;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;


import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.TimeUnit;


public class UserActivity extends AppCompatActivity implements OnMapReadyCallback {


    private GoogleMap mMap;
    private Location mLastKnownLocation;

    public String Gender, userID;
    private ProgressBar loader;

    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 16;

    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;
    private double Latitude, Longitude;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        FloatingActionButton settings = findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        loader = findViewById(R.id.loader);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        WorkManager.getInstance(this).cancelAllWork();
        if (mLocationPermissionGranted) {
            startLocationWorker();
        }
    }

    private void startLocationWorker(){
        Data workerData = new Data.Builder().putString("uid", userID).build();
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest
                .Builder(LocationWorker.class, 15, TimeUnit.MINUTES)
                .setInputData(workerData).build();
        WorkManager.getInstance(this).enqueue(workRequest);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        loader.setVisibility(View.VISIBLE);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        setMapStyle(mMap);

        getLocationPermission();

        updateLocationUI();

        getDeviceLocation();
    }

    private void getDeviceLocation(){
        try {
            if (mLocationPermissionGranted){
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()){
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null){
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                Latitude = mLastKnownLocation.getLatitude();
                                Longitude = mLastKnownLocation.getLongitude();
                                CheckMap();
                                writeToFirebaseDatabase(Latitude, Longitude);
                            } else {
                                mMap.moveCamera(CameraUpdateFactory
                                        .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                                LatLng sydney = new LatLng(-Latitude, Longitude);
                                mMap.addMarker(new MarkerOptions().position(sydney)
                                        .title("You"));
                            }
                        }
                    }
                });
            }
        } catch (SecurityException e){
            e.printStackTrace();
        }
    }

    private void getLocationPermission(){
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mLocationPermissionGranted = true;
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        if (requestCode == PERMISSION_REQUEST_ACCESS_FINE_LOCATION){
            if (grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted = true;
//               startLocationWorker();
            }
        }
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void CheckMap(){
        DatabaseReference userData = FirebaseDatabase.getInstance().getReference("User").child(userID);
        userData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Gender = dataSnapshot.child("gender").getValue(String.class);
                if (Gender.equals("MALE")){
                    loader.setVisibility(View.INVISIBLE);
                } else {
                    loader.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void writeToFirebaseDatabase(Double latitude, Double longitude){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("LocationData");
        GeoFire geoFire = new GeoFire(reference);
        geoFire.setLocation(userID, new GeoLocation(latitude, longitude));
    }

//    private void setMapStyle(GoogleMap map) {
//        try {
//            boolean success = map.setMapStyle(
//                    MapStyleOptions.loadRawResourceStyle(
//                            this, R.raw.map_style));
//            if (!success) {
//            }
//        } catch (Resources.NotFoundException e) {
//            e.printStackTrace();
//        }
//    }
    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
    }
}
