package com.example.doctorappointmentapp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

// PAYMENT MODULE - written with help from Claude (Anthropic AI) as a starting point.
// Reviewed, understood and modified by: V.G.Orshahn  (acknowledged as required by the assignment)

// RecyclerView adapter: turns each Payment object into one row (item_payment.xml).
public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.VH> {

    private final List<Payment> items;

    public PaymentAdapter(List<Payment> items) { this.items = items; }

    static class VH extends RecyclerView.ViewHolder {
        TextView doctor, details, amount, status;
        VH(View v) {
            super(v);
            doctor = v.findViewById(R.id.tvItemDoctor);
            details = v.findViewById(R.id.tvItemDetails);
            amount = v.findViewById(R.id.tvItemAmount);
            status = v.findViewById(R.id.tvItemStatus);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_payment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Payment p = items.get(pos);
        h.doctor.setText(p.doctorName);
        String how = p.method.equals("CARD") ? "Card **** " + p.last4 : "Pay at clinic";
        h.details.setText(how + "  |  " + p.paidAt);
        h.amount.setText(String.format(Locale.US, "LKR %.2f", p.amount));
        h.status.setText(p.status);
        // Colour + text (not colour alone) so it is accessible
        h.status.setTextColor(p.status.equals("SUCCESS") ? Color.parseColor("#2E7D32") : Color.parseColor("#C62828"));
    }

    @Override
    public int getItemCount() { return items.size(); }
}
