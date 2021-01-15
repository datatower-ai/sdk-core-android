package com.nodetower.analytics.core

import com.nodetower.base.utils.LogUtils
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class TrackTaskManagerThread internal constructor() : Runnable {

    private var mTrackTaskManager: TrackTaskManager? = null

    /**
     * 创建一个可重用固定线程数的线程池
     */
    private var mPool: ExecutorService? = null

    /**
     * 是否停止
     */
    var isStopped = false
        private set

    override fun run() {
        try {
            while (!isStopped) {
                val downloadTask = mTrackTaskManager!!.takeTrackEventTask()
                mPool!!.execute(downloadTask)
            }
            while (true) {
                val downloadTask = mTrackTaskManager!!.pollTrackEventTask() ?: break
                mPool!!.execute(downloadTask)
            }
            mPool!!.shutdown()
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
    }

    fun stop() {
        isStopped = true
        //解决队列阻塞时,停止队列还会触发一次事件
        mTrackTaskManager?.let { trackManager ->
            if (trackManager.isEmpty) {
                trackManager.addTrackEventTask{}
            }
        }
    }

    companion object {
        /**
         * 创建一个可重用固定线程数的线程池
         */
        private const val POOL_SIZE = 1
    }

    init {
        try {
            mTrackTaskManager = TrackTaskManager.instance
            mPool = Executors.newFixedThreadPool(POOL_SIZE)
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
    }
}
