package com.example.doctorappointmentapp;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// AI-assisted (Claude). Spinner cascade using OnItemSelectedListener,
// pattern from developer.android.com Spinners guide
public class MainActivity extends AppCompatActivity {

    // Class-level, so the listeners and helper methods can all use them
    private Spinner spHospital, spSpecialization, spDoctor, spTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // ...keep the template's EdgeToEdge/insets lines here...

        spHospital = findViewById(R.id.spHospital);
        spSpecialization = findViewById(R.id.spSpecialization);
        spDoctor = findViewById(R.id.spDoctor);
        spTime = findViewById(R.id.spTime);

        // Fake patient details (will come from the account later)
        ((TextView) findViewById(R.id.tvPatientName)).setText("Nimal Perera");
        ((TextView) findViewById(R.id.tvPatientPhone)).setText("077 123 4567");

        // Start state: only the hospital dropdown is usable
        loadSpinner(spHospital, "Select hospital", getHospitals());
        resetSpinner(spSpecialization, "Select specialization");
        resetSpinner(spDoctor, "Select doctor");
        resetSpinner(spTime, "Select time");

        // When the hospital changes: refill specialization, clear what's below
        spHospital.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {                       // placeholder chosen
                    resetSpinner(spSpecialization, "Select specialization");
                } else {
                    String hospital = parent.getItemAtPosition(position).toString();
                    loadSpinner(spSpecialization, "Select specialization",
                            getSpecializations(hospital));
                }
                resetSpinner(spDoctor, "Select doctor");   // old choices are now invalid
                resetSpinner(spTime, "Select time");
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // When the specialization changes: refill doctors, clear time
        spSpecialization.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    resetSpinner(spDoctor, "Select doctor");
                } else {
                    String hospital = spHospital.getSelectedItem().toString();
                    String specialization = parent.getItemAtPosition(position).toString();
                    loadSpinner(spDoctor, "Select doctor",
                            getDoctors(hospital, specialization));
                }
                resetSpinner(spTime, "Select time");
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // When the doctor changes: fill the time dropdown
        spDoctor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    resetSpinner(spTime, "Select time");
                } else {
                    // TODO (you): replace with real slot generation
                    // (doctor's hours minus already-booked times)
                    loadSpinner(spTime, "Select time", getFakeTimes());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    // AI-assisted (Claude). Slot generation logic, uses java.time.LocalTime (API 26+)
    private static final int SLOT_MINUTES = 30;

    // Builds the list of free times for one doctor on one day.
    // Assumes working hours do not cross midnight.
    private List<String> generateSlots(String startTime, String endTime,
                                       List<String> bookedTimes) {
        List<String> slots = new ArrayList<>();
        LocalTime current = LocalTime.parse(startTime);   // "09:00" -> a time object
        LocalTime end = LocalTime.parse(endTime);

        // Keep going while a full slot still fits before closing time
        while (!current.plusMinutes(SLOT_MINUTES).isAfter(end)) {
            String time = current.toString();             // back to "09:00" text
            if (!bookedTimes.contains(time)) {            // skip already-booked times
                slots.add(time);
            }
            current = current.plusMinutes(SLOT_MINUTES);  // move to the next slot
        }
        return slots;
    }

    // Fake booked times (later: a SELECT on the appointments table)
    private List<String> getFakeBookedTimes() {
        return Arrays.asList("09:30", "11:00");
    }

    // ---------- Helpers ----------

    // Fill a dropdown with a placeholder first, then the real items, and enable it
    private void loadSpinner(Spinner spinner, String placeholder, String[] items) {
        List<String> list = new ArrayList<>();
        list.add(placeholder);
        list.addAll(Arrays.asList(items));
        spinner.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, list));
        spinner.setEnabled(true);
        spinner.setAlpha(1f);
    }

    // Empty a dropdown (placeholder only), disable it and dim it
    private void resetSpinner(Spinner spinner, String placeholder) {
        spinner.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item,
                new String[]{placeholder}));
        spinner.setEnabled(false);
        spinner.setAlpha(0.5f);
    }


    private String[] getHospitals() {
        return new String[]{"City General Hospital", "Lakeside Medical Centre",
                "Southern Care Hospital"};
    }

    private String[] getSpecializations(String hospital) {
        switch (hospital) {
            case "City General Hospital":
                return new String[]{"General", "Cardiology"};
            case "Lakeside Medical Centre":
                return new String[]{"General", "Pediatrics"};
            default:
                return new String[]{"Cardiology", "Pediatrics"};
        }
    }

    private String[] getDoctors(String hospital, String specialization) {
        return new String[]{"Dr. Nimal Silva (" + specialization + ")",
                "Dr. Kamala Perera (" + specialization + ")"};
    }

    private String[] getFakeTimes() {
        return new String[]{"09:00", "09:30", "10:00", "10:30"};
    }
}