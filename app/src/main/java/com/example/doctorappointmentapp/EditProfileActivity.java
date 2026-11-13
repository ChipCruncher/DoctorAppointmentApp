package com.example.doctorappointmentapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private String oldEmail;

    private TextInputEditText etName, etEmail, etPhone, etDob, etGender, etEmergency;
    private TextInputLayout nameLayout, emailLayout, phoneLayout, dobLayout, genderLayout, emergencyLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        dbHelper = new DatabaseHelper(this);
        oldEmail = getIntent().getStringExtra("email");

        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        phoneLayout = findViewById(R.id.phoneLayout);
        dobLayout = findViewById(R.id.dobLayout);
        genderLayout = findViewById(R.id.genderLayout);
        emergencyLayout = findViewById(R.id.emergencyLayout);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etDob = findViewById(R.id.etDob);
        etGender = findViewById(R.id.etGender);
        etEmergency = findViewById(R.id.etEmergency);

        Button btnSave = findViewById(R.id.btnSave);

        etDob.setOnClickListener(v -> showDatePicker());

        loadUser();

        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    String date = String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            day, month + 1, year
                    );
                    etDob.setText(date);
                    dobLayout.setError(null);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private void loadUser() {
        if (oldEmail == null) return;
        Cursor cursor = dbHelper.getUser(oldEmail);

        if (cursor != null && cursor.moveToFirst()) {
            etName.setText(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            etEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            etPhone.setText(cursor.getString(cursor.getColumnIndexOrThrow("phone")));
            etDob.setText(cursor.getString(cursor.getColumnIndexOrThrow("dob")));
            etGender.setText(cursor.getString(cursor.getColumnIndexOrThrow("gender")));
            etEmergency.setText(cursor.getString(
                    cursor.getColumnIndexOrThrow("emergency_contact")
            ));
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private void saveChanges() {
        clearErrors();

        String name = getText(etName);
        String email = getText(etEmail);
        String phone = getText(etPhone);
        String dob = getText(etDob);
        String gender = getText(etGender);
        String emergency = getText(etEmergency);

        if (name.isEmpty()) {
            nameLayout.setError("Name is required");
            return;
        }

        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            return;
        }

        if (phone.isEmpty()) {
            phoneLayout.setError("Phone number is required");
            return;
        }

        if (dbHelper.emailExists(email) && !email.equalsIgnoreCase(oldEmail)) {
            emailLayout.setError("That email is already in use");
            return;
        }

        boolean updated = dbHelper.updateUser(
                oldEmail, name, email, phone, dob, gender, emergency
        );

        if (updated) {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent();
            resultIntent.putExtra("email", email);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Unable to update profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearErrors() {
        nameLayout.setError(null);
        emailLayout.setError(null);
        phoneLayout.setError(null);
        dobLayout.setError(null);
        genderLayout.setError(null);
        emergencyLayout.setError(null);
    }

    private String getText(TextInputEditText editText) {
        return editText == null || editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
