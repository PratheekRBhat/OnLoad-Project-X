package com.example.onload_project_x;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

    private FirebaseAuth mAuth;
    DatabaseReference users;
    private EditText nameET, emailET, numberET, passwordET;
    private RadioGroup genderRG;
    String name, email, password, number, gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth=FirebaseAuth.getInstance();
        FirebaseDatabase database =FirebaseDatabase.getInstance();
        users=database.getReference("Users");
        emailET=findViewById(R.id.emailET);
        nameET = findViewById(R.id.namET);
        numberET = findViewById(R.id.phone_ET);
        passwordET = findViewById(R.id.passwordET);
        genderRG = findViewById(R.id.genderPref);
        TextView submitButton = findViewById(R.id.register_btn);
        FloatingActionButton backButton = findViewById(R.id.signUpBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(emailET.getText().toString())){
                    Toast.makeText(getApplicationContext(), "Enter Valid Email", Toast.LENGTH_SHORT).show();
                }
                else  if(TextUtils.isEmpty(nameET.getText().toString())){
                    Toast.makeText(getApplicationContext(), "Enter Valid Name", Toast.LENGTH_SHORT).show();
                }
                else if(numberET.getText().toString().length() != 10){
                    Toast.makeText(getApplicationContext(), "Enter Valid Phone Number", Toast.LENGTH_SHORT).show();
                }
                else if(passwordET.getText().toString().length()!=6){
                    Toast.makeText(getApplicationContext(), "Enter Valid Password", Toast.LENGTH_SHORT).show();
                }
                else if(genderRG.getCheckedRadioButtonId()==-1){
                    Toast.makeText(getApplicationContext(), "Please Select Your Gender", Toast.LENGTH_SHORT).show();
                }
                else{
                    email = emailET.getText().toString();
                    name = nameET.getText().toString();
                    password = passwordET.getText().toString();
                    number = numberET.getText().toString();
                    int selectedID = genderRG.getCheckedRadioButtonId();
                    RadioButton selectedRadioButton = findViewById(selectedID);
                    gender = selectedRadioButton.getText().toString();
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        final FirebaseUser user = mAuth.getCurrentUser();
                                        User use = new User();
                                        use.setEmail(email);
                                        use.setName(name);
                                        use.setPhone(number);
                                        use.setPassword(password);
                                        use.setGender(gender);
                                        users.child(user.getUid()).setValue(use).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                updateUi(user);
                                            }
                                        });
                                    }
                                    else {
                                        Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                        updateUi(null);
                                    }
                                }
                            });
                }

            }
        });
    }

    private void updateUi(FirebaseUser user) {
        if(user!=null){
            Intent intent = new Intent(SignUpActivity.this, UserActivity.class);
            startActivity(intent);
        }
    }
}
