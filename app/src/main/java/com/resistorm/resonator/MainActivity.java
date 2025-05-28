package com.resistorm.resonator;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.resistorm.resonator.manager.ImageManager;
import com.resistorm.resonator.manager.NetworkManager;
import com.resistorm.resonator.manager.PermissionsManager;
import com.resistorm.resonator.manager.ResistorDisplayManager;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;

import java.util.Date;

import androidx.appcompat.app.ActionBar;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private Button btnCapture, btnSelectFromGallery, btnUpload;
    private TextView tvStatus;

    private PermissionsManager permissionsManager;
    private ImageManager imageManager;
    private NetworkManager networkManager;
    private ResistorDisplayManager resistorDisplayManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeManagers();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        setupButtonListeners();
    }

    private void initializeViews() {
        imageView = findViewById(R.id.imageView);
        btnCapture = findViewById(R.id.btnCapture);
        btnSelectFromGallery = findViewById(R.id.btnSelectFromGallery);
        btnUpload = findViewById(R.id.btnUpload);
        tvStatus = findViewById(R.id.tvStatus);
    }

    private void initializeManagers() {
        permissionsManager = new PermissionsManager(this);
        imageManager = new ImageManager(this);
        networkManager = new NetworkManager(this, getString(R.string.serverip));
        resistorDisplayManager = new ResistorDisplayManager(this);
    }

    private void setupButtonListeners() {
        btnCapture.setOnClickListener(v -> {
            if (permissionsManager.checkCameraAndStoragePermissions()) {
                imageManager.openCamera();
            } else {
                permissionsManager.requestCameraAndStoragePermissions();
            }
        });

        btnSelectFromGallery.setOnClickListener(v -> {
            if (permissionsManager.checkStoragePermission()) {
                imageManager.openGallery();
            } else {
                permissionsManager.requestStoragePermission();
            }
        });

        btnUpload.setOnClickListener(v -> {
            if (imageManager.getCapturedImage() != null) {
                uploadImage();
            } else {
                Toast.makeText(MainActivity.this, "Please select an image first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (imageManager.handleActivityResult(requestCode, resultCode, data)) {
            imageView.setImageBitmap(imageManager.getCapturedImage());
        }
    }

    private void uploadImage() {
        if (!networkManager.isNetworkAvailable()) {
            tvStatus.setText("No internet connection available");
            resistorDisplayManager.hideResistorViews();
            return;
        }

        String base64Image = imageManager.bitmapToBase64();
        String timestamp = new Date().toString();

        networkManager.uploadImage(base64Image, timestamp,
                response -> handleUploadResponse(response),
                error -> handleUploadError(error));
    }

    private void handleUploadResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            if (jsonResponse.getString("status").equals("success")) {
                tvStatus.setText("Color bands detected successfully");
                resistorDisplayManager.displayResistorData(jsonResponse);
            } else {
                String errorMsg = jsonResponse.optString("message", "Detection failed");
                tvStatus.setText("Error: " + errorMsg);
                resistorDisplayManager.hideResistorViews();
            }
        } catch (JSONException e) {
            tvStatus.setText("Error parsing server response");
            resistorDisplayManager.hideResistorViews();
        }
    }

    private void handleUploadError(VolleyError error) {
        String errorMessage;
        if (error instanceof TimeoutError) {
            errorMessage = "Connection timeout - server not responding";
        } else if (error instanceof NoConnectionError) {
            errorMessage = "No network connection";
        } else if (error instanceof AuthFailureError) {
            errorMessage = "Authentication failed";
        } else if (error instanceof ServerError) {
            errorMessage = "Server error occurred";
        } else if (error instanceof NetworkError) {
            errorMessage = "Network error - check your connection";
        } else if (error instanceof ParseError) {
            errorMessage = "Error parsing server response";
        } else {
            errorMessage = error.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = "Failed to connect to server";
            }
        }
        tvStatus.setText("Upload failed: " + errorMessage);
        resistorDisplayManager.hideResistorViews();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (imageManager.getCapturedImage() != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            imageManager.getCapturedImage().compress(Bitmap.CompressFormat.PNG, 100, stream);
            outState.putByteArray("capturedImage", stream.toByteArray());
        }
    }
}