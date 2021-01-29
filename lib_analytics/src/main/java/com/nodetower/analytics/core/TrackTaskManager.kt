package com.nodetower.analytics.core

import com.nodetower.base.utils.LogUtils
import java.util.concurrent.LinkedBlockingQueue


class TrackTaskManager private constructor() {

    private var mDataCollectEnable = true

    /**
     * 请求线程队列
     */
    private val mTrackEventTasks: LinkedBlockingQueue<Runnable> = LinkedBlockingQueue()
    private val mTrackEventTasksCache: LinkedBlockingQueue<Runnable> = LinkedBlockingQueue()

    fun addTrackEventTask(trackEvenTask: Runnable) {
        try {
            //用户暂未同意相关条款时，先存入缓存
            if (mDataCollectEnable)
                mTrackEventTasks.put(trackEvenTask)
            else
                mTrackEventTasksCache.put(trackEvenTask)
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
    }

    /**
     * 将任务添加到真正执行的队列
     * @param runnable Runnable
     */
    fun transformTaskQueue(runnable: Runnable) {
        try {
            // 最多只处理 50 条
            if (mTrackEventTasks.size <= 50) mTrackEventTasks.put(runnable)
        } catch (e: InterruptedException) {
            LogUtils.printStackTrace(e)
        }
    }

    /**
     * 取出任务，如果队列为空，会阻塞
     */
    fun takeTrackEventTask(): Runnable? {
        try {
            return if (mDataCollectEnable) mTrackEventTasks.take() else mTrackEventTasksCache.take()
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
        return null
    }

    /**
     * 取出任务，如果队列为空，返回null
     */
    fun pollTrackEventTask(): Runnable? {
        try {
            return if (mDataCollectEnable) mTrackEventTasks.poll() else mTrackEventTasksCache.poll()
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
        return null
    }

    val isEmpty: Boolean
        get() = mTrackEventTasks.isEmpty()

    fun setDataCollectEnable(isDataCollectEnable: Boolean) {
        mDataCollectEnable = isDataCollectEnable
        try {
            if (isDataCollectEnable) {
                mTrackEventTasksCache.put(Runnable { })
            } else {
                mTrackEventTasks.put(Runnable { })
            }
        } catch (e: InterruptedException) {
            LogUtils.printStackTrace(e)
        }
    }

    companion object {
        private var trackTaskManager: TrackTaskManager? = null

        @get:Synchronized
        val instance: TrackTaskManager?
            get() {
                try {
                    if (null == trackTaskManager) {
                        trackTaskManager = TrackTaskManager()
                    }
                } catch (e: Exception) {
                    LogUtils.printStackTrace(e)
                }
                return trackTaskManager
            }
    }

}
