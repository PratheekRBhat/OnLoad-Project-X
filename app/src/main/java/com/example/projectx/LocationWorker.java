package com.example.projectx;

import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.ExecutionException;

public class LocationWorker extends Worker {

    public LocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    private void updateLocationInRealtimeFromBackgroud(String userId, Double latitude, Double longitude) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("LocationData");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(userId, new GeoLocation(latitude, longitude));
    }

    @NonNull
    @Override
    public Result doWork() {
        final String uid = getInputData().getString("uid");
        FusedLocationProviderClient locationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        try {
            Tasks.await(locationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    //Don't update values in backend if user is logged out
                    if(FirebaseAuth.getInstance().getCurrentUser()!=null){
                        updateLocationInRealtimeFromBackgroud(uid, location.getLatitude(), location.getLongitude());
                    }
                }
            }));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.getMessage();
        }
        return Result.success();
    }
}
