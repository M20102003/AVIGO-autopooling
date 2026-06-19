package com.auto.pooling;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import com.auto.adapter.PoolingDataAdapter;
import com.auto.pooling.databinding.FragmentHomePageBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.auto.response_models.PoolingResponseModel;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomePageFragment extends Fragment {

    private FragmentHomePageBinding binding;

    ArrayList<PoolingResponseModel> newArrayList = new ArrayList<>();


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private PoolingDataAdapter poolingDataAdapter;

    private Date selectedDate;



    private ActivityResultLauncher<Intent> locationActivityLauncher;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding =  FragmentHomePageBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
//        fetchData();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        poolingDataAdapter = new PoolingDataAdapter(requireContext(),false,true,new ArrayList<>(),onlongClick -> {

        });
        binding.poolingListView.setAdapter(poolingDataAdapter);
        binding.userName.setText(mAuth.getCurrentUser().getDisplayName());
        Glide.with(requireContext())
                .load(mAuth.getCurrentUser().getPhotoUrl())
                .placeholder(R.drawable.example_image)
                .error(R.drawable.example_image)
                .into(binding.userImage);
        locationActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == 200 || result.getResultCode() == 201) {
                Intent data = result.getData();
                if (data != null) {
                    String location = data.getStringExtra("location");
                    if (result.getResultCode() == 200) {
                        if(binding.goingToTxt.getText().toString().trim().equalsIgnoreCase(location.toString().trim())){
                            Toast.makeText(requireContext(),"Leaving From And Going To Location Should Not Be Same",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            binding.leavingFromTxt.setText(location);
                        }
                    } else {
                        if(binding.leavingFromTxt.getText().toString().trim().equalsIgnoreCase(location.toString().trim())){
                            Toast.makeText(requireContext(),"Leaving From And Going To Location Should Not Be Same",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            binding.goingToTxt.setText(location);
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Service Not Available", Toast.LENGTH_SHORT).show();
            }
        });

        binding.leavingFromTxt.setOnClickListener(view1 -> {
            Intent intent = new Intent(requireContext(), LocationActivity.class);
            intent.putExtra("requestCode",200);
            locationActivityLauncher.launch(intent);
        });
        binding.goingToTxt.setOnClickListener(view2 -> {
            Intent intent = new Intent(requireContext(), LocationActivity.class);
            intent.putExtra("requestCode",201);
            locationActivityLauncher.launch(intent);
        });

        binding.btnSelectDate.setOnClickListener(v -> {
            // Get the current date and time
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            // Open DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (DatePicker view2, int selectedYear, int selectedMonth, int selectedDay) -> {
                // Open TimePickerDialog once date is selected
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0);
                selectedDate = selectedCalendar.getTime();
                binding.tvSelectedDate.setText(selectedDay + "-" + (selectedMonth + 1) + "-" + selectedYear);
            },
                    year,
                    month,
                    day
            );
            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());

            datePickerDialog.show();
        });

        ArrayList<String> items = new ArrayList<>();
        items.add("1 Passenger");
        items.add("2 Passengers");
        items.add("3 Pasengers");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item, items);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        binding.spinner.setAdapter(adapter);

        binding.searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.leavingFromTxt.getText().toString().trim().isEmpty()  || binding.goingToTxt.getText().toString().trim().isEmpty()){
                    Toast.makeText(requireContext(),"Missing Leaving From Or Going To Address",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(selectedDate == null){
                    Toast.makeText(requireContext(),"Date is Missing",Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(requireContext(),SearchPageActivity.class);
                intent.putExtra("leavingFrom",binding.leavingFromTxt.getText().toString().trim());
                intent.putExtra("goingTo",binding.goingToTxt.getText().toString().trim());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                intent.putExtra("date",sdf.format(selectedDate));
                double passengar = (binding.spinner.getSelectedItemPosition() + 1);
                intent.putExtra("passenger",passengar);
                startActivity(intent);
            }
        });
    }

    private void fetchData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.poolingListView.setVisibility(View.GONE);
        newArrayList.clear();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

// Convert to Date
        Date startOfToday = calendar.getTime();
        db.collection("poolings").whereGreaterThanOrEqualTo("date", startOfToday).orderBy("timestamp", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.poolingListView.setVisibility(View.VISIBLE);
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String poolingId = document.getId();
                        String driverName = document.getString("driverName");
                        String driverId = document.getString("driverId");
                        String imageUrl = document.getString("imageUrl");
                        Date date = document.getDate("date");
                        Double price = document.getDouble("price");
                        String leavingFrom = document.getString("leavingFrom");
                        String goingTo = document.getString("goingTo");
                        ArrayList<Double> bookedSeats = (ArrayList<Double>) document.get("bookedSeats");
                        PoolingResponseModel poolingModel = new PoolingResponseModel(poolingId,poolingId,"","","",imageUrl,driverName,driverId,price,"",date,leavingFrom,goingTo,bookedSeats,false);
                        newArrayList.add(poolingModel);
                    }
                    poolingDataAdapter.newData(newArrayList);
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.poolingListView.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(),"Error getting Documents",Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Error Value",e.toString());
            }
        });

        binding.searchEditTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().trim().isEmpty()){
                    poolingDataAdapter.newData(newArrayList);
                }
                else{
                    ArrayList<PoolingResponseModel> searchList = new ArrayList<>();
                    newArrayList.forEach((model) -> {
                        if(model.getLeavingFrom().trim().toLowerCase().contains(s.toString().trim().toLowerCase()) || model.getGoingTo().trim().toLowerCase().contains(s.toString().trim().toLowerCase())){
                            searchList.add(model);
                        }
                    });
                    poolingDataAdapter.newData(searchList);
                }
            }
        });
    }
}