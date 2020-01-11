package com.example.projectx;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class User_Activity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener,
        View.OnClickListener {

    private static final String TAG = User_Activity.class.getSimpleName();
    private GoogleMap mMap;
    private Button DistressSignalButton, attendingButton;
    private String Gender;
    private double longitude;
    private double latitude;
    private GoogleApiClient googleApiClient;
    private LinearLayout linearLayout;
    private ProgressBar loader;
    private DatabaseReference volunteerLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        DistressSignalButton = findViewById(R.id.DistressSignal);
        attendingButton = findViewById(R.id.attendingButton);
        linearLayout = findViewById(R.id.mapLayout);
        loader = findViewById(R.id.loader);


        attendingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Thank you", Toast.LENGTH_SHORT).show();
            }
        });

        DistressSignalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Help is on it's way", Toast.LENGTH_SHORT).show();
            }
        });

        volunteerLocation = FirebaseDatabase.getInstance().getReference("VolunteerLocationDetails");
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LatLng india = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(india).title("Marker in India"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(india));
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapLongClickListener(this);
        CheckMap();
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




    private void getCurrentLocation() {
        mMap.clear();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            moveMap();
            Volunteers volunteers = new Volunteers();
            volunteers.setLatitude(latitude);
            volunteers.setLongitude(longitude);
            volunteerLocation.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(volunteers).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                }
            });


        }
    }

    private void moveMap() {
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title("Marker in India"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18.04f));
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    @Override
    public void onClick(View v) {
        Log.v(TAG, "view click event");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        getCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.addMarker(new MarkerOptions().position(latLng).draggable(true));

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(User_Activity.this, "onMarkerClick", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        latitude = marker.getPosition().latitude;
        longitude = marker.getPosition().longitude;
        moveMap();

    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }
}
