package com.auto.pooling;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.auto.adapter.PoolingDataAdapter;
import com.auto.pooling.databinding.FragmentBookingsBinding;
import com.auto.response_models.PoolingResponseModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;


public class BookingsFragment extends Fragment {


    private FragmentBookingsBinding binding;

    ArrayList<PoolingResponseModel> newArrayList = new ArrayList<>();


    FirebaseFirestore db = FirebaseFirestore.getInstance();

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private PoolingDataAdapter poolingDataAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookingsBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        fetchData();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        poolingDataAdapter = new PoolingDataAdapter(requireContext(),false,false,new ArrayList<>(), onlongClick -> {

        });
        binding.bookingListView.setAdapter(poolingDataAdapter);
    }

    private void fetchData() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.bookingListView.setVisibility(View.GONE);
        newArrayList.clear();
        db.collection("orders").whereEqualTo("uid",mAuth.getCurrentUser().getUid()).orderBy("timestamp", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.bookingListView.setVisibility(View.VISIBLE);
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String docId = document.getId();
                        String userName = document.getString("userName");
                        String userEmail = document.getString("userEmail");
                        String userImage = document.getString("userImage");
                        String poolingId = document.getString("poolingId");
                        String driverName = document.getString("driverName");
                        String driverId = document.getString("driverId");
                        String imageUrl = document.getString("imageUrl");
                        Double price = document.getDouble("price");
                        Date date = document.getDate("date");
                        String leavingFrom = document.getString("leavingFrom");
                        String goingTo = document.getString("goingTo");
                        boolean canceled = false;
                        if(document.getBoolean("canceled") != null) {
                            canceled = document.getBoolean("canceled");
                        }else{
                            canceled = false;
                        };
                        ArrayList<Double> bookedSeats = (ArrayList<Double>) document.get("seats");
                        PoolingResponseModel poolingModel = new PoolingResponseModel(docId,poolingId,userName,userEmail,userImage,imageUrl,driverName,driverId,price,"",date,leavingFrom,goingTo,bookedSeats,canceled);
                        newArrayList.add(poolingModel);
                    }
                    poolingDataAdapter.newData(newArrayList);
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.bookingListView.setVisibility(View.VISIBLE);
                    Log.d("Value",task.getException().toString());
                    Toast.makeText(requireContext(),"Error getting Documents" + task.getException(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}