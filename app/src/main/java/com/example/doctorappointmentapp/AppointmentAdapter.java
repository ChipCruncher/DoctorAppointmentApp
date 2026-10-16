package com.example.doctorappointmentapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// AI-assisted (Claude). Pattern from developer.android.com RecyclerView guide
public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {

    public interface OnCancelListener {
        void onCancel(Appointment appointment);
    }

    private final List<Appointment> appointments;
    private final OnCancelListener listener;

    public AppointmentAdapter(List<Appointment> appointments, OnCancelListener listener) {
        this.appointments = appointments;
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvDoctor, tvHospital, tvWhen, tvStatus;
        final Button btnCancel;

        ViewHolder(View itemView) {
            super(itemView);
            tvDoctor = itemView.findViewById(R.id.tvDoctor);
            tvHospital = itemView.findViewById(R.id.tvHospital);
            tvWhen = itemView.findViewById(R.id.tvWhen);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnCancel = itemView.findViewById(R.id.btnCancel);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment a = appointments.get(position);
        holder.tvDoctor.setText(a.getDoctorName());
        holder.tvHospital.setText(a.getHospitalName());
        holder.tvWhen.setText(a.getDate() + " at " + a.getTime());
        holder.tvStatus.setText("Status: " + a.getStatus());

        // Only an active booking can be cancelled
        boolean cancellable = "BOOKED".equals(a.getStatus());
        holder.btnCancel.setVisibility(cancellable ? View.VISIBLE : View.GONE);
        holder.btnCancel.setOnClickListener(v -> listener.onCancel(a));
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }
}