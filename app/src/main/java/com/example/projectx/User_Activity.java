package com.example.projectx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


public class User_Activity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private static final String TAG = "volunteerLocation";
    private GoogleMap mMap;
    private Button DistressSignalButton, attendingButton;

    private LinearLayout linearLayout;
    private ProgressBar loader;


    private FusedLocationProviderClient mFusedLocationProviderClient;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final float DEFAULT_ZOOM = 18.04f;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private boolean mLocationPermissionGranted;
    private double Latitude, Longitude;
    private String Gender;

    private Location mLastKnownLocation;


    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private int radius = 1;
    private boolean volunteerFound = false;
    private String volunteerFoundID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            CameraPosition mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        setContentView(R.layout.activity_user_);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        // Construct a PlacesClient
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        PlacesClient mPlacesClient = Places.createClient(this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        DistressSignalButton = findViewById(R.id.DistressSignal);
        attendingButton = findViewById(R.id.attendingButton);
        linearLayout = findViewById(R.id.mapLayout);
        loader = findViewById(R.id.loader);
        linearLayout.setVisibility(View.VISIBLE);


        attendingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Thank you", Toast.LENGTH_SHORT).show();
            }
        });

        DistressSignalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "Help is on it's way", Toast.LENGTH_SHORT).show();
                findVolunteers();

            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menus) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu, menus);
        return true;
    }


    private void findVolunteers() {
        DatabaseReference findVolunteer = FirebaseDatabase.getInstance().getReference("LocationData");
        GeoFire geoFire = new GeoFire(findVolunteer);
        getDeviceLocation();
        GeoQuery findVol = geoFire.queryAtLocation(new GeoLocation(Latitude, Longitude), radius);
        findVol.removeAllListeners();

        findVol.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!volunteerFound && key != FirebaseAuth.getInstance().getCurrentUser().getUid()) {
                    volunteerFound = true;
                    volunteerFoundID = key;
                    Toast.makeText(getApplicationContext(), "Help is on it's way :" + volunteerFoundID, Toast.LENGTH_SHORT).show();
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
                if (!volunteerFound) {
                    radius += 10;
                    findVolunteers();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.signOut) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(User_Activity.this, LandingPageActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);


            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        loader.setVisibility(View.INVISIBLE);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(mLastKnownLocation.getLatitude(),
                                                mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                Latitude = mLastKnownLocation.getLatitude();
                                Longitude = mLastKnownLocation.getLongitude();
                                CheckMap();
                                writeToFirebaseDatabase(Latitude, Longitude);
                                LatLng sydney = new LatLng(Latitude, Longitude);
                                mMap.addMarker(new MarkerOptions().position(sydney)
                                        .title("You"));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            LatLng sydney = new LatLng(-Latitude, Longitude);
                            mMap.addMarker(new MarkerOptions().position(sydney)
                                    .title("You"));

                        }
                    }
                });

            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", Objects.requireNonNull(e.getMessage()));
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            mMap.setMyLocationEnabled(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
            }
        }
        updateLocationUI();
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
            Log.e("Exception: %s", Objects.requireNonNull(e.getMessage()));
        }
    }


    private void CheckMap() {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference userData = database.getReference("Users").child(userId);
        userData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Gender = dataSnapshot.child("gender").getValue(String.class);
                if (Gender.equals("MALE")) {
                    loader.setVisibility(View.INVISIBLE);
                    linearLayout.setVisibility(View.VISIBLE);
                    attendingButton.setVisibility(View.VISIBLE);
                    DistressSignalButton.setVisibility(View.VISIBLE);
                } else if (Gender.equals("FEMALE")) {
                    loader.setVisibility(View.INVISIBLE);
                    linearLayout.setVisibility(View.VISIBLE);
                    attendingButton.setVisibility(View.VISIBLE);
                    DistressSignalButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onLocationChanged(Location location) {
        Latitude = location.getLatitude();
        Longitude = location.getLongitude();
        LatLng latLng = new LatLng(Latitude, Longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        writeToFirebaseDatabase(Latitude, Longitude);

    }

    private void writeToFirebaseDatabase(Double latitude, Double longitude) {
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LocationData");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(userID, new GeoLocation(latitude, longitude));

//        Volunteers volunteers = new Volunteers();
//        volunteers.setLongitude(longitude);
//        volunteers.setLatitude(latitude);
//        DatabaseReference volunteersReference = FirebaseDatabase.getInstance().getReference("Volunteer Location Details");
//        volunteersReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(volunteers);
//
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
