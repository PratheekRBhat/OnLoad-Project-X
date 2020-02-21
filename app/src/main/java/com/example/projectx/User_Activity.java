package com.example.projectx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;


import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;

import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.projectx.DirectionHelpers.FetchURL;
import com.example.projectx.DirectionHelpers.TaskLoadedCallback;

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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class User_Activity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, TaskLoadedCallback {

    private static final String TAG = "volunteerLocation";
    private GoogleMap mMap;
    private ProgressBar loader;
    private Button distressSignalButton;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final float DEFAULT_ZOOM = 16;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private boolean mLocationPermissionGranted;
    private double Latitude, Longitude;
    public String Gender, userID,Name,Phone_number ;
    private Location mLastKnownLocation;
    private  String VName ,Vphone;

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private double radius = 0.5;
    private boolean volunteerFound = false;

    private String[] volunteerFoundID = new String[3];
    private String destinationLatitude, destinationLongitude;
    private RequestQueue requestQueue;
    private String URL = "https://fcm.googleapis.com/fcm/send";
    private boolean helping = false;
    private Polyline currentPolyline;

    public int noOfVolunteers = 0;
    private RelativeLayout callLayout;
    private TextView vname,vphone;
    private ImageButton callButton;

    private String volunteerKey;
    private String SourceKey;
    private double volunteerLatitude,volunteerLongitude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
//            CameraPosition mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        setContentView(R.layout.activity_user_);
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUser();
        checkEmergencyNumber();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        distressSignalButton = findViewById(R.id.DistressSignal);
        final LinearLayout linearLayout = findViewById(R.id.mapLayout);
        callLayout = findViewById(R.id.call);
        loader = findViewById(R.id.loader);

        linearLayout.setVisibility(View.VISIBLE);
        distressSignalButton.setVisibility(View.VISIBLE);

        requestQueue = Volley.newRequestQueue(this);

        FirebaseMessaging.getInstance().subscribeToTopic(userID);

        FloatingActionButton backButton = findViewById(R.id.settings);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(User_Activity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        Button AttendingButton = findViewById(R.id.Attending_button);

        vname = findViewById(R.id.Volunteer_name);
        vphone = findViewById(R.id.Volunteer_phone);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        callButton = findViewById(R.id.callButton);
        distressSignalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                builder.setTitle("Confirm Your Request");
                builder.setMessage("Kindly confirm your request for help");
                builder.setCancelable(false);
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Help is on its way", Toast.LENGTH_SHORT).show();
                        findVolunteers();


                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });


        Intent intent = getIntent();
        if (intent.hasExtra("DLatitude")) {
            destinationLatitude = intent.getStringExtra("DLatitude");
            destinationLongitude = intent.getStringExtra("DLongitude");
            SourceKey = intent.getStringExtra("Skey");
            String notificationSender = intent.getStringExtra("Sender");
            helping = true;
            switch (notificationSender) {
                case "DistressSignal":
                    AttendingButton.setVisibility(View.VISIBLE);
                    AttendingButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendAttendingNotification(SourceKey);
                        }
                    });
                    break;
                case "Volunteer":
                    createDistressSignalLocationOnMap(destinationLatitude, destinationLongitude);
                    break;

            }

        }
        WorkManager.getInstance(this).cancelAllWork();
        if (mLocationPermissionGranted) {
            startLocationWorker();
        }


    }

    private void sendAttendingNotification(String SKey) {
        String Lat = String.valueOf(Latitude);
        String Long = String.valueOf(Longitude);

        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put("to", "/topics/" + SKey);
            JSONObject notificationObject = new JSONObject();
            notificationObject.put("title", " " + Name + " is on their way to help you. Hang Tight");
            notificationObject.put("body", " Phone Number : "+Phone_number);
            JSONObject locationData = new JSONObject();
            locationData.put("Latitude", Lat);
            locationData.put("Longitude", Long);
            locationData.put("Key",userID);
            locationData.put("Sender","Volunteer");

            mainObj.put("notification", notificationObject);
            mainObj.put("data", locationData);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL,
                    mainObj,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }
            )
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=AIzaSyA-spthIkyVNryk0TVAFUqTvRenjeP3FeI");
                    return header;
                }
            };
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void checkEmergencyNumber(){


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String emergencyContact = sharedPreferences.getString(getString(R.string.emergency_contact_preference_key), "Not set");
        if(emergencyContact !=null){
            if(emergencyContact.equals("Not set")|| emergencyContact.length()!=10) {
                Toast.makeText(this, "Number not set or number format isn't proper", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationWorker() {
        Data workerData = new Data.Builder().putString("uid", userID).build();
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest
                .Builder(LocationWorker.class, 15, TimeUnit.MINUTES)
                .setInputData(workerData).build();
        WorkManager.getInstance(this).enqueue(workRequest);
    }



    private void addNotifiedVolunteer(String key){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LocationData");
        DatabaseReference notifiedVolunteer = FirebaseDatabase.getInstance().getReference("keyFound");

        final GeoFire findVolunteerGeoFire = new GeoFire(ref);
        final GeoFire notifiedVolunteerGeoFire = new GeoFire(notifiedVolunteer);

        findVolunteerGeoFire.getLocation(key, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {

                    volunteerKey = key;
                    volunteerLatitude = location.latitude;
                    volunteerLongitude = location.longitude;
                    Log.v("foundVolunteer","lat:"+volunteerLatitude+" long:"+volunteerLongitude);

                    notifiedVolunteerGeoFire.setLocation(volunteerKey,new GeoLocation(volunteerLatitude,volunteerLongitude));
                    findVolunteerGeoFire.removeLocation(volunteerKey);

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

    private void removeNotifiedVolunteer(String key){

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LocationData");
        DatabaseReference notifiedVolunteer = FirebaseDatabase.getInstance().getReference("keyFound");

        final GeoFire findVolunteerGeoFire = new GeoFire(ref);
        final GeoFire notifiedVolunteerGeoFire = new GeoFire(notifiedVolunteer);

        notifiedVolunteerGeoFire.getLocation(key, new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {

                    volunteerKey = key;
                    volunteerLatitude = location.latitude;
                    volunteerLongitude = location.longitude;
                    Log.v("removedVolunteer","lat:"+volunteerLatitude+" long:"+volunteerLongitude);
                    findVolunteerGeoFire.setLocation(volunteerKey,new GeoLocation(volunteerLatitude,volunteerLongitude));
                    notifiedVolunteerGeoFire.removeLocation(volunteerKey);

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

     void findVolunteers() {

        final DatabaseReference findVolunteer = FirebaseDatabase.getInstance().getReference("LocationData");
        final DatabaseReference notifiedVolunteer = FirebaseDatabase.getInstance().getReference("keyFound");

        final GeoFire geoFire = new GeoFire(findVolunteer);
        getDeviceLocation();
        final GeoQuery findVol = geoFire.queryAtLocation(new GeoLocation(Latitude, Longitude), radius);
        findVol.removeAllListeners();

         findVol.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!volunteerFound && !key.equals(userID)) {
                        if(noOfVolunteers <= 2){

                            volunteerFoundID[noOfVolunteers] = key;
                            noOfVolunteers++;
                            Log.v("volunteer",key+" "+radius);

                            addNotifiedVolunteer(key);
                            sendNotification(key,Latitude,Longitude);
                            findVolunteers();

                        } else {

                            for (String K:volunteerFoundID) {
                                removeNotifiedVolunteer(K);
                            }

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
                if (!volunteerFound) {
                    radius += 0.1;
                    findVolunteers();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
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
        setMapStyle(mMap);
        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        if (helping) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(Double.valueOf(destinationLatitude), Double.valueOf(destinationLongitude))).title("destination"));
            mMap.addMarker(new MarkerOptions().position(new LatLng(Latitude, Longitude)).title("source"));
            createDistressSignalLocationOnMap(destinationLatitude, destinationLongitude);

        }

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
                startLocationWorker();
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

        final DatabaseReference userData = FirebaseDatabase.getInstance().getReference("Users").child(userID);
        userData.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Gender = dataSnapshot.child("gender").getValue(String.class);
                if (Gender.equals("MALE")) {
                    loader.setVisibility(View.INVISIBLE);
                    //distressSignalButton.setVisibility(View.INVISIBLE);

                } else if (Gender.equals("FEMALE")) {
                    loader.setVisibility(View.INVISIBLE);
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
        LatLng source = new LatLng(Latitude, Longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(source));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        updateLocationInRealtime(Latitude, Longitude);


    }

    private void writeToFirebaseDatabase(Double latitude, Double longitude) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LocationData");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(userID, new GeoLocation(latitude, longitude));
    }

    private void updateLocationInRealtime(Double latitude, Double longitude) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LocationData");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(userID, new GeoLocation(latitude, longitude));
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

    private void currentUser(){

        DatabaseReference currentUser = FirebaseDatabase.getInstance().getReference("Users").child(userID);
       currentUser.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               Name = dataSnapshot.child("name").getValue(String.class);
               Phone_number = dataSnapshot.child("phone").getValue(String.class);

           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });
    }

    private void volunteerDetails(String Key){
        DatabaseReference VolunteerDetails = FirebaseDatabase.getInstance().getReference("Users").child(Key);
        VolunteerDetails.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                VName = dataSnapshot.child("name").getValue(String.class);
                Vphone = dataSnapshot.child("phone").getValue(String.class);
                Log.v(TAG," "+VName+"/"+Vphone);

                callLayout.setVisibility(View.VISIBLE);
                vname.setText(VName);
                vphone.setText(Vphone);
                callButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:"+Vphone));
                        startActivity(callIntent);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void sendNotification(final String key, final Double latitude, final Double longitude) {
        volunteerDetails(key);

        String Lat = String.valueOf(latitude);
        String Long = String.valueOf(longitude);

        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put("to", "/topics/" + key);
            JSONObject notificationObject = new JSONObject();
            notificationObject.put("title", " " + Name + " needs your help");
            notificationObject.put("body", " Phone Number : "+Phone_number);
            JSONObject locationData = new JSONObject();
            locationData.put("Latitude", Lat);
            locationData.put("Longitude", Long);
            locationData.put("Key",userID);
            locationData.put("Sender","DistressSignal");

            mainObj.put("notification", notificationObject);
            mainObj.put("data", locationData);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, URL,
                    mainObj,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }
            )
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=AIzaSyA-spthIkyVNryk0TVAFUqTvRenjeP3FeI");
                    return header;
                }
            };
            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void createDistressSignalLocationOnMap(String dLatitude, String dLongitude) {
        distressSignalButton.setVisibility(View.GONE);
        final double dLat = Double.valueOf(dLatitude);
        final double dLong =Double.valueOf(dLongitude);
        LatLng destination = new LatLng(dLat, dLong);
        LatLng source = new LatLng(Latitude, Longitude);
        String url = getUrl(destination, source);
        new FetchURL(User_Activity.this).execute(url, "driving");
    }


    private String getUrl(LatLng destination, LatLng source) {
        String org = "origin=" + source.latitude + "," + source.longitude;
        String dest = "destination=" + destination.latitude + "," + destination.longitude;
        String mode = "mode=" + "Driving";
        String parameters = org + "&" + dest + "&" + mode;
        String output = "json";
        return " https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
    }

    private void setMapStyle(GoogleMap map) {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }
    @Override
    public void onTaskDone(Object... values) {

        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);
    }

}

