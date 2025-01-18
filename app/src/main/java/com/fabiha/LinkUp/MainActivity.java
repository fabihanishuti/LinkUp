package com.fabiha.LinkUp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private RecyclerView mainUserRecyclerView;
    private UserAdpter adapter;
    private FirebaseDatabase database;
    private ArrayList<Users> usersArrayList;
    private ImageView imgLogout, camButton, settingsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Check if user is logged in
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        // Initialize Firebase database
        database = FirebaseDatabase.getInstance();

        // Initialize UI components
        mainUserRecyclerView = findViewById(R.id.mainUserRecyclerView);
        imgLogout = findViewById(R.id.logoutimg);
        camButton = findViewById(R.id.camBut);
        settingsButton = findViewById(R.id.settingBut);

        // Initialize RecyclerView and Adapter
        usersArrayList = new ArrayList<>();
        adapter = new UserAdpter(this, usersArrayList);
        mainUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mainUserRecyclerView.setAdapter(adapter);

        // Load users from Firebase database
        loadUsersFromDatabase();

        // Set up logout button
        imgLogout.setOnClickListener(v -> showLogoutDialog());

        // Set up settings button
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, setting.class);
            startActivity(intent);
        });

        // Set up camera button
        camButton.setOnClickListener(v -> openCamera());
    }

    private void loadUsersFromDatabase() {
        DatabaseReference reference = database.getReference().child("user");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersArrayList.clear(); // Avoid duplicates
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    if (user != null) {
                        usersArrayList.add(user);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Database Error: " + error.getMessage());
            }
        });
    }

    private void showLogoutDialog() {
        Dialog dialog = new Dialog(MainActivity.this, R.style.dialoge);
        dialog.setContentView(R.layout.dialog_layout);

        Button yesButton = dialog.findViewById(R.id.yesbnt);
        Button noButton = dialog.findViewById(R.id.nobnt);

        yesButton.setOnClickListener(v -> {
            auth.signOut();
            redirectToLogin();
        });

        noButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, login.class);
        startActivity(intent);
        finish();
    }

    private void openCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 10);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Log.e("CameraPermission", "Permission Denied");
            }
        }
    }
}
