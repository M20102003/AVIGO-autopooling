package com.auto.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.auto.pooling.CreateBookingActivity;
import com.auto.pooling.R;
import com.auto.pooling.databinding.AutoDetailsAdapterBinding;
import com.auto.pooling.databinding.SeatAdapterBinding;
import com.auto.response_models.PoolingResponseModel;
import com.auto.response_models.SeatDataModel;
import com.google.gson.Gson;

import java.util.ArrayList;


public class SelectingSeatAdapter extends RecyclerView.Adapter<SelectingSeatAdapter.Holder> {

    private Context context;
    public ArrayList<SeatDataModel> seatList;
    private OnLongClickListener onLongClick;



    public interface OnLongClickListener {
        void onLongClick(PoolingResponseModel commentModel);
    }

    public SelectingSeatAdapter(Context context, ArrayList<SeatDataModel> seatList, OnLongClickListener onLongClick) {
        this.context = context;
        this.seatList = seatList;
        this.onLongClick = onLongClick;
    }

    public static class Holder extends RecyclerView.ViewHolder {
        SeatAdapterBinding binding;

        public Holder(@NonNull SeatAdapterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SeatAdapterBinding binding = SeatAdapterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new Holder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, @SuppressLint("RecyclerView") int position) {
        SeatDataModel seatData = seatList.get(position);
        if(seatData.isSeatBooked()){
            holder.binding.selectingView.setBackgroundResource(R.drawable.booked_bg);
            holder.binding.seatNameTxt.setTextColor(ContextCompat.getColor(context, R.color.gray));
            holder.binding.seatImage.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.gray)));
        }
        else{
            if(seatData.isSeatSelected()){
                holder.binding.selectingView.setBackgroundResource(R.drawable.selected_bg);
                holder.binding.seatNameTxt.setTextColor(ContextCompat.getColor(context, R.color.white));
                holder.binding.seatImage.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white)));
            }else{
                holder.binding.selectingView.setBackgroundResource(R.drawable.white_bg);
                holder.binding.seatNameTxt.setTextColor(ContextCompat.getColor(context, R.color.primary));
                holder.binding.seatImage.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary)));
            }
        }
        holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(seatData.isSeatBooked()){
                    Toast.makeText(context,"Seat Already Booked Please Try An Other Seat",Toast.LENGTH_SHORT).show();
                }
                else{
                    changeData(position, new SeatDataModel(seatData.isSeatBooked(),!seatData.isSeatSelected(),seatData.getSeatName()));
                }
            }
        });
        holder.binding.seatNameTxt.setText(seatData.getSeatName());
    }

    @Override
    public int getItemCount() {
        return seatList != null ? seatList.size() : 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void newData(ArrayList<SeatDataModel> newData) {
        this.seatList = newData;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void changeData(int index,SeatDataModel newData) {
        seatList.set(index, newData);
        notifyItemChanged(index);
    }


}
