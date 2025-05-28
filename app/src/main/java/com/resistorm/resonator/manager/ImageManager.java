package com.resistorm.resonator.manager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ImageManager {
    private static final int CAMERA_REQUEST = 100;
    private static final int GALLERY_REQUEST = 101;
    private final Activity activity;
    private Bitmap capturedImage;

    public ImageManager(Activity activity) {
        this.activity = activity;
    }

    public void openCamera() {
        try {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            if (cameraIntent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(activity, "No camera app found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(activity, "Camera error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void openGallery() {
        try {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK);
            galleryIntent.setType("image/*");
            activity.startActivityForResult(galleryIntent, GALLERY_REQUEST);
        } catch (Exception e) {
            Toast.makeText(activity, "Gallery error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CAMERA_REQUEST) {
                try {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        capturedImage = (Bitmap) extras.get("data");
                        return true;
                    }
                } catch (Exception e) {
                    Toast.makeText(activity, "Failed to get image", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == GALLERY_REQUEST) {
                try {
                    Uri selectedImageUri = data.getData();
                    InputStream imageStream = activity.getContentResolver().openInputStream(selectedImageUri);
                    capturedImage = BitmapFactory.decodeStream(imageStream);
                    return true;
                } catch (Exception e) {
                    Toast.makeText(activity, "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
        return false;
    }

    public Bitmap getCapturedImage() {
        return capturedImage;
    }

    public String bitmapToBase64() {
        if (capturedImage == null) return "";
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        capturedImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}