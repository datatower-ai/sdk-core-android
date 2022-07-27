package com.roiquery.analytics.utils

import android.os.SystemClock
import com.roiquery.analytics.Constant
import com.roiquery.analytics.network.HttpCallback
import com.roiquery.analytics.network.HttpMethod
import com.roiquery.analytics.network.RequestHelper

/**
 * author: xiaosailing
 * date: 2022-06-23
 * description:
 * version：1.0
 */
class TimeCalibration private constructor() {

    companion object {
        private const val TIME_FROM_ROI_NET_BODY = "[{}]"
        const val TIME_NOT_VERIFY_VALUE = 0L
        val instance: TimeCalibration by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            TimeCalibration()
        }
    }

    private var _latestTime = TIME_NOT_VERIFY_VALUE
    private var _latestSystemElapsedRealtime = 0L
    fun getReferenceTime() {
        if (_latestTime == 0L) {
            RequestHelper.Builder(HttpMethod.POST_ASYNC, Constant.EVENT_REPORT_URL)
                .jsonData(TIME_FROM_ROI_NET_BODY)
                .retryCount(Constant.EVENT_REPORT_TRY_COUNT)
                .callback(object : HttpCallback.TimeCallback() {
                    override fun onFailure(code: Int, errorMessage: String?) {
                    }

                    override fun onResponse(response: Long) {
                        _latestTime = response
                        _latestSystemElapsedRealtime = getSystemHibernateTimeGap()
                    }

                }).execute()
        }
    }

    fun getVerifyTimeAsync() =
        if (_latestTime == TIME_NOT_VERIFY_VALUE) {
            TIME_NOT_VERIFY_VALUE
        } else {
            getSystemHibernateTimeGap() - _latestSystemElapsedRealtime + _latestTime
        }

    fun  getVerifyTimeAsyncByGapTime(gapTime:Long)= if (_latestSystemElapsedRealtime-gapTime>0) _latestTime-(_latestSystemElapsedRealtime-gapTime) else getVerifyTimeAsync()


    /**
     * Get system hibernate time gap
     *
     */
    fun getSystemHibernateTimeGap() = SystemClock.elapsedRealtime()
}