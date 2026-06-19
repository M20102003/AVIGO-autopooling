package com.auto.pooling;

import static com.auto.extensions.extension.generateRandomName;
import static com.auto.extensions.extension.getImageUrl;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.auto.adapter.SelectingSeatAdapter;
import com.auto.pooling.databinding.FragmentCreatePoolingBinding;
import com.auto.response_models.SeatDataModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class CreatePoolingFragment extends Fragment {


     private FragmentCreatePoolingBinding _binding;
    private Date selectedDate;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    List<Double> bookingPassenger = new ArrayList<>();

    private boolean isDriver = false;

    private ActivityResultLauncher<Intent> locationActivityLauncher;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        _binding = FragmentCreatePoolingBinding.inflate(getLayoutInflater());
        return _binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        db.collection("users").document("" + mAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            isDriver = Boolean.TRUE.equals(documentSnapshot.getBoolean("driver"));
                            if(isDriver){
                                _binding.createPoolingView.setVisibility(View.VISIBLE);
                                _binding.registerAsDriverView.setVisibility(View.GONE);
                            }
                            else{
                                _binding.createPoolingView.setVisibility(View.GONE);
                                _binding.registerAsDriverView.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(requireContext(), "No user document found for ID", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        _binding.priceEditTxt.setText("");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        locationActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == 200 || result.getResultCode() == 201) {
                Intent data = result.getData();
                if (data != null) {
                    String location = data.getStringExtra("location");
                    if (result.getResultCode() == 200) {
                        if(_binding.goingToTxt.getText().toString().trim().equalsIgnoreCase(location.toString().trim())){
                            Toast.makeText(requireContext(),"Leaving From And Going To Location Should Not Be Same",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            _binding.leavingFromTxt.setText(location);
                        }
                    } else {
                        if(_binding.leavingFromTxt.getText().toString().trim().equalsIgnoreCase(location.toString().trim())){
                            Toast.makeText(requireContext(),"Leaving From And Going To Location Should Not Be Same",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            _binding.goingToTxt.setText(location);
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Service Not Available", Toast.LENGTH_SHORT).show();
            }
        });



//        _binding.goingToTxt.setOnTouchListener((v, event) -> {
//            v.performClick();
//            return true;
//        });
        _binding.btnSelectDate.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);


            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (DatePicker view2, int selectedYear, int selectedMonth, int selectedDay) -> {

                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        requireContext(),
                        (TimePicker timeView, int selectedHour, int selectedMinute) -> {
                            Calendar selectedCalendar = Calendar.getInstance();
                            selectedCalendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, 0);
                            if (selectedCalendar.before(calendar)) {
                                Toast.makeText(requireContext(), "Past dates and times are not allowed.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            selectedDate = selectedCalendar.getTime();
                            _binding.tvSelectedDate.setText(selectedDay + "-" + (selectedMonth + 1) + "-" + selectedYear + " " + selectedHour + ":" + selectedMinute);
                        },
                        hour,
                        minute,
                        true
                );
                timePickerDialog.show();
            },
                    year,
                    month,
                    day
            );
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

            datePickerDialog.show();
        });


        _binding.createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _binding.progressBar.setVisibility(View.VISIBLE);
                _binding.createBtn.setVisibility(View.GONE);
                if(_binding.leavingFromTxt.getText().toString().trim().isEmpty()  || _binding.goingToTxt.getText().toString().trim().isEmpty()){
                    Toast.makeText(requireContext(),"Missing Leaving From Or Going To Address",Toast.LENGTH_SHORT).show();
                    _binding.progressBar.setVisibility(View.GONE);
                    _binding.createBtn.setVisibility(View.VISIBLE);
                    return;
                }
                else if(selectedDate == null){
                    Toast.makeText(requireContext(),"Date is Missing",Toast.LENGTH_SHORT).show();
                    _binding.progressBar.setVisibility(View.GONE);
                    _binding.createBtn.setVisibility(View.VISIBLE);
                    return;
                }
                else if(_binding.priceEditTxt.getText().toString().trim().isEmpty()){
                    Toast.makeText(requireContext(),"Enter Price",Toast.LENGTH_SHORT).show();
                    _binding.progressBar.setVisibility(View.GONE);
                    _binding.createBtn.setVisibility(View.VISIBLE);
                    return;
                }

                if(isDriver){
                    Map<String, Object> poolingData = new HashMap<>();
                    poolingData.put("timestamp", FieldValue.serverTimestamp());
                    poolingData.put("driverName",mAuth.getCurrentUser().getDisplayName());
                    poolingData.put("driverId",mAuth.getCurrentUser().getUid());
                    poolingData.put("imageUrl",mAuth.getCurrentUser().getPhotoUrl());
                    poolingData.put("date", selectedDate);
                    poolingData.put("leavingFrom", _binding.leavingFromTxt.getText().toString().trim());
                    poolingData.put("goingTo", _binding.goingToTxt.getText().toString().trim());
                    poolingData.put("bookedSeats",bookingPassenger);
                    poolingData.put("price",Double.parseDouble(_binding.priceEditTxt.getText().toString()));
                    db.collection("poolings")
                            .add(poolingData)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    _binding.progressBar.setVisibility(View.GONE);
                                    _binding.createBtn.setVisibility(View.VISIBLE);
                                    Toast.makeText(requireContext(),"Data Added Successfully",Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(requireContext(), SuccessPage.class);
                                    intent.putExtra("leavingFrom",_binding.leavingFromTxt.getText().toString().trim());
                                    intent.putExtra("goingTo",_binding.goingToTxt.getText().toString().trim());
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    _binding.progressBar.setVisibility(View.GONE);
                                    _binding.createBtn.setVisibility(View.VISIBLE);
                                    Toast.makeText(requireContext(),e.toString(),Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else{
                    Toast.makeText(requireContext(), "Only Driver Can Create Pooling", Toast.LENGTH_SHORT).show();
                    _binding.progressBar.setVisibility(View.GONE);
                    _binding.createBtn.setVisibility(View.VISIBLE);
                }
            }
        });

        _binding.registerAsDriverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              Intent intent = new Intent(requireContext(),RegisterDriverActivity.class);
              startActivity(intent);
            }
        });

        _binding.leavingFromTxt.setOnClickListener(view1 -> {
            Intent intent = new Intent(requireContext(), LocationActivity.class);
            intent.putExtra("requestCode",200);
            locationActivityLauncher.launch(intent);
        });
        _binding.goingToTxt.setOnClickListener(view2 -> {
            Intent intent = new Intent(requireContext(), LocationActivity.class);
            intent.putExtra("requestCode",201);
            locationActivityLauncher.launch(intent);
        });
    }

}