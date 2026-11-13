package com.example.doctorappointmentapp;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

// PAYMENT MODULE - written with help from Claude (Anthropic AI) as a starting point.
// Reviewed, understood and modified by: V.G.Orshahn  (acknowledged as required by the assignment)

// Reads all payments from SQLite and shows them in a RecyclerView.
public class PaymentHistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_history);
        setTitle("Payment History");

        List<Payment> payments = new DatabaseHelper(this).getAllPayments();
        RecyclerView rv = findViewById(R.id.rvPayments);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new PaymentAdapter(payments));

        // Empty state message
        findViewById(R.id.tvEmpty).setVisibility(payments.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
