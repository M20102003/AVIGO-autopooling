package com.auto.pooling;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.auto.pooling.databinding.ActivitySuccessPageBinding;

public class SuccessPage extends AppCompatActivity {

    private ActivitySuccessPageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuccessPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        String leavingFrom = intent.getStringExtra("leavingFrom");
        String goingTo = intent.getStringExtra("goingTo");
        binding.leavingFromTxt.setText(leavingFrom);
        binding.goingToTxt.setText(goingTo);
        binding.homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}