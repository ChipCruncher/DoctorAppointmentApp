package com.example.doctorappointmentapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etNewPassword, etConfirmPassword;
    private TextInputLayout emailLayout, newPasswordLayout, confirmLayout;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        dbHelper = new DatabaseHelper(this);

        emailLayout = findViewById(R.id.emailLayout);
        newPasswordLayout = findViewById(R.id.newPasswordLayout);
        confirmLayout = findViewById(R.id.confirmLayout);

        etEmail = findViewById(R.id.etEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        Button btnReset = findViewById(R.id.btnResetPassword);

        btnReset.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        emailLayout.setError(null);
        newPasswordLayout.setError(null);
        confirmLayout.setError(null);

        String email = getText(etEmail);
        String newPassword = getText(etNewPassword);
        String confirm = getText(etConfirmPassword);

        if (email.isEmpty()) {
            emailLayout.setError("Please enter your email");
            return;
        }

        if (!dbHelper.emailExists(email)) {
            emailLayout.setError("No account found with this email");
            return;
        }

        if (newPassword.length() < 8) {
            newPasswordLayout.setError("Password must be at least 8 characters");
            return;
        }

        if (!newPassword.equals(confirm)) {
            confirmLayout.setError("Passwords do not match");
            return;
        }

        if (dbHelper.resetPassword(email, newPassword)) {
            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_LONG).show();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.putExtra("registeredEmail", email);
            startActivity(intent);
            finish();
        }
    }

    private String getText(TextInputEditText editText) {
        return editText == null || editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
