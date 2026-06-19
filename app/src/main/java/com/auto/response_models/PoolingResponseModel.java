package com.auto.response_models;

import java.util.ArrayList;
import java.util.Date;

public class PoolingResponseModel {

    public PoolingResponseModel(String docId,String poolingId,String userName,String userEmail,String userImage,String imageUrl,String driverName,String driverId,Double price,String rating, Date date, String leavingFrom, String goingTo,ArrayList<Double> bookedSeats,boolean canceled) {
        this.docId = docId;
        this.poolingId = poolingId;
        this.userName = userName;
        this.userEmail =userEmail;
        this.userImage = userImage;
        this.imageUrl = imageUrl;
        this.driverName = driverName;
        this.driverId = driverId;
        this.price = price;
        this.rating = rating;
        this.date = date;
        this.leavingFrom = leavingFrom;
        this.goingTo = goingTo;
        this.bookedSeats = bookedSeats;
        this.canceled = canceled;
    }

    public boolean isCanceled() {
        return canceled;
    }

    final boolean canceled;

    private final String docId;

    public String getDriverId() {
        return driverId;
    }

    private  final  String driverId;

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserImage() {
        return userImage;
    }

    private final String userName;
    private final String userEmail;
    private final String userImage;

    public String getPoolingId() {
        return poolingId;
    }

    public void setPoolingId(String poolingId) {
        this.poolingId = poolingId;
    }

    private String poolingId;

    private ArrayList<Double> bookedSeats;
    private String driverName;

    private String imageUrl;

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    private Double price;

    private String rating;

    private Date date;

    private String leavingFrom;

    private String goingTo;


    public String getDocId() {
        return docId;
    }


    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }


    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLeavingFrom() {
        return leavingFrom;
    }

    public void setLeavingFrom(String leavingFrom) {
        this.leavingFrom = leavingFrom;
    }

    public String getGoingTo() {
        return goingTo;
    }

    public void setGoingTo(String goingTo) {
        this.goingTo = goingTo;
    }

    public ArrayList<Double> getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(ArrayList<Double> bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Constructor







}

