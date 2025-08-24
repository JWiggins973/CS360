package com.zybooks.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private InventoryDatabase inventoryDB;
    private InventoryDao inventoryDao;
    private inventoryAdapter adapter;
    private Executor dbExecutor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the inventory database and DAO
        inventoryDB = InventoryDatabase.getInstance(this);
        inventoryDao = inventoryDB.inventoryDao();
        this.dbExecutor = Executors.newSingleThreadExecutor();

        // set up the recycler view
        setupRecyclerView();

        // observe the inventory data
        observeInventoryData();

        // observe low stock data and send SMS notifications if enabled
        observeLowStockData();

        // set up notification button
        ImageButton notificationButton = findViewById(R.id.notifications);
        notificationButton.setOnClickListener(v -> startActivity(new Intent(this, notifications.class)));

        // set up fab button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showAddItemDialog());
    }


    // set up the recycler view
    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        // set up the adapter
        adapter = new inventoryAdapter(inventoryDao);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    // observe the inventory data
    private void observeInventoryData() {
        // get data to display
        inventoryDao.getAllItemsLiveData().observe(this, inventoryItems -> {
            if (inventoryItems != null) {
                adapter.updateInventoryList(inventoryItems);

                // test data
                if (inventoryItems.isEmpty() ) {
                    // if no data, add some dummy data
                    dbExecutor.execute(() -> {
                        inventoryDao.insertItem(new InventoryItem("Apples", 10));
                        inventoryDao.insertItem(new InventoryItem("Bananas", 5));
                        inventoryDao.insertItem(new InventoryItem("Oranges", 8));
                    });
                }
            }
        });

    }

    // observe low stock data and send SMS notifications if enabled
    private void observeLowStockData() {
        // get data to display
        inventoryDao.getLowInventoryItems().observe(this, this::sendSmsNotifications);
    }

    // load notification state from shared preferences
    private boolean loadNotificationState() {
        return getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("notificationsEnabled", false);
    }

    // send SMS notifications for low stock items
    private void sendSmsNotifications(List<InventoryItem> lowStockItems) {
        // check if notifications are enabled and if there are any low stock items
        boolean notificationsEnabled = loadNotificationState();
        if (!notificationsEnabled || lowStockItems == null || lowStockItems.isEmpty()) {
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            String phoneNumber = "5554"; // emulator number for testing

            for (InventoryItem item : lowStockItems) {
                String message = "⚠️ Low Stock Alert: " + item.getName() +
                        " has only " + item.getQuantity() + " remaining!";

                try {
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                    Toast.makeText(this, "SMS sent for " + item.getName(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to send SMS for " + item.getName() +
                            ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "SMS service error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // show add item dialog
    private void showAddItemDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // add input fields for item name and quantity
        EditText nameInput = new EditText(this);
        nameInput.setHint("Item Name");
        layout.addView(nameInput);

        EditText quantityInput = new EditText(this);
        quantityInput.setHint("Quantity");
        layout.addView(quantityInput);

        // create and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Item");
        builder.setView(layout);

        builder.setPositiveButton("Add", null);
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();

        // OVERRIDES SO DIALOG STAYS OPEN UNTIL USER CLICKS OK AND INPUT VALID
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            String quantityStr = quantityInput.getText().toString();
            // validate inputs
            if (!validInput(name, quantityStr, nameInput, quantityInput)) {
                return;
            }

                dbExecutor.execute(() -> {
                    InventoryItem existingItem = inventoryDao.getItemByName(name);

                    runOnUiThread(() -> {
                        if (existingItem != null) {
                                nameInput.setError("Item name already exists");
                                Toast.makeText(this, "Item name already exists", Toast.LENGTH_SHORT).show();
                            }
                        else {
                            // if name doesn't exist, add item to database
                            dbExecutor.execute(() -> inventoryDao.insertItem(new InventoryItem(name, Integer.parseInt(quantityStr))));
                            dialog.dismiss(); // dismiss the dialog
                        }
                    });
                });
            });
    }

    // validate inputs
    private boolean validInput(String name, String quantityStr, EditText nameInput, EditText quantityInput) {
        // Check if the item name is valid
        if (name.isEmpty()) {
            nameInput.setError("Item name cannot be empty");
            Toast.makeText(this, "Item name cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if the quantity is valid
        if (quantityStr.isEmpty()) {
            quantityInput.setError("Quantity cannot be empty");
            Toast.makeText(this, "Quantity cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            quantityInput.setError("Invalid quantity");
            Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (quantity <= 0) {
            quantityInput.setError("Quantity must be a positive number");
            Toast.makeText(this, "Quantity must be a positive number", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to the app
        dbExecutor.execute(() -> {
            List<InventoryItem> currentItems = inventoryDao.getAllItems();
            runOnUiThread(() -> {
                adapter.updateInventoryList(currentItems);
            });
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (inventoryDB != null)
            inventoryDB.close();
    }
}