package com.roiquery.analytics.utils

import android.os.SystemClock
import com.roiquery.analytics.Constant
import com.roiquery.analytics.Constant.TIME_FROM_ROI_NET_BODY
import com.roiquery.analytics.DTAnalytics
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.core.EventUploadManager
import com.roiquery.analytics.data.EventDataAdapter
import com.roiquery.analytics.network.HttpMethod
import com.roiquery.analytics.network.RequestHelper
import com.roiquery.quality.PerfAction
import com.roiquery.quality.PerfLogger
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantReadWriteLock

/**
 * author: xiaosailing
 * date: 2022-06-23
 * description:
 * version：1.0
 */
class TimeCalibration private constructor() {

    companion object {
        const val TIME_NOT_VERIFY_VALUE = 0L
        val instance: TimeCalibration by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            TimeCalibration()
        }
    }

    @Volatile
    private var _latestTime =  TIME_NOT_VERIFY_VALUE
    @Volatile
    private var _latestSystemElapsedRealtime = 0L

    private val calibratedTimeLock = ReentrantReadWriteLock()

    private var isVerifyTimeRunning = AtomicBoolean(false)

    fun getReferenceTime() {
        Thread {
            if (_latestTime == 0L) {
                if (isVerifyTimeRunning.get()) {
                    return@Thread
                }
                PerfLogger.doPerfLog(PerfAction.GETSRVTIMEBEGIN, System.currentTimeMillis())

                isVerifyTimeRunning.set(true)

                //子进程只读取主进程的时间，不获取服务器时间
                if (!ProcessUtil.isMainProcess(AnalyticsConfig.instance.mContext)) {
                    setVerifyTimeForSubProcess()
                    isVerifyTimeRunning.set(false)
                    return@Thread
                }
                val response = RequestHelper.Builder(
                    HttpMethod.POST_SYNC,
                    EventUploadManager.getInstance()?.getEventUploadUrl()
                )
                    .jsonData(TIME_FROM_ROI_NET_BODY)
                    .retryCount(Constant.EVENT_REPORT_TRY_COUNT)
                    .executeSync()

                if (response != null && response.date != 0L) {
                    setVerifyTime(response.date)
                    //避免因为时间未同步而造成数据堆积
                    if (DTAnalytics.isSDKInitSuccess()) {
                        EventUploadManager.getInstance()?.flush()
                    }
                }
                isVerifyTimeRunning.set(false)

                PerfLogger.doPerfLog(PerfAction.GETSRVTIMEEND, System.currentTimeMillis())
            }
        }.start()
    }

    private fun setVerifyTime(time: Long){
        calibratedTimeLock.writeLock().lock()
        if (_latestTime == TIME_NOT_VERIFY_VALUE){
            val dbTime = runBlocking { EventDataAdapter.getInstance()?.getLatestNetTime()?.await()  }
            val dbGamTime = runBlocking { EventDataAdapter.getInstance()?.getLatestGapTime()?.await() }

            if (time - (dbTime ?: TIME_NOT_VERIFY_VALUE) > 5000){
                _latestTime = time
                _latestSystemElapsedRealtime = SystemClock.elapsedRealtime()
                EventDataAdapter.getInstance()?.setLatestNetTime(_latestTime)
                EventDataAdapter.getInstance()?.setLatestGapTime( _latestSystemElapsedRealtime)
            } else {
                _latestTime = dbTime ?: TIME_NOT_VERIFY_VALUE
                _latestSystemElapsedRealtime = dbGamTime ?: TIME_NOT_VERIFY_VALUE
            }
        }
        calibratedTimeLock.writeLock().unlock()
    }

    private fun setVerifyTimeForSubProcess(){
        calibratedTimeLock.writeLock().lock()
        val dbTime = runBlocking { EventDataAdapter.getInstance()?.getLatestNetTime()?.await()  }
        val dbGamTime = runBlocking { EventDataAdapter.getInstance()?.getLatestGapTime()?.await() }
        _latestTime = dbTime ?: TIME_NOT_VERIFY_VALUE
        _latestSystemElapsedRealtime = dbGamTime ?: TIME_NOT_VERIFY_VALUE
        calibratedTimeLock.writeLock().unlock()
    }

    fun getVerifyTimeAsync(): Long {
        calibratedTimeLock.readLock().lock()
        val time = if (_latestTime == TIME_NOT_VERIFY_VALUE) {
            TIME_NOT_VERIFY_VALUE
        } else {
            if (_latestSystemElapsedRealtime == 0L) _latestTime
            else SystemClock.elapsedRealtime() - _latestSystemElapsedRealtime + _latestTime
        }
        calibratedTimeLock.readLock().unlock()
        return time
    }

    fun getVerifyTimeAsyncByGapTime(gapTime:Long) : Long {
        calibratedTimeLock.readLock().lock()
        if (_latestSystemElapsedRealtime == 0L) {
            _latestSystemElapsedRealtime = SystemClock.elapsedRealtime()
        }
        var tempGapTime = gapTime
        if (gapTime == 0L){
            tempGapTime = SystemClock.elapsedRealtime()
        }
        val time = if (_latestSystemElapsedRealtime - tempGapTime > 0) _latestTime - (_latestSystemElapsedRealtime - tempGapTime) else getVerifyTimeAsync()
        calibratedTimeLock.readLock().unlock()
        return time
    }

    fun getServerTime() = _latestTime

    fun getUpdateSystemUpTime() = _latestSystemElapsedRealtime

    /**
     * Get system hibernate time gap
     *
     */
    fun getSystemHibernateTimeGap() = SystemClock.elapsedRealtime()

    init {
        if (ProcessUtil.isMainProcess(AnalyticsConfig.instance.mContext)){
            EventDataAdapter.getInstance()?.setLatestNetTime(TIME_NOT_VERIFY_VALUE)
            EventDataAdapter.getInstance()?.setLatestGapTime(TIME_NOT_VERIFY_VALUE)
        }
    }
}
