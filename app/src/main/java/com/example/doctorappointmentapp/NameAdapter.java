package com.example.doctorappointmentapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// AI-assisted (Claude). Pattern from developer.android.com RecyclerView guide
public class NameAdapter extends RecyclerView.Adapter<NameAdapter.ViewHolder> {

    public interface OnNameClickListener {
        void onNameClick(String name);
    }

    private final List<String> names;
    private final OnNameClickListener listener;

    public NameAdapter(List<String> names, OnNameClickListener listener) {
        this.names = names;
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = names.get(position);
        holder.tvName.setText(name);
        holder.itemView.setOnClickListener(v -> listener.onNameClick(name));
    }

    @Override
    public int getItemCount() {
        return names.size();
    }
}