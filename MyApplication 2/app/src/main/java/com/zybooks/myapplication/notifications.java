package com.zybooks.myapplication;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

// Activity to handle SMS notifications for low stock inventory items
public class notifications extends AppCompatActivity {

    private static final int SMS_PERMISSION_REQUEST_CODE = 100;
    private boolean notificationsEnabled = false;
    private LowStockAdapter lowStockAdapter;
    private List<InventoryItem> currentLowStockItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        // Load notification state from shared preferences
        notificationsEnabled = loadNotificationState();


        // Initialize InventoryDao to access inventory data
        InventoryDao inventoryDao = InventoryDatabase.getInstance(getApplicationContext()).inventoryDao();

        // Setup RecyclerView to display low stock items
        setupRecyclerView();

        // Observe LiveData for low stock inventory items
        observeLowStockData(inventoryDao);


        // Setup button to toggle SMS notification permission
        Button toggleButton = findViewById(R.id.request_sms_permission_button);
        updateButtonText(toggleButton);

        toggleButton.setOnClickListener(v -> toggleNotifications(toggleButton));
    }

    // set up recycler view
    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_notification_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        lowStockAdapter = new LowStockAdapter();
        recyclerView.setAdapter(lowStockAdapter);
    }

    //observe low stock inventory data
    private void observeLowStockData(InventoryDao inventoryDao) {
        inventoryDao.getLowInventoryItems().observe(this, lowStockItems -> {
            currentLowStockItems = lowStockItems;
            lowStockAdapter.setItems(lowStockItems);
        });
    }

    // toggle button to enable/disable notifications
    private void toggleNotifications(Button toggleButton) {
        if (!notificationsEnabled) {
            // Show dialog to request SMS permission to enable notifications
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Enable SMS Notifications")
                    .setMessage("Allow SMS permission to receive low stock alerts?")
                    .setPositiveButton("Allow", (dialog, which) -> requestSmsPermission())
                    .setNegativeButton("Cancel", (dialog, which) ->
                            Toast.makeText(this, "SMS notifications remain disabled", Toast.LENGTH_SHORT).show())
                    .show();
        }
        else {
            // Disable notifications if already enabled
            notificationsEnabled = false;
            saveNotificationState(false);
            updateButtonText(toggleButton);
            Toast.makeText(this, "SMS notifications disabled", Toast.LENGTH_SHORT).show();
        }
    }

    // Request SMS permission from the user
    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, enable notifications
            enableNotifications();
        }
    }

    // Enable SMS notifications and update UI accordingly
    private void enableNotifications() {
        notificationsEnabled = true;
        saveNotificationState(true);
        Button toggleButton = findViewById(R.id.request_sms_permission_button);
        updateButtonText(toggleButton);
        Toast.makeText(this, "SMS notifications enabled!", Toast.LENGTH_SHORT).show();

        // Send notifications for current low stock items immediately
        if (currentLowStockItems != null && !currentLowStockItems.isEmpty()) {
            lowStockAdapter.setItems(currentLowStockItems);
        }
    }

    // Update the button text based on notification state
    private void updateButtonText(Button button) {
        if (notificationsEnabled) {
            button.setText("Disable SMS Notifications");
        } else {
            button.setText("Enable SMS Notifications");
        }
    }

    // Handle the result of SMS permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable notifications
                enableNotifications();
            } else {
                // Permission denied, keep notifications disabled and update UI
                Toast.makeText(this, "SMS permission denied. Notifications remain disabled.", Toast.LENGTH_SHORT).show();
                Button toggleButton = findViewById(R.id.request_sms_permission_button);
                updateButtonText(toggleButton);
            }
        }
    }

    private void saveNotificationState(boolean enabled) {
        getSharedPreferences("app_prefs", MODE_PRIVATE)
                .edit()
                .putBoolean("notificationsEnabled", enabled)
                .apply();
    }
    private boolean loadNotificationState() {
        return getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getBoolean("notificationsEnabled", false);
    }


}