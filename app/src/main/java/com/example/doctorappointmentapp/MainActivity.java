package com.example.doctorappointmentapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

// AI-assisted (Claude). Spinner cascade pattern from developer.android.com Spinners guide;
// DatePickerDialog pattern from developer.android.com
public class MainActivity extends AppCompatActivity {

    private static final int SLOT_MINUTES = 30;
    private static final int TEMP_USER_ID = 1;   // NEW: the test user, replaced by the logged-in user later

    private DatabaseHelper db;                   // NEW
    private Spinner spHospital, spSpecialization, spDoctor, spTime;
    private Button btnDate, btnConfirm;
    private String selectedDate = null;          // "YYYY-MM-DD", null until chosen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);           // NEW

        spHospital = findViewById(R.id.spHospital);
        spSpecialization = findViewById(R.id.spSpecialization);
        spDoctor = findViewById(R.id.spDoctor);
        spTime = findViewById(R.id.spTime);
        btnDate = findViewById(R.id.btnDate);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Fake patient details (will come from the account later)
        ((TextView) findViewById(R.id.tvPatientName)).setText("Nimal Perera");
        ((TextView) findViewById(R.id.tvPatientPhone)).setText("077 123 4567");

        // Start state: only the hospital dropdown is usable
        loadSpinner(spHospital, "Select hospital",
                db.getHospitalNames().toArray(new String[0]));   // NEW: from SQLite
        resetSpinner(spSpecialization, "Select specialization");
        resetSpinner(spDoctor, "Select doctor");
        resetSpinner(spTime, "Select time");
        updateConfirmButton();

        btnDate.setOnClickListener(v -> showDatePicker());
        btnConfirm.setOnClickListener(v -> saveBooking());    // NEW

        spHospital.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    resetSpinner(spSpecialization, "Select specialization");
                } else {
                    String hospital = parent.getItemAtPosition(position).toString();
                    loadSpinner(spSpecialization, "Select specialization",
                            db.getSpecializations(hospital).toArray(new String[0]));  // NEW
                }
                resetSpinner(spDoctor, "Select doctor");
                resetSpinner(spTime, "Select time");
                updateConfirmButton();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        spSpecialization.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    resetSpinner(spDoctor, "Select doctor");
                } else {
                    String hospital = spHospital.getSelectedItem().toString();
                    String specialization = parent.getItemAtPosition(position).toString();
                    loadSpinner(spDoctor, "Select doctor",
                            db.getDoctorNames(hospital, specialization)
                                    .toArray(new String[0]));                         // NEW
                }
                resetSpinner(spTime, "Select time");
                updateConfirmButton();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        spDoctor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshTimes();
                updateConfirmButton();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        spTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateConfirmButton();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        findViewById(R.id.btnMyAppointments).setOnClickListener(v ->
                startActivity(new Intent(this, MyAppointmentsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTimes();
        updateConfirmButton();
    }

    // ---------- Date picker ----------

    private void showDatePicker() {
        Calendar today = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (picker, year, month, day) -> {
                    selectedDate = String.format(Locale.US, "%04d-%02d-%02d",
                            year, month + 1, day);   // month is 0-based
                    btnDate.setText(selectedDate);
                    refreshTimes();
                    updateConfirmButton();
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    // ---------- What the time dropdown shows ----------

    private void refreshTimes() {
        boolean doctorChosen = spDoctor.isEnabled() && spDoctor.getSelectedItemPosition() > 0;
        if (!doctorChosen || selectedDate == null) {
            resetSpinner(spTime, "Select time");
            return;
        }

        String doctorName = spDoctor.getSelectedItem().toString();
        String[] info = db.getDoctorInfo(doctorName);        // NEW: hours from SQLite
        if (info == null) {
            resetSpinner(spTime, "No free times");
            return;
        }

        List<String> booked = db.getBookedTimes(doctorName, selectedDate);   // NEW
        List<String> slots = generateSlots(info[2], info[3], booked);        // info[2], info[3] = start, end

        if (selectedDate.equals(LocalDate.now().toString())) {
            String now = LocalTime.now().toString().substring(0, 5);
            slots.removeIf(time -> time.compareTo(now) <= 0);
        }

        if (slots.isEmpty()) {
            resetSpinner(spTime, "No free times");
        } else {
            loadSpinner(spTime, "Select time", slots.toArray(new String[0]));
        }
    }

    // ---------- NEW: save the booking ----------

    private void saveBooking() {
        String doctorName = spDoctor.getSelectedItem().toString();
        String time = spTime.getSelectedItem().toString();
        String[] info = db.getDoctorInfo(doctorName);
        if (info == null) return;

        long result = db.bookAppointment(
                TEMP_USER_ID,
                Integer.parseInt(info[0]),       // doctor_id
                selectedDate,
                time,
                Integer.parseInt(info[1]));      // fee

        if (result == -1) {
            // The unique index refused it: someone already holds that slot
            Toast.makeText(this, "That time was just taken. Please pick another.",
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Appointment booked for " + selectedDate + " at " + time,
                    Toast.LENGTH_LONG).show();
        }
        refreshTimes();             // reload free times (the booked one disappears)
        updateConfirmButton();
    }

    // ---------- Confirm is only enabled when everything is chosen ----------

    private void updateConfirmButton() {
        boolean ready = spHospital.getSelectedItemPosition() > 0
                && spSpecialization.isEnabled() && spSpecialization.getSelectedItemPosition() > 0
                && spDoctor.isEnabled() && spDoctor.getSelectedItemPosition() > 0
                && selectedDate != null
                && spTime.isEnabled() && spTime.getSelectedItemPosition() > 0;
        btnConfirm.setEnabled(ready);
        btnConfirm.setAlpha(ready ? 1f : 0.5f);
    }

    // ---------- Slot generation ----------

    private List<String> generateSlots(String startTime, String endTime,
                                       List<String> bookedTimes) {
        List<String> slots = new ArrayList<>();
        LocalTime current = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        while (!current.plusMinutes(SLOT_MINUTES).isAfter(end)) {
            String time = current.toString();
            if (!bookedTimes.contains(time)) {
                slots.add(time);
            }
            current = current.plusMinutes(SLOT_MINUTES);
        }
        return slots;
    }

    // ---------- Spinner helpers ----------

    private void loadSpinner(Spinner spinner, String placeholder, String[] items) {
        List<String> list = new ArrayList<>();
        list.add(placeholder);
        list.addAll(Arrays.asList(items));
        spinner.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, list));
        spinner.setEnabled(true);
        spinner.setAlpha(1f);
    }

    private void resetSpinner(Spinner spinner, String placeholder) {
        spinner.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{placeholder}));
        spinner.setEnabled(false);
        spinner.setAlpha(0.5f);
    }
}