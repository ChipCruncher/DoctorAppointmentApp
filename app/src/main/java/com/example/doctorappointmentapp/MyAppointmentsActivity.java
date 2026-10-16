package com.example.doctorappointmentapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// AI-assisted (Claude). AlertDialog pattern from Lab Worksheet 7, Activity 4
public class MyAppointmentsActivity extends AppCompatActivity {

    private static final int TEMP_USER_ID = 1;   // replaced by the logged-in user later

    private DatabaseHelper db;
    private RecyclerView rvAppointments;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_appointments);

        db = new DatabaseHelper(this);
        rvAppointments = findViewById(R.id.rvAppointments);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAppointments();      // reload every time the screen comes into view
    }

    private void loadAppointments() {
        List<Appointment> list = db.getAppointmentsForUser(TEMP_USER_ID);
        tvEmpty.setVisibility(list.isEmpty() ? android.view.View.VISIBLE
                : android.view.View.GONE);
        rvAppointments.setAdapter(new AppointmentAdapter(list, this::confirmCancel));
    }

    private void confirmCancel(Appointment a) {
        new AlertDialog.Builder(this)
                .setMessage("Cancel your appointment with " + a.getDoctorName()
                        + " on " + a.getDate() + " at " + a.getTime() + "?")
                .setPositiveButton("Yes, cancel", (dialog, which) -> {
                    db.cancelAppointment(a.getId());
                    Toast.makeText(this, "Appointment cancelled", Toast.LENGTH_SHORT).show();
                    loadAppointments();
                })
                .setNegativeButton("No", null)
                .show();
    }
}