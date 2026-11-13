package com.example.doctorappointmentapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

// AI-assisted (Claude). Pattern from developer.android.com SQLiteOpenHelper guide.
// Initial SQLite database schema is written by the student.
// getSpecializations and bookAppointment are written by the student.
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "clinic.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_USERS = "users";

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
        // Database schema and some queries created by c'est moi Oleen :>
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "full_name TEXT NOT NULL, " +
                "email TEXT NOT NULL UNIQUE, " +
                "phone TEXT NOT NULL, " +
                "password_hash TEXT NOT NULL)");

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

        db.execSQL("INSERT INTO appointments (user_id, doctor_id, appointment_date, " +
                "appointment_time, fee, status) VALUES " +
                "(1, 1, '2026-10-10', '09:30', 2500, 'BOOKED'), " +
                "(1, 1, '2026-10-10', '11:00', 2500, 'BOOKED'), " +
                "(1, 1, '2026-10-10', '10:00', 2500, 'CANCELLED'), " +
                "(1, 2, '2026-10-10', '09:30', 1500, 'BOOKED')");

        // ----------------------------- ORSHAHN CREATE -------------------------------------------

        db.execSQL("CREATE TABLE payments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "doctor_name TEXT, appointment_date TEXT, amount REAL," +
                "method TEXT, last4 TEXT, status TEXT, paid_at TEXT)"
        );

        // ----------------------------- MAGHELESH CREATE ------------------------------------------

        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "email TEXT UNIQUE NOT NULL, " +
                "phone TEXT NOT NULL, " +
                "dob TEXT, " +
                "gender TEXT, " +
                "password TEXT NOT NULL, " +
                "emergency_contact TEXT)");
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
        db.execSQL("DROP TABLE IF EXISTS payments");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
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
        Log.d("DB", "getBookedTimes " + doctorName + " " + date + " -> " + list);
        return list;
    }

    public void cancelAppointment(int id) {
        ContentValues values = new ContentValues();
        values.put("status", "CANCELLED");
        int rows = getWritableDatabase().update("appointments", values,
                "appointment_id = ?", new String[]{String.valueOf(id)});
        Log.d("DB", "cancelAppointment " + id + " -> rows affected: " + rows);
    }

    public void markAsPaid(int id) {
        ContentValues values = new ContentValues();
        values.put("payment_status", "PAID");
        int rows = getWritableDatabase().update("appointments", values,
                "appointment_id = ?", new String[]{String.valueOf(id)});
        Log.d("DB", "markAsPaid " + id + " -> rows affected: " + rows);
    }

    // Oleen's queries down below :)
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

        long id = getWritableDatabase().insert("appointments", null, values);
        Log.d("DB", "bookAppointment user=" + userId + " doctor=" + doctorId
                + " " + date + " " + time + " -> " + id);   // -1 means the unique index refused it
        return id;
    }

    // AI-assisted skeleton (Claude). The SELECT is written by the student.
    public List<Appointment> getAppointmentsForUser(int userId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.appointment_id, h.name AS hospital, d.name AS doctor, "
                + "a.appointment_date, a.appointment_time, a.status "
                + "FROM appointments a "
                + "JOIN doctors d ON a.doctor_id = d.doctor_id "
                + "JOIN hospitals h ON d.hospital_id = h.hospital_id "
                + "WHERE a.user_id = ? "
                + "ORDER BY a.appointment_date, a.appointment_time";
        if (sql.isEmpty()) return list;

        Cursor c = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(userId)});
        while (c.moveToNext()) {
            list.add(new Appointment(c.getInt(0), c.getString(1), c.getString(2),
                    c.getString(3), c.getString(4), c.getString(5)));
        }
        c.close();
        return list;
    }

    // AI-assisted (Claude). Debug helper: prints a whole table to Logcat, one row per line
    // using logcat was my idea here, since it's a good testing tool I heard. [oleen]
    public void dumpTable(String table) {
        // Table names can't use "?" placeholders. This is safe because the name comes
        // from my own code, never from user input; values always use "?".
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM " + table, null);
        Log.d("DB", "---- " + table + " (" + c.getCount() + " rows) ----");
        String[] cols = c.getColumnNames();
        while (c.moveToNext()) {
            StringBuilder row = new StringBuilder();
            for (int i = 0; i < cols.length; i++) {
                row.append(cols[i]).append("=").append(c.getString(i));
                if (i < cols.length - 1) row.append(" | ");
            }
            Log.d("DB", row.toString());
        }
        c.close();
    }

    public void dumpAll() {
        dumpTable("hospitals");
        dumpTable("doctors");
        dumpTable("appointments");
    }

    // -------------------------------------- MAGHELESH DATABASE ------------------------------------

    private String safeTrim(String str) {
        return str == null ? "" : str.trim().toLowerCase();
    }

    public boolean registerUser(String name, String email, String phone,
                                String dob, String gender, String password,
                                String emergencyContact) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", name != null ? name.trim() : "");
        values.put("email", safeTrim(email));
        values.put("phone", phone != null ? phone.trim() : "");
        values.put("dob", dob != null ? dob.trim() : "");
        values.put("gender", gender != null ? gender.trim() : "");
        values.put("password", password != null ? password : "");
        values.put("emergency_contact", emergencyContact != null ? emergencyContact.trim() : "");

        try {
            long result = db.insertOrThrow(TABLE_USERS, null, values);
            return result != -1;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean loginUser(String email, String password) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + TABLE_USERS +
                        " WHERE email = ? AND password = ?",
                new String[]{safeTrim(email), password != null ? password : ""}
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public Cursor getUser(String email) {
        SQLiteDatabase db = getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE email = ?",
                new String[]{safeTrim(email)}
        );
    }

    public boolean emailExists(String email) {
        Cursor cursor = getUser(email);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean updateUser(String oldEmail, String name, String email,
                              String phone, String dob, String gender,
                              String emergencyContact) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("name", name != null ? name.trim() : "");
        values.put("email", safeTrim(email));
        values.put("phone", phone != null ? phone.trim() : "");
        values.put("dob", dob != null ? dob.trim() : "");
        values.put("gender", gender != null ? gender.trim() : "");
        values.put("emergency_contact", emergencyContact != null ? emergencyContact.trim() : "");

        int rows = db.update(
                TABLE_USERS,
                values,
                "email = ?",
                new String[]{safeTrim(oldEmail)}
        );

        return rows > 0;
    }

    public boolean resetPassword(String email, String newPassword) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("password", newPassword != null ? newPassword : "");

        int rows = db.update(
                TABLE_USERS,
                values,
                "email = ?",
                new String[]{safeTrim(email)}
        );

        return rows > 0;
    }


    // ------------------------------------- ORSHAHN DATABASE ---------------------------------------

    // WRITE
    public long insertPayment(Payment p) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("doctor_name", p.doctorName);
        cv.put("appointment_date", p.appointmentDate);
        cv.put("amount", p.amount);
        cv.put("method", p.method);
        cv.put("last4", p.last4);
        cv.put("status", p.status);
        cv.put("paid_at", p.paidAt);
        return db.insert("payments", null, cv);
    }

    // READ (newest first)
    public List<Payment> getAllPayments() {
        List<Payment> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM payments ORDER BY id DESC", null);
        while (c.moveToNext()) {
            Payment p = new Payment();
            p.id = c.getInt(c.getColumnIndexOrThrow("id"));
            p.doctorName = c.getString(c.getColumnIndexOrThrow("doctor_name"));
            p.appointmentDate = c.getString(c.getColumnIndexOrThrow("appointment_date"));
            p.amount = c.getDouble(c.getColumnIndexOrThrow("amount"));
            p.method = c.getString(c.getColumnIndexOrThrow("method"));
            p.last4 = c.getString(c.getColumnIndexOrThrow("last4"));
            p.status = c.getString(c.getColumnIndexOrThrow("status"));
            p.paidAt = c.getString(c.getColumnIndexOrThrow("paid_at"));
            list.add(p);
        }
        c.close();
        return list;
    }
}