package com.example.projectx;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    DatabaseReference user;
    private MaterialButton submitButton;
    private TextInputEditText emailET, nameET, phoneET, passwordET;
    private RadioGroup genderRG;
    String name, email, password, phone, gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        emailET = findViewById(R.id.email);
        nameET = findViewById(R.id.full_name_et);
        phoneET = findViewById(R.id.phone);
        passwordET = findViewById(R.id.password);
        genderRG = findViewById(R.id.Gender_radio_group);
        submitButton = findViewById(R.id.register_btn);

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
                    Toast.makeText(getApplicationContext(), "Success " + gender, Toast.LENGTH_SHORT).show();


                }
            }
        });
    }
}
