package com.auto.pooling;

import static com.auto.extensions.extension.validateNumberPlate;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.auto.pooling.databinding.ActivityRegisterDriverBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class RegisterDriverActivity extends AppCompatActivity {

    private ActivityRegisterDriverBinding binding;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private ActivityResultLauncher<Intent> pickImageLauncher;

    private Uri imageUri;

    private String documentUrl;

    private String numberPlate;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterDriverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);

        Intent intent = getIntent();
        documentUrl = intent.getStringExtra("documentUrl");
        numberPlate = intent.getStringExtra("numberPlate");

        binding.numberPlateTxt.setText(numberPlate);
        Glide.with(RegisterDriverActivity.this)
                .load(documentUrl)
                .into(binding.fileImage);

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            documentUrl = "";
                            binding.fileImage.setImageURI(imageUri);
                        } else {
                            Toast.makeText(RegisterDriverActivity.this, "Choose Correct File", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        binding.uploadFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        binding.locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchLocation();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.submitBtn.setVisibility(View.GONE);

                if ((imageUri != null || !documentUrl.isEmpty()) && !binding.numberPlateTxt.getText().toString().trim().isEmpty() &&
                        validateNumberPlate(binding.numberPlateTxt.getText().toString().trim())) {
                    if (documentUrl.isEmpty() && imageUri != null) {
                        uploadImageToStorage(imageUri, binding.numberPlateTxt.getText().toString().trim());
                    } else {
                        updateDriverDetails(documentUrl, binding.numberPlateTxt.getText().toString().trim());
                    }
                } else if (imageUri == null && documentUrl.isEmpty()) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.submitBtn.setVisibility(View.VISIBLE);
                    Toast.makeText(RegisterDriverActivity.this, "Licence Should Be Uploaded", Toast.LENGTH_SHORT).show();
                } else if (!validateNumberPlate(binding.numberPlateTxt.getText().toString().trim()) ||
                        binding.numberPlateTxt.getText().toString().trim().isEmpty()) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.submitBtn.setVisibility(View.VISIBLE);
                    Toast.makeText(RegisterDriverActivity.this, "Enter Registered Number Plate Correctly", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLocations().size() > 0) {
                    Location location = locationResult.getLastLocation();
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    binding.locationTxt.setText("Latitude: " + latitude + ", Longitude: " + longitude);
                    fusedLocationClient.removeLocationUpdates(this);
                }
            }
        }, Looper.getMainLooper());
    }

    private void uploadImageToStorage(Uri imageUri, String numberPlate) {
        if (imageUri != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference profilePicRef = storageRef.child("licence/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");

            profilePicRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            Toast.makeText(RegisterDriverActivity.this, "Uploaded On FireBase", Toast.LENGTH_SHORT).show();
                            updateDriverDetails(uri.toString(), numberPlate);
                        });
                    })
                    .addOnFailureListener(e -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.submitBtn.setVisibility(View.VISIBLE);
                        Toast.makeText(RegisterDriverActivity.this, "Failed To Upload On FireBase", Toast.LENGTH_SHORT).show();
                        Log.e("ImageUpload", "Failed to upload image", e);
                    });
        }
    }

    private void updateDriverDetails(String documentUrl, String numberPlate) {
        Map<String, Object> details = new HashMap<>();
        details.put("documentUrl", documentUrl);
        details.put("numberPlate", numberPlate);
        details.put("latitude", latitude);
        details.put("longitude", longitude);

        db.collection("users").document(auth.getCurrentUser().getUid()).update(
                "driver", true,
                "details", details
        ).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                binding.progressBar.setVisibility(View.GONE);
                binding.submitBtn.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "Updated As Driver", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}