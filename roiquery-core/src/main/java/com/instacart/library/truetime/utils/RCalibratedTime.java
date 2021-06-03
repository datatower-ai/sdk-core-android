package com.instacart.library.truetime.utils;

import android.os.SystemClock;



public final class RCalibratedTime implements ICalibratedTime {

    private final long startTime;
    private final long mSystemElapsedRealtime;

    public RCalibratedTime(long startTime) {
        this.startTime = startTime;
        mSystemElapsedRealtime = SystemClock.elapsedRealtime();
    }

    @Override
    public long get(long systemElapsedRealtime) {
        return systemElapsedRealtime - this.mSystemElapsedRealtime + startTime;
    }
}
