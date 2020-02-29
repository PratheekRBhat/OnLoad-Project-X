package com.example.onload_project_x;

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

    private void updateLocationInRealtimeFromBackground(String userId, Double latitude, Double longitude){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("LocationData");
        GeoFire geoFire = new GeoFire(reference);
        geoFire.setLocation(userId, new GeoLocation(latitude, longitude));
    }
    @NonNull
    @Override
    public Result doWork() {
        final String uid = getInputData().getString("uid");
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        try{
            Tasks.await(fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (FirebaseAuth.getInstance().getCurrentUser() != null){
                        updateLocationInRealtimeFromBackground(uid, location.getLatitude(), location.getLongitude());
                    }
                }
            }));
        } catch (ExecutionException e){
            e.printStackTrace();
        } catch (InterruptedException i){
            i.getMessage();
        }

        return Result.success();
    }
}
