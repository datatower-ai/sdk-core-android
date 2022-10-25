package com.roiquery.analytics.core

import com.roiquery.analytics.Constant
import com.roiquery.analytics.utils.LogUtils

class EventTimerManager {

    companion object {
        private const val TAG = Constant.LOG_TAG
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            EventTimerManager()
        }
    }

    private var mTrackTimer: MutableMap<String, EventTimer> = HashMap()


    fun addEventTimer(eventName: String, eventTimer: EventTimer) {
        synchronized(mTrackTimer) {
            // remind：update startTime before runnable queue
            mTrackTimer[eventName] = eventTimer
            LogUtils.d(TAG, "$eventName start timer")
        }
    }

    fun removeTimer(eventName: String) {
        synchronized(mTrackTimer) { mTrackTimer.remove(eventName) }
    }

    fun updateEndTime(eventName: String, endTime: Long) {
        synchronized(mTrackTimer) {
            mTrackTimer[eventName]?.setEndTime(endTime)
            LogUtils.d(TAG,"$eventName end timer")
        }
    }

    fun updateTimerState(eventName: String, startTime: Long, isPause: Boolean) {
        try {
            synchronized(mTrackTimer) {
                val eventTimer: EventTimer? = mTrackTimer[eventName]
                if (eventTimer != null && eventTimer.isPaused() !== isPause) {
                    eventTimer.setTimerState(isPause, startTime)
                    LogUtils.d(TAG,"$eventName update Timer State, isPause :$isPause")
                }
            }
        } catch (e: Exception) {

        }
    }

    fun getEventTimer(eventName: String): EventTimer? {
        var eventTimer: EventTimer?
        synchronized(mTrackTimer) {
            eventTimer = mTrackTimer[eventName]
            mTrackTimer.remove(eventName)
        }
        return eventTimer
    }

    fun clearTimers() {
        try {
            synchronized(mTrackTimer) { mTrackTimer.clear() }
        } catch (e: Exception) {
        }
    }
}