package com.auto.pooling;

import static android.app.Activity.RESULT_OK;
import static androidx.core.app.ActivityCompat.finishAffinity;

import android.content.Intent;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.auto.pooling.databinding.FragmentProfilePageBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;


public class ProfilePageFragment extends Fragment {

    private FragmentProfilePageBinding binding;

    private ActivityResultLauncher<Intent> pickImageLauncher;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private GoogleSignInClient googleSignInClient;

    private boolean driver = false;
    private String documentUrl = "";
    private String numberPlate = "";

    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfilePageBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        db.collection("users").document("" + mAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    boolean isDriver = Boolean.TRUE.equals(documentSnapshot.getBoolean("driver"));
                    if(isDriver){
                        binding.driverPoolingsBtn.setVisibility(View.VISIBLE);
                        driver = true;
                        Map<String, Object> details = (Map<String, Object>) documentSnapshot.get("details");
                        if (details != null) {
                            documentUrl = ((String) details.get("documentUrl")); // Firestore stores numbers as Long
                            numberPlate = (String) details.get("numberPlate");
                        }
                        binding.registerBtnTxt.setText("Update Driver Details");
                    }
                    else{
                        binding.driverPoolingsBtn.setVisibility(View.GONE);
                        driver = false;
                        binding.registerBtnTxt.setText("Register As Driver");
                    }
                } else {
                    Toast.makeText(requireContext(),"User Not Exisit",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.nameTxt.setText(mAuth.getCurrentUser().getDisplayName());
        binding.emailTxt.setText(mAuth.getCurrentUser().getEmail());
        Glide.with(requireContext())
                .load(mAuth.getCurrentUser().getPhotoUrl())
                .placeholder(R.drawable.example_image)
                .error(R.drawable.example_image)
                .into(binding.userImage);

        binding.registerBtnTxt.setText("Register As Driver");

        binding.registerAsDriverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(requireContext(), RegisterDriverActivity.class);
                i.putExtra("documentUrl",documentUrl);
                i.putExtra("numberPlate",numberPlate);
                startActivity(i);
            }
        });
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri imageUri = result.getData().getData();
                            binding.progressBar.setVisibility(View.VISIBLE);
                            binding.userImage.setVisibility(View.GONE);
                            uploadImageToStorage(imageUri);
                            Toast.makeText(requireContext(),"Uploading",Toast.LENGTH_SHORT).show();
                        }

                        else{
                            binding.progressBar.setVisibility(View.GONE);
                            binding.userImage.setVisibility(View.VISIBLE);
                        }
                    }
                });

        binding.userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        binding.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "Hey! You can Create An Auto Pooling With Your Family and Friends. Download Our App Now. \n  http://play.google.com/store/apps/details?id=com.auto.pooling";
                onWhatsAppMessageButtonClicked(message);
            }
        });
        binding.driverPoolingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(requireContext(), DriverPoolingsActivity.class);
                startActivity(i);
            }
        });
        binding.logOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                googleSignInClient = GoogleSignIn.getClient(requireContext(), GoogleSignInOptions.DEFAULT_SIGN_IN);

                googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mAuth.signOut();
                            Intent i = new Intent(requireContext(), LoginActivity.class);
                            startActivity(i);
                            finishAffinity(requireActivity());
                        }
                    }
              });
            }
        });
    }


    private void onWhatsAppMessageButtonClicked(String message) {
        String sendUriString = "https://api.whatsapp.com/send?text=" + Uri.encode(message); // Use encode to ensure special characters are handled
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(sendUriString));
        startActivity(intent);
    }
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    // Upload image to Firebase Storage
    private void uploadImageToStorage(Uri imageUri) {
        if (imageUri != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference profilePicRef = storageRef.child("profile_pics/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");

            profilePicRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        profilePicRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            Toast.makeText(requireContext(),"Uploaded On FireBase",Toast.LENGTH_SHORT).show();
                            updateProfilePicture(uri.toString());
                        });
                    })
                    .addOnFailureListener(e -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.userImage.setVisibility(View.VISIBLE);
                        Toast.makeText(requireContext(),"Failed To Uploaded On FireBase",Toast.LENGTH_SHORT).show();
                        Toast.makeText(requireContext(),e.toString(),Toast.LENGTH_SHORT).show();
                        Log.e("ImageUpload", "Failed to upload image", e);
                    });
        }
    }

    private void updateProfilePicture(String photoUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(photoUrl))  // Set the new photo URL
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.userImage.setVisibility(View.VISIBLE);
                            Glide.with(requireContext())
                                    .load(photoUrl)
                                    .placeholder(R.drawable.example_image)
                                    .error(R.drawable.example_image)
                                    .into(binding.userImage);
                            Toast.makeText(requireContext(),"ProfilePic Updated Successfully",Toast.LENGTH_SHORT).show();
                            Log.d("ProfileUpdate", "User profile updated.");
                        } else {
                            binding.progressBar.setVisibility(View.GONE);
                            binding.userImage.setVisibility(View.VISIBLE);
                            Toast.makeText(requireContext(),"Error updating profile",Toast.LENGTH_SHORT).show();
                            Log.e("ProfileUpdate", "Error updating profile", task.getException());
                        }
                    });
        }
    }

}