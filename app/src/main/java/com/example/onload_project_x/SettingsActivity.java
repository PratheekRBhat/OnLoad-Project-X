package com.example.onload_project_x;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.awt.font.TextAttribute;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String emergencyContact = sharedPreferences.getString(getString(R.string.emergency_contact_preference_key), "Not Set");
        FloatingActionButton backButton = findViewById(R.id.settingsBack);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        final String UserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        TextView signOutTextView = findViewById(R.id.signOut);
        signOutTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                DatabaseReference loggedOutUserLocationData = FirebaseDatabase.getInstance().getReference("LocationData").child(UserId);
                loggedOutUserLocationData.removeValue();
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

        });
        TextView callEmergencyContact = findViewById(R.id.callEmergencyContact);
        callEmergencyContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+emergencyContact));
                startActivity(intent);
            }
        });
    }
}
