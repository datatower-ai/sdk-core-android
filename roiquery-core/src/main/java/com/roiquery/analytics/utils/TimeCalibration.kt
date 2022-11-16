package com.roiquery.analytics.utils

import android.os.SystemClock
import android.util.Log
import com.google.android.gms.common.util.ProcessUtils
import com.roiquery.analytics.Constant
import com.roiquery.analytics.Constant.TIME_FROM_ROI_NET_BODY
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.analytics.core.EventUploadManager
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.network.HttpCallback
import com.roiquery.analytics.network.HttpMethod
import com.roiquery.analytics.network.RequestHelper
import java.util.concurrent.atomic.AtomicLong

/**
 * author: xiaosailing
 * date: 2022-06-23
 * description:
 * version：1.0
 */
class TimeCalibration {

    companion object {
        const val TIME_NOT_VERIFY_VALUE = 0L
        val instance: TimeCalibration by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            TimeCalibration()
        }
    }

    private constructor(){
        EventDateAdapter.getInstance()?.latestNetTime = TIME_NOT_VERIFY_VALUE
        EventDateAdapter.getInstance()?.latestGapTime = TIME_NOT_VERIFY_VALUE
    }
    @Volatile
    private var _latestTime =  TIME_NOT_VERIFY_VALUE
    @Volatile
    private var _latestSystemElapsedRealtime = 0L
    fun getReferenceTime() {
        if (_latestTime == 0L) {
            //子进程只读取主进程的时间，不获取服务器时间
            if (!ProcessUtil.isMainProcess(AdtUtil.getInstance().applicationContext)){
                _latestTime = EventDateAdapter.getInstance()?.latestNetTime ?: TIME_NOT_VERIFY_VALUE
                _latestSystemElapsedRealtime = EventDateAdapter.getInstance()?.latestGapTime ?: TIME_NOT_VERIFY_VALUE
                return
            }
            RequestHelper.Builder(HttpMethod.POST_ASYNC, Constant.EVENT_REPORT_URL)
                .jsonData(TIME_FROM_ROI_NET_BODY)
                .retryCount(Constant.EVENT_REPORT_TRY_COUNT)
                .callback(object : HttpCallback.TimeCallback() {
                    override fun onFailure(code: Int, errorMessage: String?) {
                    }

                    override fun onResponse(response: Long) {
                        synchronized(instance){
                            if (_latestTime == TIME_NOT_VERIFY_VALUE){
                                if (response-(EventDateAdapter.getInstance()?.latestNetTime ?: TIME_NOT_VERIFY_VALUE)>5000){
                                    _latestTime = response
                                    EventDateAdapter.getInstance()?.latestNetTime = _latestTime
                                    _latestSystemElapsedRealtime = getSystemHibernateTimeGap()
                                    EventDateAdapter.getInstance()?.latestGapTime = _latestSystemElapsedRealtime
                                }else{
                                    _latestTime = EventDateAdapter.getInstance()?.latestNetTime ?: TIME_NOT_VERIFY_VALUE
                                    _latestSystemElapsedRealtime = EventDateAdapter.getInstance()?.latestGapTime ?: TIME_NOT_VERIFY_VALUE
                                }
                            }
                            //避免因为时间未同步而造成数据堆积
                            if (ROIQueryAnalytics.isSDKInitSuccess()) {
                                EventUploadManager.getInstance()?.flush()
                            }
                        }

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