package com.zybooks.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Adapter class for displaying low stock inventory items in a RecyclerView
public class LowStockAdapter extends RecyclerView.Adapter<LowStockAdapter.ViewHolder> {

    // List holding the inventory items to display
    private List<InventoryItem> items = new ArrayList<>();

    // Constructor
    public LowStockAdapter() {
        this.items = new ArrayList<>();
    }

    // Updates the adapter with a new list of inventory items
    public void setItems(List<InventoryItem> newItems) {
        this.items = Objects.requireNonNullElseGet(newItems, ArrayList::new);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    // Called when RecyclerView needs a new ViewHolder
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_notification_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    // Binds data to the ViewHolder at the specified position
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItem item = items.get(position);
        holder.name.setText(item.getName());
        holder.message.setText("Low inventory!");
    }

    @Override
    // Returns the total number of items in the data set
    public int getItemCount() {
        return items.size();
    }


    // ViewHolder class holds references to the views for each item
    static class ViewHolder extends RecyclerView.ViewHolder {
        // TextView for item name
        TextView name;
        // TextView for notification message
        TextView message;

        // Constructor initializes the TextViews from the itemView
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.itemName);
            message = itemView.findViewById(R.id.notification_message);
        }
    }
}