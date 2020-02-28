package com.example.onload_project_x;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dmax.dialog.SpotsDialog;

public class LoginInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_in);
        mAuth = FirebaseAuth.getInstance();
        final EditText l_email = findViewById(R.id.login_email);
        final EditText l_password = findViewById(R.id.login_password);
        TextView login_btn = findViewById(R.id.Login_btn);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = l_email.getText().toString();
                String password = l_password.getText().toString();
                if(email.isEmpty()&& password.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Please Fill in the credentials",Toast.LENGTH_SHORT).show();
                }
                else{
                    final AlertDialog waitingDialog = new SpotsDialog(LoginInActivity.this);
                    waitingDialog.show();
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        waitingDialog.dismiss();
                                        updateUI(user);
                                    } else {
                                        Toast.makeText(LoginInActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                        waitingDialog.dismiss();
                                        updateUI(null);
                                    }
                                }
                            });
                }
            }
        });

        TextView signUpLink = findViewById(R.id.signUpLink);
        signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        FloatingActionButton backButton = findViewById(R.id.loginPageBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(LoginInActivity.this, UserActivity.class);
            startActivity(intent);
        }
    }
}
