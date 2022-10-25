package com.roiquery.analytics.core

import android.os.SystemClock

class EventTimer(
    private var startTime: Long,
) {
    private var endTime: Long = 0
    private var eventAccumulatedDuration: Long = 0
    private var isPaused = false

    init {
        eventAccumulatedDuration = 0
        endTime = -1
    }

    fun duration(): Long {
        endTime = if (isPaused) {
            startTime
        } else {
            if (endTime < 0) SystemClock.elapsedRealtime() else endTime
        }
        return  endTime - startTime + eventAccumulatedDuration
    }

    fun getStartTime(): Long {
        return startTime
    }

    fun setStartTime(startTime: Long) {
        this.startTime = startTime
    }

    fun setEndTime(endTime: Long) {
        this.endTime = endTime
    }

    fun getEndTime(): Long {
        return endTime
    }

    fun setTimerState(isPaused: Boolean, elapsedRealtime: Long) {
        this.isPaused = isPaused
        if (isPaused) {
            eventAccumulatedDuration = eventAccumulatedDuration + elapsedRealtime - startTime
        }
        setStartTime(elapsedRealtime)
    }

    fun isPaused(): Boolean  = isPaused
}