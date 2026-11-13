package com.example.doctorappointmentapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String email;

    private TextView tvName, tvEmail, tvPhone, tvDob, tvGender, tvEmergency;

    private final ActivityResultLauncher<Intent> editProfileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String newEmail = result.getData().getStringExtra("email");
                    if (newEmail != null && !newEmail.isEmpty()) {
                        email = newEmail;
                    }
                    loadProfile();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DatabaseHelper(this);

        email = getIntent().getStringExtra("email");

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvDob = findViewById(R.id.tvDob);
        tvGender = findViewById(R.id.tvGender);
        tvEmergency = findViewById(R.id.tvEmergency);

        Button btnEdit = findViewById(R.id.btnEditProfile);
        Button btnLogout = findViewById(R.id.btnLogout);

        loadProfile();

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            intent.putExtra("email", email);
            editProfileLauncher.launch(intent);
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dbHelper != null && email != null) {
            loadProfile();
        }
    }

    private void loadProfile() {
        if (email == null) return;
        Cursor cursor = dbHelper.getUser(email);

        if (cursor != null && cursor.moveToFirst()) {
            tvName.setText("Name: " + cursor.getString(cursor.getColumnIndexOrThrow("name")));
            tvEmail.setText("Email: " + cursor.getString(cursor.getColumnIndexOrThrow("email")));
            tvPhone.setText("Phone: " + cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            tvDob.setText("DOB: " + cursor.getString(cursor.getColumnIndexOrThrow("dob")));
            tvGender.setText("Gender: " + cursor.getString(cursor.getColumnIndexOrThrow("gender")));
            tvEmergency.setText("Emergency Contact: " + cursor.getString(
                    cursor.getColumnIndexOrThrow("emergency_contact")
            ));
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Log out?")
                .setMessage("Are you sure you want to log out of your account?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Log out", (dialog, which) -> {
                    Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, WelcomeActivity.class);
                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                    );
                    startActivity(intent);
                    finish();
                })
                .show();
    }
}
