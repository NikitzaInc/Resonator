package com.resistorm.resonator.manager;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionsManager {
    private static final int PERMISSION_REQUEST_CODE = 200;
    private final Activity activity;

    public PermissionsManager(Activity activity) {
        this.activity = activity;
    }

    public boolean checkCameraAndStoragePermissions() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestCameraAndStoragePermissions() {
        ActivityCompat.requestPermissions(activity,
                new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_MEDIA_IMAGES
                },
                PERMISSION_REQUEST_CODE);
    }

    public void requestStoragePermission() {
        ActivityCompat.requestPermissions(activity,
                new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES
                },
                PERMISSION_REQUEST_CODE);
    }
}