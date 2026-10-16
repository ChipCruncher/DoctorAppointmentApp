package com.example.doctorappointmentapp;

public class Appointment {
    private final int id;
    private final String hospitalName;
    private final String doctorName;
    private final String date;
    private final String time;
    private final String status;

    public Appointment(int id, String hospitalName, String doctorName,
                       String date, String time, String status) {
        this.id = id;
        this.hospitalName = hospitalName;
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
        this.status = status;
    }

    public int getId() {
        return id;
    }
    public String getHospitalName() {
        return hospitalName;
    }
    public String getDoctorName() {
        return doctorName;
    }
    public String getDate() {
        return date;
    }
    public String getTime() {
        return time;
    }
    public String getStatus() {
        return status;
    }
}