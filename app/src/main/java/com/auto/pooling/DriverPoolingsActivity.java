package com.auto.pooling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.auto.adapter.PoolingDataAdapter;
import com.auto.pooling.databinding.ActivityDriverPoolingsBinding;
import com.auto.response_models.PoolingResponseModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DriverPoolingsActivity extends AppCompatActivity {

    private ActivityDriverPoolingsBinding binding;



    ArrayList<PoolingResponseModel> newArrayList = new ArrayList<>();


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private PoolingDataAdapter poolingDataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriverPoolingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        poolingDataAdapter = new PoolingDataAdapter(DriverPoolingsActivity.this,true,true,new ArrayList<>(), onlongClick -> {

        });
        binding.poolingListView.setAdapter(poolingDataAdapter);
        fetchData();
    }

    private void fetchData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.poolingListView.setVisibility(View.GONE);
        newArrayList.clear();
        db.collection("poolings").whereEqualTo("driverId",mAuth.getCurrentUser().getUid()).orderBy("timestamp", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.poolingListView.setVisibility(View.VISIBLE);
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String docId = document.getId();
                        String driverName = document.getString("driverName");
                        String driverId = document.getString("driverId");
                        String imageUrl = document.getString("imageUrl");
                        Date date = document.getDate("date");
                        Double price = document.getDouble("price");
                        String leavingFrom = document.getString("leavingFrom");
                        String goingTo = document.getString("goingTo");
                        ArrayList<Double> bookedSeats = (ArrayList<Double>) document.get("bookedSeats");
                        PoolingResponseModel poolingModel = new PoolingResponseModel(docId,docId,"","","",imageUrl,driverName,driverId,price,"",date,leavingFrom,goingTo,bookedSeats,false);
                        newArrayList.add(poolingModel);
                    }
                    poolingDataAdapter.newData(newArrayList);
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.poolingListView.setVisibility(View.VISIBLE);
                    Toast.makeText(DriverPoolingsActivity.this,"Error getting Documents",Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Error Vacsd",e.toString());
                Toast.makeText(DriverPoolingsActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
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