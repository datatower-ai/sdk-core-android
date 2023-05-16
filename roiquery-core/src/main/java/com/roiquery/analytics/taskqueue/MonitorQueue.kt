package com.roiquery.analytics.taskqueue

import android.os.Handler
import android.os.Looper
import com.roiquery.analytics.data.EventDataAdapter
import com.roiquery.analytics.utils.LogUtils
import com.roiquery.quality.ROIQueryErrorParams
import com.roiquery.quality.ROIQueryQualityHelper
import kotlinx.coroutines.runBlocking
import org.slf4j.helpers.Util
import java.lang.Thread.sleep

// 需要上报的场景，记录下
//1 上报gaid获取不到，检测是不是用户未授权
//2 检测用户是否重启了系统，或者修改了本地时间 (done)
//3 uploadData的每个子步骤的超时（现在定的是5秒）
//4 MainQueue任务执行耗时超过1秒
//5 所有串行队列是否卡死（1分钟无响应）(done)
//6 DB的未上报记录超过100条 (done)
//7 uploadData连续3次执行失败
class MonitorQueue private constructor() : AsyncTaskQueue("MonitorQueue") {
    @Volatile
    var mMainQueueFlag = 1
    var mUploadQueueFlag = 1
    var mDBQueueFlag = 1
    private var isRunning = false
    fun startMonitor() {
        if (isRunning) return
        isRunning = true
        postTask {
          loop()
        }
    }

    fun stopMonitor() {
        isRunning = false
    }

    fun checkDBCount() {
        val count = runBlocking { EventDataAdapter.getInstance()?.queryDataCount()?.await() }
        count?.let {
            if (it > 100) {
                ROIQueryQualityHelper.instance.reportQualityMessage(
                    ROIQueryErrorParams.CODE_DB_DATA_COUNT,
                    null
                )
            }
        }
    }

    fun reportUploadError(reason: Int) {

    }

    fun loop() {
        // db count
        checkDBCount()

        // 卡死
        MainQueue.get().postTask { get()?.mMainQueueFlag = 1 }
        DataUploadQueue.get().postTask { get()?.mUploadQueueFlag = 1 }
        DBQueue.get().postTask { get()?.mDBQueueFlag = 1 }

        Handler(Looper.getMainLooper()).postDelayed({

            get()?.postTask {
                if (mMainQueueFlag != 1) {
                    val stackTrack = MainQueue.get().currentThread.stackTrace.toString()
                    ROIQueryQualityHelper.instance.reportQualityMessage(
                        ROIQueryErrorParams.CODE_QUEUE_MAIN_DEAD,
                        stackTrack
                    )
                }

                if (mUploadQueueFlag != 1) {
                    val stackTrack = DataUploadQueue.get().currentThread.stackTrace.toString()
                    ROIQueryQualityHelper.instance.reportQualityMessage(
                        ROIQueryErrorParams.CODE_QUEUE_UPLOAD_DEAD,
                        stackTrack
                    )
                }

                if (mDBQueueFlag != 1) {
                    val stackTrack = DBQueue.get().currentThread.stackTrace.toString()
                    ROIQueryQualityHelper.instance.reportQualityMessage(
                        ROIQueryErrorParams.CODE_QUEUE_DB_DEAD,
                        stackTrack
                    )
                }

                mMainQueueFlag = 2
                mUploadQueueFlag = 2
                mDBQueueFlag = 2

                // Your Code
                if (isRunning) {
                    get()?.postTask {
                        loop()
                    }
                }
            }
        }, 10000)
    }

    companion object {
        @Volatile
        private var singleton //1:volatile修饰
                : MonitorQueue? = null

        fun get(): MonitorQueue? {
            if (singleton == null) {  //2:减少不要同步，优化性能
                synchronized(MonitorQueue::class.java) {  // 3：同步，线程安全
                    if (singleton == null) {
                        singleton = MonitorQueue() //4：创建singleton 对象
                    }
                }
            }
            return singleton
        }
    }
}