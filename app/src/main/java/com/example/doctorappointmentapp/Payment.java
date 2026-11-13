package com.example.doctorappointmentapp;

// PAYMENT MODULE - written with help from Claude (Anthropic AI) as a starting point.
// Reviewed, understood and modified by: V.G.Orshahn  (acknowledged as required by the assignment)

// Simple model class: one row of the "payments" table.
public class Payment {
    public int id;
    public String doctorName;
    public String appointmentDate;
    public double amount;
    public String method;   // "CARD" or "CASH"
    public String last4;    // only last 4 digits are stored, NEVER the full card number or CVV
    public String status;   // "SUCCESS" or "FAILED"
    public String paidAt;   // timestamp
}
