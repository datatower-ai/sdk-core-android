package com.roiquery.analytics.utils

import android.os.SystemClock
import com.roiquery.analytics.Constant
import com.roiquery.analytics.network.HttpCallback
import com.roiquery.analytics.network.HttpMethod
import com.roiquery.analytics.network.RequestHelper
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

    private var _latestTime = 0L
    private var _latestSystemElapsedRealtime = 0L
    private fun getReferenceTime(timeError: () -> Unit, timeFinish: () -> Unit = {}) {
        if (_latestTime == 0L) {
            RequestHelper.Builder(HttpMethod.POST_ASYNC, Constant.EVENT_REPORT_URL)
                .jsonData(TIME_FROM_ROI_NET_BODY)
                .retryCount(Constant.EVENT_REPORT_TRY_COUNT)
                .callback(object : HttpCallback.TimeCallback() {
                    override fun onFailure(code: Int, errorMessage: String?) {
                        timeError.invoke()
                    }

                    override fun onResponse(response: Long) {
                        _latestTime = response
                        _latestSystemElapsedRealtime = SystemClock.elapsedRealtime()
                        timeFinish.invoke()
                    }

                }).execute()
        }
    }

    suspend fun getVerifyTimeAsync()=
        suspendCoroutine<Long> {
            if (_latestTime == TIME_NOT_VERIFY_VALUE) {
                getReferenceTime({
                    it.resume(TIME_NOT_VERIFY_VALUE)
                }) {
                    it.resume(
                        SystemClock.elapsedRealtime() - _latestSystemElapsedRealtime + _latestTime
                    )
                }
            } else {
                it.resume(
                    SystemClock.elapsedRealtime() - _latestSystemElapsedRealtime + _latestTime
                )
            }
        }



}