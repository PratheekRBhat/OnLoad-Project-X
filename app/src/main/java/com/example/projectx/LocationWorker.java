package com.example.projectx;

import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.ExecutionException;

public class LocationWorker extends Worker {

    public LocationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    private void updateLocationInRealtimeFromBackgroud(String userId, Double latitude, Double longitude) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("https://project-x-b7828.firebaseio.com/LocationData/" + userId + "/l/0").setValue(latitude);
        ref.child("https://project-x-b7828.firebaseio.com/LocationData/" + userId + "/l/1").setValue(longitude);
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
                    updateLocationInRealtimeFromBackgroud(uid, location.getLatitude(), location.getLongitude());
                }
            }));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return Result.success();
    }
}
