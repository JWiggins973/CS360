package com.zybooks.myapplication;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class login extends AppCompatActivity {
    private loginDatabase loginDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Initialize database
        loginDB = new loginDatabase(this);

        // Initialize views
        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);
        Button login = findViewById(R.id.login_button);
        Button register = findViewById(R.id.register_button);

        // Set click listener for login button
        login.setOnClickListener(v -> {
            String usernameText = username.getText().toString();
            String passwordText = password.getText().toString();

            // Check if username and password are valid
            if (loginDB.validateLogin(usernameText, passwordText)){
                // Login successful, navigate to main activity
                startActivity(new Intent(login.this, MainActivity.class));
                finish();
            }
            else {
                // Login failed, show error message
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                username.setError("Invalid username or password");
            }
        });

        // Set click listener for register button
        register.setOnClickListener(v -> {
            String usernameText = username.getText().toString();
            String passwordText = password.getText().toString();

            // Check if username and password are valid
            if (!loginDB.checkUsername(usernameText)){

                // Register new user
                loginDB.addUser(usernameText, passwordText);

                // Show success message
                Toast.makeText(this, "Registration successful please login", Toast.LENGTH_SHORT).show();
            }
            else {
                // Username already exists, show error message
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
            }

        });

    }
}