package com.example.projectx;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class User_Activity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ProgressBar loader;
    private LinearLayout linearLayout;
    private Button DistressSignalButton, AttendingButton;
    private String Gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        loader = findViewById(R.id.loader);
        DistressSignalButton = findViewById(R.id.DistressSignal);
        AttendingButton = findViewById(R.id.attendingButton);
        linearLayout = findViewById(R.id.mapLayout);
        loader.setVisibility(View.VISIBLE);

        AttendingButton.setOnClickListener(new View.OnClickListener() {
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


        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Gender = dataSnapshot.child("gender").getValue(String.class);
                    LoadUI();
                    SetUI(Gender);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }



    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void LoadUI() {
        final Handler handler = new Handler();

// Create and start a new Thread
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(400);
                } catch (Exception e) {
                } // Just catch the InterruptedException

                // Now we use the Handler to post back to the main thread
                handler.post(new Runnable() {
                    public void run() {
                        // Set the View's visibility back on the main UI Thread

                    }
                });
            }
        }).start();

    }

    private void SetUI(String aVoid) {
        loader.setVisibility(View.INVISIBLE);
        if (aVoid.equals("MALE")) {
            linearLayout.setVisibility(View.VISIBLE);

        } else if (aVoid.equals("FEMALE")) {
            linearLayout.setVisibility(View.VISIBLE);
            DistressSignalButton.setVisibility(View.VISIBLE);
        }
    }


}
