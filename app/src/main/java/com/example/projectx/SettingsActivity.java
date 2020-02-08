package com.example.projectx;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class SettingsActivity extends AppCompatActivity {
    private String contactNumber=" ",userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings2);
        TextView emergencyContactTV = findViewById(R.id.ContactTV);
        TextView signOutTV = findViewById(R.id.signOut);
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();



        signOutTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                DatabaseReference loggedOutUserLocationData = FirebaseDatabase.getInstance().getReference("LocationData").child(userID);
                loggedOutUserLocationData.removeValue();
                Intent intent = new Intent(SettingsActivity.this, LandingPageActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}
