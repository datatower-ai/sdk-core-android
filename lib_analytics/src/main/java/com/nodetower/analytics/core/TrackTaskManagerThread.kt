package com.nodetower.analytics.core

import com.nodetower.base.utils.LogUtils
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class TrackTaskManagerThread internal constructor() : Runnable {

    private var mTrackTaskManager: TrackTaskManager? = null

    /**
     * 创建一个可重用固定线程数的线程池
     */
    private var mTrackExecutor: ExecutorService? = null

    /**
     * 是否停止
     */
    var isStopped = false
        private set

    override fun run() {
        try {
            //如果未停止，则执行此while
            while (!isStopped) {
                val downloadTask = mTrackTaskManager!!.takeTrackEventTask()
                mTrackExecutor!!.execute(downloadTask)
            }
            //如果停止，则执行此while，先取出队列内所有task 并执行，取到为空时结束
            while (true) {
                val downloadTask = mTrackTaskManager!!.pollTrackEventTask() ?: break
                mTrackExecutor!!.execute(downloadTask)
            }
            mTrackExecutor!!.shutdown()
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
            mTrackExecutor = Executors.newFixedThreadPool(POOL_SIZE)
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
    }
}
