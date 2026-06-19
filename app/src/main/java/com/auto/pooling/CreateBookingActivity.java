package com.auto.pooling;

import static com.auto.extensions.extension.getDateTimeFromDate;
import static com.auto.extensions.extension.getDateTimeWithExtraHour;
import static com.auto.extensions.extension.getTimeFromDate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.auto.adapter.SelectingSeatAdapter;
import com.auto.pooling.databinding.ActivityCreateBookingBinding;
import com.auto.response_models.PoolingResponseModel;
import com.auto.response_models.SeatDataModel;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateBookingActivity extends AppCompatActivity {
    private ActivityCreateBookingBinding binding;
    List<Double> bookingPassenger = new ArrayList<>();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    String url1 = "https://androbim-d97d9.el.r.appspot.com/rikshaw/sendNotificaton";
    String url2 = "https://androbim-d97d9.el.r.appspot.com/rikshaw/createNotificationSchedular";


    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private SelectingSeatAdapter selectingSeatAdapter;

    PoolingResponseModel poolingData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Gson gson = new Gson();
        Intent intent = getIntent();
        String poolingModel = intent.getStringExtra("poolingModel");
        String position = intent.getStringExtra("position");
        boolean fromHome = intent.getBooleanExtra("fromHome",true);
         poolingData = gson.fromJson(poolingModel, PoolingResponseModel.class);
        ArrayList<SeatDataModel> list = new ArrayList<>();
        List<Double> bookedSeats = poolingData.getBookedSeats();

        if(fromHome){
            binding.canceledView.setVisibility(View.GONE);
            db.collection("poolings").document(poolingData.getDocId()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot document) {
                    String poolingId = document.getId();
                    String driverName = document.getString("driverName");
                    String driverId = document.getString("driverId");
                    String imageUrl = document.getString("imageUrl");
                    Date date = document.getDate("date");
                    Double price = document.getDouble("price");
                    String leavingFrom = document.getString("leavingFrom");
                    String goingTo = document.getString("goingTo");
                    ArrayList<Double> bookedSeats = (ArrayList<Double>) document.get("bookedSeats");
                    poolingData = new PoolingResponseModel(poolingId,poolingId,"","","",imageUrl,driverName,driverId,price,"",date,leavingFrom,goingTo,bookedSeats,false);
                    list.clear();
                    list.add(new SeatDataModel(bookedSeats.contains(1.0),false,"1"));
                    list.add(new SeatDataModel(bookedSeats.contains(2.0),false,"2"));
                    list.add(new SeatDataModel(bookedSeats.contains(3.0),false,"3"));
                    binding.userDetailsView.setVisibility(View.GONE);
                    binding.cancelBookingView.setVisibility(View.GONE);
                    binding.confirmButtonView.setVisibility(View.VISIBLE);
                    selectingSeatAdapter = new SelectingSeatAdapter(CreateBookingActivity.this,list,onlongClick ->{});
                    binding.seatRecyclerList.setAdapter(selectingSeatAdapter);
                }
            });
         }
        else{
            list.clear();
            bookedSeats.forEach((seatNumber) -> {
                list.add(new SeatDataModel(true, false, String.valueOf(((Number) seatNumber).intValue())));
            });
            binding.userDetailsView.setVisibility(View.VISIBLE);
            binding.cancelBookingView.setVisibility(View.VISIBLE);
            binding.confirmButtonView.setVisibility(View.GONE);
            if(poolingData.isCanceled()){
                binding.canceledView.setVisibility(View.VISIBLE);
                binding.cancelBookingView.setVisibility(View.GONE);
            }
            else{
                binding.canceledView.setVisibility(View.GONE);
                binding.cancelBookingView.setVisibility(View.VISIBLE);
            }
            String userImageUrl = "https://api.dicebear.com/9.x/avataaars/png?seed="+position;
            if(poolingData.getImageUrl() !=null){
                userImageUrl = poolingData.getUserImage();
            }
            Glide.with(CreateBookingActivity.this)
                    .load(userImageUrl)
                    .placeholder(R.drawable.example_image)
                    .error(R.drawable.example_image)
                    .into(binding.userImage);
            binding.userName.setText(poolingData.getUserName());
            selectingSeatAdapter = new SelectingSeatAdapter(CreateBookingActivity.this,list,onlongClick ->{});
            binding.seatRecyclerList.setAdapter(selectingSeatAdapter);
        }


        String imageUrl = "https://api.dicebear.com/9.x/avataaars/png?seed="+position;
        if(poolingData.getImageUrl() !=null){
            imageUrl = poolingData.getImageUrl();
        }
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.example_image)
                .error(R.drawable.example_image)
                .into(binding.autoDriverImage);


        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
        binding.autoDriverName.setText(poolingData.getDriverName());
        binding.leavingFromTxt.setText(poolingData.getLeavingFrom());
        binding.goingToTxt.setText(poolingData.getGoingTo());
        binding.bookingDateAndTimeTxt.setText(getDateTimeFromDate(poolingData.getDate()));
        binding.startTime.setText(getTimeFromDate(poolingData.getDate()));
        binding.endTime.setText(getDateTimeWithExtraHour(poolingData.getDate()));
        binding.priceTxt.setText("₹"+String.valueOf(poolingData.getPrice()));


        binding.confirmBookingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.confirmBookingBtn.setVisibility(View.GONE);
                bookingPassenger.clear();
                selectingSeatAdapter.seatList.forEach((data) ->{
                    if(data.isSeatSelected()){
                        bookingPassenger.add(Double.parseDouble(data.getSeatName()));
                    }
                });
                if(bookingPassenger.size() == 0) {
                    Toast.makeText(CreateBookingActivity.this,"Select Any Seat",Toast.LENGTH_SHORT).show();
                    binding.progressBar.setVisibility(View.GONE);
                    binding.confirmBookingBtn.setVisibility(View.VISIBLE);
                    return;
                }
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("timestamp", FieldValue.serverTimestamp());
                orderData.put("userImage", mAuth.getCurrentUser().getPhotoUrl());
                orderData.put("userName", mAuth.getCurrentUser().getDisplayName());
                orderData.put("userEmail", mAuth.getCurrentUser().getEmail());
                orderData.put("poolingId", poolingData.getDocId());
                orderData.put("imageUrl", poolingData.getImageUrl());
                orderData.put("driverName", poolingData.getDriverName());
                orderData.put("driverId", poolingData.getDriverId());
                orderData.put("date", poolingData.getDate());
                orderData.put("leavingFrom", poolingData.getLeavingFrom());
                orderData.put("goingTo", poolingData.getGoingTo());
                orderData.put("seats", bookingPassenger);
                orderData.put("price",poolingData.getPrice());
                orderData.put("uid",mAuth.getCurrentUser().getUid());

                db.collection("orders")
                        .add(orderData)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                bookingPassenger.addAll(bookedSeats);
                                db.collection("poolings").document(poolingData.getDocId())
                                        .update("bookedSeats",bookingPassenger)
                                        .addOnSuccessListener(aVoid -> {
                                            JSONObject jsonBody = new JSONObject();
                                            JSONObject jsonBody2 = new JSONObject();

                                                // Add data to the JSON body
                                            try {
                                                jsonBody.put("userId", poolingData.getDriverId());
                                                jsonBody.put("title", "New Ride");
                                                jsonBody.put("description", "A New Ride Is Booked By "+mAuth.getCurrentUser().getDisplayName());
                                                confirmBtn(url1, jsonBody);
                                                jsonBody2.put("userId",mAuth.getCurrentUser().getUid());
                                                jsonBody.put("duration",poolingData.getDate());
                                                jsonBody2.put("bookingId",documentReference.getId());
                                                confirmBtn(url2, jsonBody2);
                                            } catch (JSONException e) {
                                               Log.d("","");
                                            }

                                            binding.progressBar.setVisibility(View.GONE);
                                            binding.confirmBookingBtn.setVisibility(View.VISIBLE);
                                            Toast.makeText(CreateBookingActivity.this,"Data Added Successfully",Toast.LENGTH_SHORT).show();
                                            Intent resultIntent = new Intent();
                                            setResult(RESULT_OK, resultIntent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            binding.progressBar.setVisibility(View.GONE);
                                            binding.confirmBookingBtn.setVisibility(View.VISIBLE);
                                            Toast.makeText(CreateBookingActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                binding.progressBar.setVisibility(View.GONE);
                                binding.confirmBookingBtn.setVisibility(View.VISIBLE);
                                Toast.makeText(CreateBookingActivity.this,e.toString(),Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        binding.cancelBookingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.cancelProgressBar.setVisibility(View.VISIBLE);
                binding.cancelBookingBtn.setVisibility(View.GONE);
                WriteBatch batch = db.batch();

                DocumentReference orderDocRef = db.collection("orders").document(poolingData.getDocId());

                batch.update(orderDocRef, "canceled", true);

                DocumentReference poolingDocRef = db.collection("poolings").document(poolingData.getPoolingId());

                batch.update(poolingDocRef, "bookedSeats", FieldValue.arrayRemove(bookedSeats.toArray()));

                batch.commit().addOnSuccessListener(aVoid -> {
                    binding.cancelProgressBar.setVisibility(View.GONE);
                    binding.cancelBookingBtn.setVisibility(View.VISIBLE);
                    Toast.makeText(CreateBookingActivity.this,"Booking Canceled Successfully",Toast.LENGTH_SHORT).show();
                    finish();
                }).addOnFailureListener(e -> {
                    binding.cancelProgressBar.setVisibility(View.GONE);
                    binding.cancelBookingBtn.setVisibility(View.VISIBLE);
                    Toast.makeText(CreateBookingActivity.this,"Error "+e,Toast.LENGTH_SHORT).show();
                    Log.d("Batch update failed: ", e.getMessage());
                });
            }
        });
    }

    private void confirmBtn(String url, JSONObject jsonBody) {

        RequestQueue requestQueue = Volley.newRequestQueue(this);


        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {

                   },
                error -> {

                         }) {


            @Override
            public byte[] getBody() {
                return jsonBody.toString().getBytes();
            }


            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };


        requestQueue.add(stringRequest);
    }

}