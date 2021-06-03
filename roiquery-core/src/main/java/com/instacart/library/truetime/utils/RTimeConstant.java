package com.instacart.library.truetime.utils;

/**
 * ITime with constant value.
 */
public class RTimeConstant implements ITime {

    private final String mTimeString;


    public RTimeConstant(String timeString) {
        mTimeString = timeString;

    }

    @Override
    public String getTime() {
        return mTimeString;
    }


}
