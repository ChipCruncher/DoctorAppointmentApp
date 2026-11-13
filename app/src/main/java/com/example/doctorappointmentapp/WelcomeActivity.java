package com.example.doctorappointmentapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button loginButton = findViewById(R.id.btnLogin);
        Button signUpButton = findViewById(R.id.btnSignUp);

        loginButton.setOnClickListener(v ->
                startActivity(new Intent(this, com.example.doctorappointmentapp.LoginActivity.class)));

        signUpButton.setOnClickListener(v ->
                startActivity(new Intent(this, com.example.doctorappointmentapp.SignUpActivity.class)));
    }
}
