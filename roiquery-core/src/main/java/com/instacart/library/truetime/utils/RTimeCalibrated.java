package com.instacart.library.truetime.utils;

import android.os.SystemClock;




public class RTimeCalibrated implements ITime {

    private long mSystemElapsedRealtime;
    private ICalibratedTime mCalibratedTime;

    private long mDate;

    public RTimeCalibrated(ICalibratedTime calibratedTime) {
        mCalibratedTime = calibratedTime;
        mSystemElapsedRealtime = SystemClock.elapsedRealtime();
    }

    private synchronized long getDate() {
        if (0 == mDate) {
            mDate = mCalibratedTime.get(mSystemElapsedRealtime);
        }
        return mDate;
    }

    @Override
    public String getTime() {
        try {
            return String.valueOf(getDate());
        } catch (Exception e) {
            e.printStackTrace();
            return String.valueOf(System.currentTimeMillis());
        }
    }

}
