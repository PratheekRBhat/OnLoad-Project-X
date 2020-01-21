package com.example.projectx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
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
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class User_Activity extends AppCompatActivity implements OnMapReadyCallback, LocationListener, RoutingListener {

    private static final String TAG = "volunteerLocation";
    private GoogleMap mMap;
    private LinearLayout linearLayout;
    private ProgressBar loader;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final float DEFAULT_ZOOM = 18.04f;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private boolean mLocationPermissionGranted;
    private double Latitude, Longitude;
    public String Gender, userID;
    private Location mLastKnownLocation;

    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    private int radius = 1;
    private boolean volunteerFound = false;

    private String volunteerFoundID, destinationLatitude, destinationLongitude;
    private RequestQueue requestQueue;
    private String URL = "https://fcm.googleapis.com/fcm/send";
    private boolean helping = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
//            CameraPosition mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        setContentView(R.layout.activity_user_);



        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        assert mapFragment != null;
        mapFragment.getMapAsync(this);


        Button distressSignalButton = findViewById(R.id.DistressSignal);
        linearLayout = findViewById(R.id.mapLayout);
        loader = findViewById(R.id.loader);
        linearLayout.setVisibility(View.VISIBLE);
        distressSignalButton.setVisibility(View.VISIBLE);
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        requestQueue = Volley.newRequestQueue(this);
        FirebaseMessaging.getInstance().subscribeToTopic(userID);

        polylines = new ArrayList<>();

        distressSignalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                findVolunteers();
            }
        });
        Intent intent = getIntent();
        if (intent.hasExtra("DLatitude")) {
            destinationLatitude = intent.getStringExtra("DLatitude");
            destinationLongitude = intent.getStringExtra("DLongitude");

            helping = true;
            createDistressSignalLocationOnMap(destinationLatitude, destinationLatitude);
        }
        WorkManager.getInstance(this).cancelAllWork();
        if (mLocationPermissionGranted) {
            startLocationWorker();
        }
    }


    private void startLocationWorker() {
        Data workerData = new Data.Builder().putString("uid", userID).build();
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest
                .Builder(LocationWorker.class, 15, TimeUnit.MINUTES)
                .setInputData(workerData).build();
        WorkManager.getInstance(this).enqueue(workRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menus) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.user_menu, menus);
        return true;
    }

    private void findVolunteers() {
        Toast.makeText(this, "Help is on its way", Toast.LENGTH_SHORT).show();
        DatabaseReference findVolunteer = FirebaseDatabase.getInstance().getReference("LocationData");
        GeoFire geoFire = new GeoFire(findVolunteer);
        getDeviceLocation();
        GeoQuery findVol = geoFire.queryAtLocation(new GeoLocation(Latitude, Longitude), radius);
        findVol.removeAllListeners();

        findVol.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!volunteerFound && !key.equals(userID)) {
                    volunteerFound = true;
                    volunteerFoundID = key;
                    sendNotification(volunteerFoundID, location.latitude, location.longitude);


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
                    radius += 1;
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
            DatabaseReference loggedOutUserLocationData = FirebaseDatabase.getInstance().getReference("LocationData").child(userID);
            loggedOutUserLocationData.removeValue();
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

        if (helping) {
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
                    linearLayout.setVisibility(View.VISIBLE);


                } else if (Gender.equals("FEMALE")) {
                    loader.setVisibility(View.INVISIBLE);
                    linearLayout.setVisibility(View.VISIBLE);


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
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LocationData").child("" + userID);
        ref.child("/l/0").setValue(latitude);
        ref.child("/l/1").setValue(longitude);
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

    private void sendNotification(final String key, final Double latitude, final Double longitude) {
        Toast.makeText(this, "" + latitude + " " + longitude, Toast.LENGTH_SHORT).show();

        String Lat = String.valueOf(latitude);
        String Long = String.valueOf(longitude);

        JSONObject mainObj = new JSONObject();
        try {
            mainObj.put("to", "/topics/" + key);
            JSONObject notificationObject = new JSONObject();
            notificationObject.put("title", "Emergency Alert");
            notificationObject.put("body", " " + latitude + "/ " + longitude);
            JSONObject locationData = new JSONObject();
            locationData.put("Latitude", Lat);
            locationData.put("Longitude", Long);

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
            ) {
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
        final double dLat = Double.valueOf(dLatitude);
        final double dLong = Double.valueOf(dLongitude);
        LatLng destination = new LatLng(dLat, dLong);
        try {
            mMap.addMarker(new MarkerOptions().position(destination).title("Destination"));
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), destination)
                    .build();
            routing.execute();

        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
        }


    }

    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};


    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        polylines = new ArrayList<>();

        for (int i = 0; i < route.size(); i++) {
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(), "Route " + (i + 1) + ": distance - " + route.get(i).getDistanceValue() + ": duration - " + route.get(i).getDurationValue(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingCancelled() {

    }
}
