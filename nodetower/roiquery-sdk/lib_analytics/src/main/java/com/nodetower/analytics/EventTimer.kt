package com.nodetower.analytics

import android.os.SystemClock
import com.nodetower.base.utils.LogUtils
import java.util.*
import java.util.concurrent.TimeUnit


class EventTimer(
    private var timeUnit: TimeUnit,
    var startTime: Long) {
    var endTime: Long = -1
    var eventAccumulatedDuration: Long = 0
    var isPaused = false
        private set

    fun duration(): String {
        endTime = if (isPaused) {
            startTime
        } else {
            if (endTime < 0) SystemClock.elapsedRealtime() else endTime
        }
        val duration = endTime - startTime + eventAccumulatedDuration
        return try {
            if (duration < 0 || duration > 24 * 60 * 60 * 1000) {
                return 0.toString()
            }
            val durationFloat: Float
            durationFloat = if (timeUnit === TimeUnit.MILLISECONDS) {
                duration.toFloat()
            } else if (timeUnit === TimeUnit.SECONDS) {
                duration / 1000.0f
            } else if (timeUnit === TimeUnit.MINUTES) {
                duration / 1000.0f / 60.0f
            } else if (timeUnit === TimeUnit.HOURS) {
                duration / 1000.0f / 60.0f / 60.0f
            } else {
                duration.toFloat()
            }
            if (durationFloat < 0) 0.toString() else java.lang.String.format(
                Locale.CHINA,
                "%.3f",
                durationFloat
            )
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
            0.toString()
        }
    }

    fun setTimerState(isPaused: Boolean, elapsedRealtime: Long) {
        this.isPaused = isPaused
        if (isPaused) {
            eventAccumulatedDuration = eventAccumulatedDuration + elapsedRealtime - startTime
        }
        startTime = elapsedRealtime
    }

}
