package com.example.projectx;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private FirebaseAuth mAuth;
    DatabaseReference users;
    private EditText emailET, nameET, phoneET, passwordET;
    private RadioGroup genderRG;
    String name, email, password, phone, gender;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        users = database.getReference("Users");
        emailET = findViewById(R.id.email);
        nameET = findViewById(R.id.full_name_et);
        phoneET = findViewById(R.id.phone);
        passwordET = findViewById(R.id.password);
        genderRG = findViewById(R.id.Gender_radio_group);
        TextView submitButton = findViewById(R.id.register_btn);
        FloatingActionButton backButton = findViewById(R.id.signUpbackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(emailET.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Enter Valid Email", Toast.LENGTH_SHORT).show();

                } else if (TextUtils.isEmpty(nameET.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Enter Valid Name", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(passwordET.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Enter Valid Password", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(phoneET.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Enter Valid Name", Toast.LENGTH_SHORT).show();
                } else if (phoneET.getText().toString().length() != 10) {
                    Toast.makeText(getApplicationContext(), "Enter Valid Phone number", Toast.LENGTH_SHORT).show();
                } else if (passwordET.getText().toString().length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password has to be more than 5 Characters", Toast.LENGTH_SHORT).show();
                } else if (genderRG.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(getApplicationContext(), "Enter Valid Gender", Toast.LENGTH_SHORT).show();
                } else {


                    email = emailET.getText().toString();
                    name = nameET.getText().toString();
                    phone = phoneET.getText().toString();
                    password = passwordET.getText().toString();
                    int selectedId = genderRG.getCheckedRadioButtonId();
                    RadioButton selectedRadioButton = findViewById(selectedId);
                    gender = selectedRadioButton.getText().toString();
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();

                                        Users use = new Users();
                                        use.setEmail(email);
                                        use.setName(name);
                                        use.setPhone(phone);
                                        use.setPassword(password);
                                        use.setGender(gender);

                                        users.child(user.getUid()).setValue(use).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                        updateUI(user);
                                    } else {
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                        updateUI(null);
                                    }
                                }
                            });


                }
            }
        });
    }

    private void updateUI(FirebaseUser firebaseUser) {
        if (firebaseUser != null) {
            Intent intent = new Intent(SignUpActivity.this, User_Activity.class);
            startActivity(intent);
        }
    }


}
