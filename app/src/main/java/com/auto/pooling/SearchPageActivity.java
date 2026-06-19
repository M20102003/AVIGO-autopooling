package com.auto.pooling;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.auto.adapter.PoolingDataAdapter;
import com.auto.pooling.databinding.ActivitySearchPageBinding;
import com.auto.response_models.PoolingResponseModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SearchPageActivity extends AppCompatActivity {
    private com.auto.pooling.databinding.ActivitySearchPageBinding binding;

    private PoolingDataAdapter poolingDataAdapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        poolingDataAdapter = new PoolingDataAdapter(SearchPageActivity.this,false,true,new ArrayList<>(), onlongClick -> {

        });
        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.poolingListView.setAdapter(poolingDataAdapter);
        Intent intent = getIntent();
        String leavingFrom = intent.getStringExtra("leavingFrom");
        String goingTo =intent.getStringExtra("goingTo");

        String dateString =intent.getStringExtra("date");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date receivedDate = null;
        try {
            receivedDate = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        double passengers = intent.getDoubleExtra("passenger",1.0);
        fetchData(leavingFrom,goingTo,receivedDate,passengers);
    }

    private void fetchData(String leavingFrom,String goingTo,Date receivedDate,Double passengers) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.poolingListView.setVisibility(View.GONE);

        Calendar calendarStart = Calendar.getInstance();
        calendarStart.setTime(receivedDate);
        calendarStart.set(Calendar.HOUR_OF_DAY, 0);
        calendarStart.set(Calendar.MINUTE, 0);
        calendarStart.set(Calendar.SECOND, 0);
        calendarStart.set(Calendar.MILLISECOND, 0);
        Date startDate = calendarStart.getTime();

        // End of the day (23:59:59.999)
        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.setTime(receivedDate);
        calendarEnd.set(Calendar.HOUR_OF_DAY, 23);
        calendarEnd.set(Calendar.MINUTE, 59);
        calendarEnd.set(Calendar.SECOND, 59);
        calendarEnd.set(Calendar.MILLISECOND, 999);
        Date endDate = calendarEnd.getTime();


        db.collection("poolings").whereEqualTo("leavingFrom",leavingFrom.trim()).whereEqualTo("goingTo", goingTo.trim()).whereGreaterThanOrEqualTo("date",startDate).whereLessThanOrEqualTo("date",endDate).orderBy("date", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.poolingListView.setVisibility(View.VISIBLE);
                    ArrayList<PoolingResponseModel> newArrayList = new ArrayList<>();

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
                        if(3-bookedSeats.size() >= (passengers.intValue())){
                            newArrayList.add(poolingModel);
                        }

                    }
                    if(newArrayList.size() == 0){
                        binding.poolingListView.setVisibility(View.GONE);
                        binding.emptyDataImage.setVisibility(View.VISIBLE);
                    }
                    else{
                        binding.poolingListView.setVisibility(View.VISIBLE);
                        binding.emptyDataImage.setVisibility(View.GONE);
                    }
                    poolingDataAdapter.newData(newArrayList);
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.poolingListView.setVisibility(View.VISIBLE);
                    Toast.makeText(SearchPageActivity.this,"Error getting Documents",Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                binding.progressBar.setVisibility(View.GONE);
                binding.poolingListView.setVisibility(View.VISIBLE);
                Log.d("Error Value",e.toString());
                Toast.makeText(SearchPageActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}