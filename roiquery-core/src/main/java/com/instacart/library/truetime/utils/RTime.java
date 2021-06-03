package com.instacart.library.truetime.utils;



public class RTime implements ITime {

    private final String mDate;

    public RTime() {
        mDate = String.valueOf(System.currentTimeMillis());
    }

    public RTime(String date) {
        mDate = date;
    }
    @Override
    public String getTime() {
        try {
            return mDate;
        } catch (Exception e) {
            e.printStackTrace();
            return String.valueOf(System.currentTimeMillis());
        }
    }
}
