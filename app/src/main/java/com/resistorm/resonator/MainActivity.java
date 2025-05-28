package com.resistorm.resonator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_REQUEST = 100;
    private static final int GALLERY_REQUEST = 101;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private ImageView imageView;
    private Button btnCapture, btnSelectFromGallery, btnUpload;
    private TextView tvStatus;
    private Bitmap capturedImage;
    private String serverUrl = "http://192.168.0.2:8080/upload";// 0 = none, 1 = camera, 2 = gallery

    private static final Map<String, Integer> COLOR_MAP = new HashMap<String, Integer>() {{
        put("Black", Color.BLACK);
        put("Brown", Color.parseColor("#A52A2A"));
        put("Red", Color.RED);
        put("Orange", Color.parseColor("#FFA500"));
        put("Yellow", Color.YELLOW);
        put("Green", Color.GREEN);
        put("Blue", Color.BLUE);
        put("Violet", Color.parseColor("#8A2BE2"));
        put("Gray", Color.GRAY);
        put("White", Color.WHITE);
        put("Gold", Color.parseColor("#FFD700"));
        put("Silver", Color.parseColor("#C0C0C0"));
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        btnCapture = findViewById(R.id.btnCapture);
        btnSelectFromGallery = findViewById(R.id.btnSelectFromGallery);
        btnUpload = findViewById(R.id.btnUpload);
        tvStatus = findViewById(R.id.tvStatus);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        if (savedInstanceState != null) {
            byte[] byteArray = savedInstanceState.getByteArray("capturedImage");
            if (byteArray != null) {
                capturedImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                imageView.setImageBitmap(capturedImage);
            }
        }

        btnCapture.setOnClickListener(v -> {
            if (checkPermissions()) {
                openCamera();
            } else {
                requestPermissions();
            }
        });

        btnSelectFromGallery.setOnClickListener(v -> {
            if (checkStoragePermission()) {
                openGallery();
            } else {
                requestStoragePermission();
            }
        });

        btnUpload.setOnClickListener(v -> {
            if (capturedImage != null) {
                uploadImage();
            } else {
                Toast.makeText(MainActivity.this, "Please select an image first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openCamera() {
        try {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Camera error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        try {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, GALLERY_REQUEST);
        } catch (Exception e) {
            Toast.makeText(this, "Gallery error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                try {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        capturedImage = (Bitmap) extras.get("data");
                        imageView.setImageBitmap(capturedImage);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == GALLERY_REQUEST) {
                try {
                    Uri selectedImageUri = data.getData();
                    InputStream imageStream = getContentResolver().openInputStream(selectedImageUri);
                    capturedImage = BitmapFactory.decodeStream(imageStream);
                    imageView.setImageBitmap(capturedImage);
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_MEDIA_IMAGES
                },
                PERMISSION_REQUEST_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES
                },
                PERMISSION_REQUEST_CODE);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void uploadImage() {
        if (!isNetworkAvailable()) {
            tvStatus.setText("No internet connection available");
            hideResistorViews();
            return;
        }

        String base64Image = bitmapToBase64(capturedImage);
        String timestamp = new Date().toString();

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("image", base64Image);
            jsonBody.put("timestamp", timestamp);

            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, serverUrl,
                    response -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getString("status").equals("success")) {
                                tvStatus.setText("Color bands detected successfully");
                                displayResistorData(jsonResponse);
                            } else {
                                String errorMsg = jsonResponse.optString("message", "Detection failed");
                                tvStatus.setText("Error: " + errorMsg);
                                hideResistorViews();
                            }
                        } catch (JSONException e) {
                            tvStatus.setText("Error parsing server response");
                            hideResistorViews();
                        }
                    },
                    error -> {
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
                        hideResistorViews();
                    }) {

                @Override
                public byte[] getBody() {
                    return jsonBody.toString().getBytes();
                }

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
            };

            queue.add(stringRequest);
        } catch (JSONException e) {
            tvStatus.setText("Error creating JSON: " + e.getMessage());
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (capturedImage != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            capturedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
            outState.putByteArray("capturedImage", stream.toByteArray());
        }
    }

    private void displayResistorData(JSONObject response) throws JSONException {
        TextView tvResistanceValue = findViewById(R.id.tvResistanceValue);
        LinearLayout bandContainer = findViewById(R.id.resistorBandContainer);

        bandContainer.removeViews(1, bandContainer.getChildCount() - 1);

        String resistance = response.getString("value");
        tvResistanceValue.setText("Resistance: " + resistance + " Ω");
        tvResistanceValue.setVisibility(View.VISIBLE);

        JSONArray colors = response.getJSONArray("colors");
        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < colors.length(); i++) {
            String colorName = colors.getString(i);
            View band = inflater.inflate(R.layout.resistor_band, bandContainer, false);
            View bandView = band.findViewById(R.id.resistorBand);

            Integer color = COLOR_MAP.get(colorName);
            if (color != null) {
                bandView.setBackgroundColor(color);
            } else {
                bandView.setBackgroundColor(Color.BLACK);
            }

            bandContainer.addView(band);
        }

        bandContainer.setVisibility(View.VISIBLE);
    }

    private void hideResistorViews() {
        findViewById(R.id.tvResistanceValue).setVisibility(View.GONE);
        findViewById(R.id.resistorBandContainer).setVisibility(View.GONE);
    }
}