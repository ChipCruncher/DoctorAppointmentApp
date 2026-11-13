package com.example.doctorappointmentapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private TextInputLayout emailLayout, passwordLayout;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);

        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        String registeredEmail = getIntent().getStringExtra("registeredEmail");
        if (registeredEmail != null && etEmail != null) {
            etEmail.setText(registeredEmail);
        }

        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvSignUp = findViewById(R.id.tvSignUp);
        TextView tvForgot = findViewById(R.id.tvForgot);

        btnLogin.setOnClickListener(v -> login());

        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));

        tvForgot.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void login() {
        String email = getText(etEmail);
        String password = getText(etPassword);

        emailLayout.setError(null);
        passwordLayout.setError(null);

        if (email.isEmpty()) {
            emailLayout.setError("Please enter your email address");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address");
            return;
        }

        if (password.isEmpty()) {
            passwordLayout.setError("Please enter your password");
            return;
        }

        if (dbHelper.loginUser(email, password)) {
            Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("email", email.toLowerCase().trim());
            startActivity(intent);
            finish();
        } else {
            passwordLayout.setError("Email or password is incorrect");
        }
    }

    private String getText(TextInputEditText editText) {
        return editText == null || editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
