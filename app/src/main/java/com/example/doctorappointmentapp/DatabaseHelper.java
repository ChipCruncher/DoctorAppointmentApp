package com.example.doctorappointmentapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

// AI-assisted (Claude). Pattern from developer.android.com SQLiteOpenHelper guide.
// getSpecializations and bookAppointment are written by the student.
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "clinic.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);   // SQLite ignores foreign keys unless this is on
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TEMPORARY stand-in for Maghelesh's table. Delete when his is merged in.
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "full_name TEXT NOT NULL, " +
                "email TEXT NOT NULL UNIQUE, " +
                "phone TEXT NOT NULL, " +
                "password_hash TEXT NOT NULL)");

        // ---- Akeno's tables ----
        db.execSQL("CREATE TABLE hospitals (" +
                "hospital_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "address TEXT NOT NULL)");

        db.execSQL("CREATE TABLE doctors (" +
                "doctor_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "hospital_id INTEGER NOT NULL REFERENCES hospitals(hospital_id) ON DELETE CASCADE, " +
                "name TEXT NOT NULL, " +
                "specialization TEXT NOT NULL, " +
                "fee INTEGER NOT NULL, " +
                "gender TEXT, " +
                "languages TEXT, " +
                "start_time TEXT NOT NULL, " +
                "end_time TEXT NOT NULL)");

        db.execSQL("CREATE TABLE appointments (" +
                "appointment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, " +
                "doctor_id INTEGER NOT NULL REFERENCES doctors(doctor_id) ON DELETE CASCADE, " +
                "appointment_date TEXT NOT NULL, " +
                "appointment_time TEXT NOT NULL, " +
                "fee INTEGER NOT NULL, " +
                "status TEXT NOT NULL DEFAULT 'BOOKED', " +
                "payment_status TEXT NOT NULL DEFAULT 'UNPAID', " +
                "created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");

        db.execSQL("CREATE UNIQUE INDEX idx_no_double_booking " +
                "ON appointments(doctor_id, appointment_date, appointment_time) " +
                "WHERE status = 'BOOKED'");

        // ---- Orshahn's tables go here (saved_cards, payments, and their two indexes) ----

        // ---- Starter data (extend it as you like) ----
        db.execSQL("INSERT INTO users (full_name, email, phone, password_hash) " +
                "VALUES ('Nimal Perera', 'test@mail.com', '0771234567', 'x')");

        db.execSQL("INSERT INTO hospitals (name, address) VALUES " +
                "('City General Hospital', '12 Main Street, Colombo'), " +
                "('Lakeside Medical Centre', '45 Lake Road, Kandy'), " +
                "('Southern Care Hospital', '8 Beach Road, Galle')");

        db.execSQL("INSERT INTO doctors (hospital_id, name, specialization, fee, gender, " +
                "languages, start_time, end_time) VALUES " +
                "(1, 'Dr. Nimal Silva', 'Cardiology', 2500, 'M', 'English, Sinhala', '09:00', '13:00'), " +
                "(1, 'Dr. Kamala Perera', 'General', 1500, 'F', 'English, Sinhala, Tamil', '09:00', '12:00'), " +
                "(2, 'Dr. Ruwan Fernando', 'Pediatrics', 2000, 'M', 'English, Sinhala', '10:00', '14:00'), " +
                "(2, 'Dr. Anushka Jayasinghe', 'General', 1500, 'F', 'English, Tamil', '09:00', '13:00'), " +
                "(3, 'Dr. Sanjeewa Wickrama', 'Cardiology', 3000, 'M', 'English, Sinhala', '08:00', '12:00'), " +
                "(3, 'Dr. Dilini Mendis', 'Pediatrics', 2000, 'F', 'English, Sinhala', '09:00', '13:00')");

        // Test bookings so getBookedTimes has something to find
        db.execSQL("INSERT INTO appointments (user_id, doctor_id, appointment_date, " +
                "appointment_time, fee, status) VALUES " +
                "(1, 1, '2026-10-10', '09:30', 2500, 'BOOKED'), " +
                "(1, 1, '2026-10-10', '11:00', 2500, 'BOOKED'), " +
                "(1, 1, '2026-10-10', '10:00', 2500, 'CANCELLED'), " +
                "(1, 2, '2026-10-10', '09:30', 1500, 'BOOKED')");
    }

    // AI-assisted (Claude)
    // returns {doctor_id, fee, start_time, end_time}, or null if the doctor isn't found
    public String[] getDoctorInfo(String doctorName) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT doctor_id, fee, start_time, end_time FROM doctors WHERE name = ?",
                new String[]{doctorName});
        String[] info = null;
        if (c.moveToFirst()) {
            info = new String[]{c.getString(0), c.getString(1),
                    c.getString(2), c.getString(3)};
        }
        c.close();
        return info;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // MVP: wipe and rebuild, children first because of the foreign keys
        db.execSQL("DROP TABLE IF EXISTS payments");
        db.execSQL("DROP TABLE IF EXISTS saved_cards");
        db.execSQL("DROP TABLE IF EXISTS appointments");
        db.execSQL("DROP TABLE IF EXISTS doctors");
        db.execSQL("DROP TABLE IF EXISTS hospitals");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // ---------- Queries ----------

    public List<String> getHospitalNames() {
        List<String> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT name FROM hospitals ORDER BY name", null);
        while (c.moveToNext()) list.add(c.getString(0));
        c.close();
        return list;
    }

    public List<String> getDoctorNames(String hospitalName, String specialization) {
        List<String> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT d.name FROM doctors d " +
                        "JOIN hospitals h ON h.hospital_id = d.hospital_id " +
                        "WHERE h.name = ? AND d.specialization = ?",
                new String[]{hospitalName, specialization});
        while (c.moveToNext()) list.add(c.getString(0));
        c.close();
        return list;
    }

    public List<String> getBookedTimes(String doctorName, String date) {
        List<String> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT a.appointment_time FROM appointments a " +
                        "JOIN doctors d ON d.doctor_id = a.doctor_id " +
                        "WHERE d.name = ? AND a.appointment_date = ? AND a.status = 'BOOKED' " +
                        "ORDER BY a.appointment_time",
                new String[]{doctorName, date});
        while (c.moveToNext()) list.add(c.getString(0));
        c.close();
        return list;
    }

    public void cancelAppointment(int id) {
        getWritableDatabase().execSQL(
                "UPDATE appointments SET status = 'CANCELLED' WHERE appointment_id = ?",
                new Object[]{id});
    }

    public void markAsPaid(int id) {
        getWritableDatabase().execSQL(
                "UPDATE appointments SET payment_status = 'PAID' WHERE appointment_id = ?",
                new Object[]{id});
    }

    public List<String> getSpecializations(String hospitalName) {
        List<String> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT DISTINCT d.specialization FROM doctors d " +
                        "JOIN hospitals h ON h.hospital_id = d.hospital_id " +
                        "WHERE h.name = ?",
                new String[]{hospitalName});
        while (c.moveToNext()) list.add(c.getString(0));
        c.close();
        return list;
    }

    public long bookAppointment(int userId, int doctorId, String date,
                                String time, int fee) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("doctor_id", doctorId);
        values.put("appointment_date", date);
        values.put("appointment_time", time);
        values.put("fee", fee);
        return getWritableDatabase().insert("appointments", null, values);
    }
}