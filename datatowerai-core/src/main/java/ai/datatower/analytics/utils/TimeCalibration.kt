package ai.datatower.analytics.utils

import android.os.SystemClock
import ai.datatower.ad.utils.UUIDUtils
import ai.datatower.analytics.Constant
import ai.datatower.analytics.Constant.TIME_FROM_ROI_NET_BODY
import ai.datatower.analytics.DTAnalytics
import ai.datatower.analytics.config.AnalyticsConfig
import ai.datatower.analytics.core.EventUploadManager
import ai.datatower.analytics.data.EventDataAdapter
import ai.datatower.analytics.network.HttpMethod
import ai.datatower.analytics.network.RequestHelper
import ai.datatower.quality.PerfAction
import ai.datatower.quality.PerfLogger
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.math.abs

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

    private var _latestDeviceTime = TIME_NOT_VERIFY_VALUE

    val sessionId: String = UUIDUtils.generateUUID()

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
                _latestDeviceTime = System.currentTimeMillis()
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

    fun getServerTime(): Long {
        calibratedTimeLock.readLock().lock()
        val ret = _latestTime
        calibratedTimeLock.readLock().unlock()

        return ret
    }

    fun getUpdateSystemUpTime(): Long {
        calibratedTimeLock.readLock().lock()
        val ret = _latestSystemElapsedRealtime
        calibratedTimeLock.readLock().unlock()

        return ret
    }

    fun getDeviceTime() : Long {
        calibratedTimeLock.readLock().lock()
        val ret = _latestDeviceTime
        calibratedTimeLock.readLock().unlock()

        return ret
    }

    fun isDeviceTimeCorrect(): Boolean {
        calibratedTimeLock.readLock().lock()
        if (_latestSystemElapsedRealtime == 0L) return false

//        5分钟的误差
        val ret = abs(_latestTime - _latestDeviceTime) < 5 * 60 * 1000L

        calibratedTimeLock.readLock().unlock()

        return ret
    }

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
