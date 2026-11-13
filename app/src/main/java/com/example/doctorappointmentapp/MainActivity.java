package com.example.doctorappointmentapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // MainActivity acts as the entry point for the Account module.
        startActivity(new Intent(this, WelcomeActivity.class));
        finish();
    }
}
