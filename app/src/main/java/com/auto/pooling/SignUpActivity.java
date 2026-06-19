package com.auto.pooling;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.auto.pooling.databinding.ActivitySignUpBinding;
import com.auto.pooling.databinding.DialogBoxBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private GoogleSignInClient mGoogleSignInClient;
    ProgressBar pd;
    private ActivityResultLauncher<Intent> googleSignInLauncher;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        pd = binding.progressBar;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("310427835343-t2ra624m1isl5fthe6nbj16a6sc8o1gk.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        binding.googleBtn.setOnClickListener(view -> signIn());

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account.getIdToken());
                        } catch (ApiException e) {
                            Toast.makeText(getApplicationContext(), "Failed: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Sign-In canceled or failed", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        binding.signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.signUpProgressBar.setVisibility(View.VISIBLE);
                binding.signUpBtn.setVisibility(View.GONE);
                if(!binding.emailEdtTxt.getText().toString().isEmpty() && binding.emailEdtTxt.getText().toString().contains("@gmail.com") && !binding.passwordEdtTxt.getText().toString().isEmpty()){
                    mAuth.createUserWithEmailAndPassword(binding.emailEdtTxt.getText().toString(), binding.passwordEdtTxt.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    db = FirebaseFirestore.getInstance();
                                    Map<String, Object> users = new HashMap<>();
                                    users.put("uid", user.getUid() + "");
                                    users.put("name", binding.nameEdtTxt.getText().toString());
                                    users.put("email", binding.emailEdtTxt.getText().toString());
                                    users.put("driver", false);

                                    // Update the FirebaseAuth display name
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(binding.nameEdtTxt.getText().toString()) // Set the display name to the name entered
                                            .build();

                                    user.updateProfile(profileUpdates)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        db.collection("users").document(user.getUid()).set(users, SetOptions.merge())
                                                                .addOnSuccessListener(a -> {
                                                                    binding.signUpProgressBar.setVisibility(View.GONE);
                                                                    binding.signUpBtn.setVisibility(View.VISIBLE);
                                                                    Toast.makeText(getApplicationContext(), "User Created Successfully. Please Sign In and Continue", Toast.LENGTH_SHORT).show();
                                                                    startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                                                                    finishAffinity();
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    Toast.makeText(getApplicationContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                                    binding.signUpProgressBar.setVisibility(View.GONE);
                                                                    binding.signUpBtn.setVisibility(View.VISIBLE);
                                                                });
                                                    } else {
                                                        // Handle the case when profile update fails
                                                        binding.signUpProgressBar.setVisibility(View.GONE);
                                                        binding.signUpBtn.setVisibility(View.VISIBLE);
                                                        Toast.makeText(getApplicationContext(), "Failed to update profile: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    binding.signUpProgressBar.setVisibility(View.GONE);
                                    binding.signUpBtn.setVisibility(View.VISIBLE);
                                    Toast.makeText(SignUpActivity.this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(SignUpActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else if(binding.nameEdtTxt.getText().toString().isEmpty()){
                    Toast.makeText(SignUpActivity.this,"Enter Your Name",Toast.LENGTH_SHORT).show();
                    binding.signUpProgressBar.setVisibility(View.GONE);
                    binding.signUpBtn.setVisibility(View.VISIBLE);

                }
                else if(binding.emailEdtTxt.getText().toString().isEmpty() || !binding.emailEdtTxt.getText().toString().contains("@gmail.com")){
                    Toast.makeText(SignUpActivity.this,"Enter Your Email Correctly",Toast.LENGTH_SHORT).show();
                    binding.signUpProgressBar.setVisibility(View.GONE);
                    binding.signUpBtn.setVisibility(View.VISIBLE);
                }
                else{
                    Toast.makeText(SignUpActivity.this,"Enter Password",Toast.LENGTH_SHORT).show();
                    binding.signUpProgressBar.setVisibility(View.GONE);
                    binding.signUpBtn.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        binding.googleBtn.setVisibility(View.GONE);
        pd.setVisibility(View.VISIBLE);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            try {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    db = FirebaseFirestore.getInstance();
                    db.collection("users").document("" + user.getUid()).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                pd.setVisibility(View.GONE);
                                binding.googleBtn.setVisibility(View.VISIBLE);
                                if (documentSnapshot.exists()) {
                                    startActivity(new Intent(SignUpActivity.this, MainPageActivity.class));
                                    finishAffinity();
                                } else {
                                    // Document does not exist, add a new document
                                    Map<String, Object> users = new HashMap<>();
                                    users.put("uid", user.getUid());
                                    users.put("name", user.getDisplayName());
                                    users.put("email", user.getEmail());
                                    boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                                    if (isNewUser) {
                                        users.put("driver", false);
                                    }

                                    db.collection("users").document(user.getUid()).set(users,SetOptions.merge())
                                            .addOnSuccessListener(a -> {
                                                pd.setVisibility(View.GONE);
                                                binding.googleBtn.setVisibility(View.VISIBLE);
                                                Toast.makeText(getApplicationContext(), "User Created Successfully", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(SignUpActivity.this, MainPageActivity.class));
                                                finishAffinity();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(getApplicationContext(), "failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                pd.setVisibility(View.GONE);
                                                binding.googleBtn.setVisibility(View.VISIBLE);
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getApplicationContext(), "failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                pd.setVisibility(View.GONE);
                                binding.googleBtn.setVisibility(View.VISIBLE);
                            });
                } else {
                    Toast.makeText(getApplicationContext(), "Failed " + task.getException(), Toast.LENGTH_SHORT).show();
                    pd.setVisibility(View.INVISIBLE);
                    binding.googleBtn.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                Toast.makeText(this, "Exception " + e, Toast.LENGTH_SHORT).show();
                pd.setVisibility(View.INVISIBLE);
            }

        });


    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }
}