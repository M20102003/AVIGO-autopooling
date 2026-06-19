package com.auto.response_models;

public class SeatDataModel {

    private boolean seatBooked;
    private boolean seatSelected;
    private String seatName;

    public SeatDataModel(boolean seatBooked,boolean seatSelected, String seatName) {
        this.seatBooked = seatBooked;
        this.seatSelected = seatSelected;
        this.seatName = seatName;
    }


    public boolean isSeatSelected() {
        return seatSelected;
    }

    public void setSeatSelected(boolean seatSelected) {
        this.seatSelected = seatSelected;
    }

    public String getSeatName() {
        return seatName;
    }

    public void setSeatName(String seatName) {
        this.seatName = seatName;
    }

    public boolean isSeatBooked() {
        return seatBooked;
    }

    public void setSeatBooked(boolean seatBooked) {
        this.seatBooked = seatBooked;
    }
}
