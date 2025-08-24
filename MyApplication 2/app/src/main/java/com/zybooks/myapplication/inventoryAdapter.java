package com.zybooks.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// Adapter class for the RecyclerView to display and manage inventory items
public class inventoryAdapter extends RecyclerView.Adapter<inventoryAdapter.InventoryViewHolder> {

    // List holding the inventory items to display
    private List<InventoryItem> mInventoryList;
    // Data Access Object for performing database operations on inventory items
    private InventoryDao inventoryDao;
    private final Executor dbExecutor;

    // Constructor to initialize the adapter with the inventory list and DAO
    public inventoryAdapter(InventoryDao inventoryDao) {
        this.mInventoryList = new ArrayList<>();
        this.inventoryDao = inventoryDao;
        this.dbExecutor = Executors.newSingleThreadExecutor();
    }

    @NonNull
    @Override
    // Inflate the item layout and create a ViewHolder object
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.activity_item_list, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    // Bind data to the ViewHolder views for the given position
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem item = mInventoryList.get(position);
        // Set item name and quantity text views
        holder.itemNameTextView.setText(item.getName());
        holder.itemQuantityTextView.setText(String.valueOf(item.getQuantity()));

        // Set click listener for the edit button to allow editing or deleting the item
        holder.editButton.setOnClickListener(v -> {
            // Create an AlertDialog for editing the quantity or deleting the item
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle("Edit Quantity for " + item.getName());

            // Create an EditText input field for entering the new quantity
            final EditText input = new EditText(v.getContext());
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setText(String.valueOf(item.getQuantity()));
            builder.setView(input);

            // Positive button to save the new quantity
            builder.setPositiveButton("Save", (dialog, which) -> {
                String newQuantityStr = input.getText().toString();
                if (!newQuantityStr.isEmpty()) {
                    int newQuantity = Integer.parseInt(newQuantityStr);
                    item.setQuantity(newQuantity);

                    // Perform database update on a background thread to avoid blocking UI
                    dbExecutor.execute(() -> {
                        inventoryDao.updateItem(item);
                    });

                }
            });
            // Negative button to delete the item from the list and database
            builder.setNegativeButton("Delete item", (dialog, which) -> {
                // Perform database deletion on a background thread
                dbExecutor.execute(() -> {
                    inventoryDao.deleteItem(item);
                });
            });

            // Show the AlertDialog
            builder.show();
        });
    }

    @Override
    // Return the total number of items in the inventory list
    public int getItemCount() {
        return mInventoryList.size();
    }


    // Method to update the entire list when LiveData changes
    public void updateInventoryList(List<InventoryItem> newInventoryList) {
        this.mInventoryList = newInventoryList;
        notifyDataSetChanged();
    }


    // ViewHolder class to hold references to the views for each inventory item
    static class InventoryViewHolder extends RecyclerView.ViewHolder {

        TextView itemNameTextView;
        TextView itemQuantityTextView;
        Button editButton;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views from the item layout
            itemNameTextView = itemView.findViewById(R.id.itemName);
            itemQuantityTextView = itemView.findViewById(R.id.itemQuantity);
            editButton = itemView.findViewById(R.id.itemEdit);
        }
    }
}
