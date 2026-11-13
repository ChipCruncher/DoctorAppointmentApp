package com.example.doctorappointmentapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// PAYMENT MODULE - written with help from Claude (Anthropic AI) as a starting point.
// Reviewed, understood and modified by: V.G.Orshahn  (acknowledged as required by the assignment)

// Payment screen: shows appointment summary, validates card details,
// simulates processing (dummy payment gateway) and saves the result to SQLite.
public class PaymentActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilNumber, tilExpiry, tilCvv;
    private TextInputEditText etName, etNumber, etExpiry, etCvv;
    private View cardForm;
    private RadioGroup rgMethod;
    private Button btnPay;
    private ProgressBar progress;

    private String doctorName, appointmentDate;
    private double fee;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        setTitle("Payment");

        db = new DatabaseHelper(this);

        // Data sent from the teammate's appointment screen (defaults used for testing)
        Intent in = getIntent();
        doctorName = in.getStringExtra("doctor_name");
        appointmentDate = in.getStringExtra("appointment_date");
        fee = in.getDoubleExtra("fee", 2500.00);
        if (doctorName == null) doctorName = "Dr. Silva";
        if (appointmentDate == null) appointmentDate = "14/11/2026 10:00 AM";

        ((TextView) findViewById(R.id.tvDoctor)).setText(doctorName);
        ((TextView) findViewById(R.id.tvDate)).setText(appointmentDate);
        ((TextView) findViewById(R.id.tvFee)).setText(String.format(Locale.US, "LKR %.2f", fee));

        tilName = findViewById(R.id.tilName);
        tilNumber = findViewById(R.id.tilNumber);
        tilExpiry = findViewById(R.id.tilExpiry);
        tilCvv = findViewById(R.id.tilCvv);
        etName = findViewById(R.id.etName);
        etNumber = findViewById(R.id.etNumber);
        etExpiry = findViewById(R.id.etExpiry);
        etCvv = findViewById(R.id.etCvv);
        cardForm = findViewById(R.id.cardForm);
        rgMethod = findViewById(R.id.rgMethod);
        btnPay = findViewById(R.id.btnPay);
        progress = findViewById(R.id.progress);

        // Hide the card form when "Pay at clinic" is selected
        rgMethod.setOnCheckedChangeListener((group, id) ->
                cardForm.setVisibility(id == R.id.rbCard ? View.VISIBLE : View.GONE));

        btnPay.setOnClickListener(v -> onPayClicked());
        findViewById(R.id.btnHistory).setOnClickListener(v ->
                startActivity(new Intent(this, PaymentHistoryActivity.class)));
    }

    private void onPayClicked() {
        boolean isCard = rgMethod.getCheckedRadioButtonId() == R.id.rbCard;
        if (isCard && !validateCard()) return;

        // Show loading state and block double taps
        btnPay.setEnabled(false);
        progress.setVisibility(View.VISIBLE);

        // DUMMY payment gateway: waits 2 seconds, then returns a fake result
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String number = isCard ? digitsOnly(etNumber.getText()) : "";
            // Test card ending 0002 behaves like a "declined" card for demo purposes
            boolean success = !(isCard && number.endsWith("0002"));
            savePayment(isCard, number, success);
            progress.setVisibility(View.GONE);
            btnPay.setEnabled(true);
            showResult(success);
        }, 2000);
    }

    // ---------- Validation ----------
    private boolean validateCard() {
        boolean ok = true;
        tilName.setError(null); tilNumber.setError(null);
        tilExpiry.setError(null); tilCvv.setError(null);

        String name = String.valueOf(etName.getText()).trim();
        if (name.isEmpty()) { tilName.setError("Enter the name on the card"); ok = false; }

        String num = digitsOnly(etNumber.getText());
        if (num.length() != 16) { tilNumber.setError("Card number must be 16 digits"); ok = false; }
        else if (!passesLuhn(num)) { tilNumber.setError("Invalid card number"); ok = false; }

        if (!isExpiryValid(String.valueOf(etExpiry.getText()).trim())) {
            tilExpiry.setError("Use MM/YY and a future date"); ok = false;
        }

        String cvv = String.valueOf(etCvv.getText()).trim();
        if (!cvv.matches("\\d{3}")) { tilCvv.setError("CVV must be 3 digits"); ok = false; }
        return ok;
    }

    private String digitsOnly(CharSequence s) {
        return s == null ? "" : s.toString().replaceAll("\\D", "");
    }

    // Luhn algorithm: standard checksum used by real card numbers
    private boolean passesLuhn(String number) {
        int sum = 0;
        boolean doubleIt = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int d = number.charAt(i) - '0';
            if (doubleIt) { d *= 2; if (d > 9) d -= 9; }
            sum += d;
            doubleIt = !doubleIt;
        }
        return sum % 10 == 0;
    }

    private boolean isExpiryValid(String exp) {
        if (!exp.matches("(0[1-9]|1[0-2])/\\d{2}")) return false;
        int month = Integer.parseInt(exp.substring(0, 2));
        int year = 2000 + Integer.parseInt(exp.substring(3));
        Calendar now = Calendar.getInstance();
        int curYear = now.get(Calendar.YEAR);
        int curMonth = now.get(Calendar.MONTH) + 1;
        return year > curYear || (year == curYear && month >= curMonth);
    }

    // ---------- Database ----------
    private void savePayment(boolean isCard, String number, boolean success) {
        Payment p = new Payment();
        p.doctorName = doctorName;
        p.appointmentDate = appointmentDate;
        p.amount = fee;
        p.method = isCard ? "CARD" : "CASH";
        // Security: store ONLY the last 4 digits. Never store the full number or CVV.
        p.last4 = isCard ? number.substring(number.length() - 4) : "-";
        p.status = success ? "SUCCESS" : "FAILED";
        p.paidAt = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US).format(new Date());
        db.insertPayment(p);
    }

    private void showResult(boolean success) {
        new AlertDialog.Builder(this)
                .setTitle(success ? "Payment successful" : "Payment declined")
                .setMessage(success
                        ? "Your appointment with " + doctorName + " is confirmed."
                        : "Your card was declined. Please try another card.")
                .setPositiveButton(success ? "View history" : "Try again", (d, w) -> {
                    if (success) startActivity(new Intent(this, PaymentHistoryActivity.class));
                })
                .setCancelable(false)
                .show();
    }
}
