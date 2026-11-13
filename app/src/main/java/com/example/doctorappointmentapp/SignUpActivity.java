package com.example.doctorappointmentapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.doctorappointmentapp.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

public class SignUpActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPhone, etDob, etPassword,
            etConfirmPassword, etEmergency;
    private TextInputLayout nameLayout, emailLayout, phoneLayout, dobLayout,
            passwordLayout, confirmLayout, emergencyLayout;
    private Spinner spinnerGender;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        dbHelper = new DatabaseHelper(this);

        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        phoneLayout = findViewById(R.id.phoneLayout);
        dobLayout = findViewById(R.id.dobLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        confirmLayout = findViewById(R.id.confirmLayout);
        emergencyLayout = findViewById(R.id.emergencyLayout);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etDob = findViewById(R.id.etDob);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etEmergency = findViewById(R.id.etEmergency);

        spinnerGender = findViewById(R.id.spinnerGender);

        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"Select gender", "Female", "Male", "Other"}
        );
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        etDob.setOnClickListener(v -> showDatePicker());

        Button btnCreate = findViewById(R.id.btnCreateAccount);
        TextView tvLogin = findViewById(R.id.tvLogin);

        btnCreate.setOnClickListener(v -> createAccount());

        tvLogin.setOnClickListener(v -> finish());
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

    private void createAccount() {
        clearErrors();

        String name = getText(etName);
        String email = getText(etEmail);
        String phone = getText(etPhone);
        String dob = getText(etDob);
        String password = getText(etPassword);
        String confirm = getText(etConfirmPassword);
        String emergency = getText(etEmergency);
        Object selectedGender = spinnerGender.getSelectedItem();
        String gender = selectedGender == null ? "" : selectedGender.toString();

        if (name.isEmpty()) {
            nameLayout.setError("Please enter your full name");
            return;
        }

        if (name.length() < 3) {
            nameLayout.setError("Name must contain at least 3 characters");
            return;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address");
            return;
        }

        if (dbHelper.emailExists(email)) {
            emailLayout.setError("An account with this email already exists");
            return;
        }

        if (phone.isEmpty() || phone.length() < 9) {
            phoneLayout.setError("Enter a valid phone number");
            return;
        }

        if (dob.isEmpty()) {
            dobLayout.setError("Please select your date of birth");
            return;
        }

        if (gender.equals("Select gender") || gender.isEmpty()) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            passwordLayout.setError("Password must be at least 8 characters");
            return;
        }

        if (!password.matches(".*[A-Z].*")) {
            passwordLayout.setError("Add at least one uppercase letter");
            return;
        }

        if (!password.matches(".*[0-9].*")) {
            passwordLayout.setError("Add at least one number");
            return;
        }

        if (!password.equals(confirm)) {
            confirmLayout.setError("Passwords do not match");
            return;
        }

        if (emergency.isEmpty() || emergency.length() < 9) {
            emergencyLayout.setError("Enter an emergency contact number");
            return;
        }

        boolean inserted = dbHelper.registerUser(
                name, email, phone, dob, gender, password, emergency
        );

        if (inserted) {
            Toast.makeText(
                    this,
                    "Account created successfully!",
                    Toast.LENGTH_LONG
            ).show();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("registeredEmail", email);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(
                    this,
                    "Unable to create account. Please try again.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void clearErrors() {
        nameLayout.setError(null);
        emailLayout.setError(null);
        phoneLayout.setError(null);
        dobLayout.setError(null);
        passwordLayout.setError(null);
        confirmLayout.setError(null);
        emergencyLayout.setError(null);
    }

    private String getText(TextInputEditText editText) {
        return editText == null || editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
