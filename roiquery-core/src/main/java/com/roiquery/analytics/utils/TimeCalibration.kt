package com.roiquery.analytics.utils

import android.os.SystemClock
import android.util.Log
import com.roiquery.analytics.Constant
import com.roiquery.analytics.Constant.TIME_FROM_ROI_NET_BODY
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.analytics.core.EventUploadManager
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.network.HttpCallback
import com.roiquery.analytics.network.HttpMethod
import com.roiquery.analytics.network.RequestHelper
import com.roiquery.quality.ROIQueryErrorParams
import com.roiquery.quality.ROIQueryQualityHelper
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs

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
            if (!ProcessUtils.isInMainProcess(AdtUtil.getInstance().applicationContext)){
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


    fun getVerifyTimeAsync(): Long{
        if (_latestTime == TIME_NOT_VERIFY_VALUE) {
            return TIME_NOT_VERIFY_VALUE
        } else {
            val time = getSystemHibernateTimeGap() - _latestSystemElapsedRealtime + _latestTime
            judgeIllegalTime(time,0L,"Async")
            return time
        }
    }


    fun  getVerifyTimeAsyncByGapTime(gapTime:Long): Long{
        val time = if (_latestSystemElapsedRealtime-gapTime > 0){
            _latestTime-(_latestSystemElapsedRealtime-gapTime)
        } else {
            getVerifyTimeAsync()
        }
        judgeIllegalTime(time, gapTime, "GapTime")
        return time
    }


    /**
     * Get system hibernate time gap
     *
     */
    fun getSystemHibernateTimeGap() = SystemClock.elapsedRealtime()


    //just for debug
    private val oneWeekMs =  7 * 24 * 60 * 60 * 1000

    private fun judgeIllegalTime(time: Long, gapTime: Long, type: String){
        // 以2022.11.15(1668441600000)为基准， 这个时间一周前的，以及这个时间未来一周的时间，都认为非法
        if (abs(time - 1668441600000) > oneWeekMs){
            val processName = ProcessUtils.getProcessName(AdtUtil.getInstance().applicationContext)
            val msg = "processName: $processName, type: $type, time: $time, gapTime: $gapTime, serverTime: $_latestTime, systemHibernateTime: $_latestSystemElapsedRealtime"
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.CODE_ILLEGAL_TIME_ERROR,
                msg,
                ROIQueryErrorParams.ILLEGAL_TIME_ERROR
            )
        }

    }
}