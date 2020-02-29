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
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
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


public class UserActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {


    private GoogleMap mMap;
    private Location mLastKnownLocation;

    public String Gender, userID;
    private ProgressBar loader;

    private String volunteerKey;
    private String SourceKey;
    private double volunteerLatitude,volunteerLongitude;

    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 16;

    private static final int PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;
    private double Latitude, Longitude;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private double radius = 0.5;
    private boolean volunteerFound = false;
    private int noOfVolunteers = 0;

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

    private void findVolunteers(){
        final DatabaseReference findVolunteer = FirebaseDatabase.getInstance().getReference("LocationData");
        final DatabaseReference notifiedVolunteer = FirebaseDatabase.getInstance().getReference("keyFound");
        final GeoFire geoFire = new GeoFire(findVolunteer);
        getDeviceLocation();
        final GeoQuery findVol =geoFire.queryAtLocation(new GeoLocation(Latitude, Longitude), radius);
        findVol.removeAllListeners();

        findVol.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!volunteerFound && !key.equals(userID)){
                    if (noOfVolunteers <= 2){
                        noOfVolunteers++;

                        addNotifiedVolunteer(key);
                        //sendNotification(key, Latitude, Longitude);
                        findVolunteers();
                    } else {
                        volunteerFound = true;
                    }
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!volunteerFound){
                    radius += 0.1;
                    findVolunteers();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void addNotifiedVolunteer(String key) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("LocationData");
        DatabaseReference notifiedVolunteer = FirebaseDatabase.getInstance().getReference("keyFound");

        final GeoFire findVolunteerGeoFire = new GeoFire(reference);
        final GeoFire notifiedVolunteerGeoFire = new GeoFire(notifiedVolunteer);

        findVolunteerGeoFire.getLocation(key, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null){
                    volunteerKey = key;
                    volunteerLatitude = location.latitude;
                    volunteerLongitude = location.longitude;

                    notifiedVolunteerGeoFire.setLocation(key, new GeoLocation(volunteerLatitude, volunteerLongitude));
                    findVolunteerGeoFire.removeLocation(key);
                } else {
                    System.err.println(String.format("There is no location for key %s in GeoFire", key));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("There was an error getting the GeoFire location: " + databaseError);
            }
        });
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

    @Override
    public void onLocationChanged(Location location) {
        Latitude = location.getLatitude();
        Longitude = location.getLongitude();

        LatLng source = new LatLng(Latitude, Longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(source));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        updateLocationInRealtime(Latitude, Longitude);
    }

    private void updateLocationInRealtime(double latitude, double longitude) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("LocationData");
        GeoFire geoFire = new GeoFire(reference);
        geoFire.setLocation(userID, new GeoLocation(latitude, longitude));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
