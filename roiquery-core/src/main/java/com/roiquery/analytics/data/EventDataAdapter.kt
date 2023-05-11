package com.roiquery.analytics.data

import android.content.Context
import com.roiquery.analytics.taskqueue.DBQueue
import com.roiquery.analytics.taskqueue.asyncCatching
import com.roiquery.analytics.taskqueue.asyncChained
import com.roiquery.analytics.utils.TimeCalibration
import com.roiquery.quality.PerfAction
import com.roiquery.quality.PerfLogger
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.json.JSONObject

class EventDataAdapter private constructor(
    context: Context,
){
    private var mOperation: EventDataOperation? = EventDataOperation(context.applicationContext)

    // region Property accessors

    /**
     * 是否上报数据，默认是
     */
    fun isUploadEnabled() = DBQueue.get().asyncChained {
        getBooleanConfig(DataParams.CONFIG_ENABLE_UPLOADS, true)
    }

    fun setIsUploadEnabled(value: Boolean) = DBQueue.get().async {
        setBooleanConfig(DataParams.CONFIG_ENABLE_UPLOADS, value)
    }

    /**
     * install 事件的插入数据库状态
     */
    fun isAppInstallInserted() = DBQueue.get().asyncChained {
        getBooleanConfig(DataParams.CONFIG_APP_INSTALL_INSERT_STATE, false)
    }

    fun setIsAppInstallInserted(value: Boolean) = DBQueue.get().async {
        setBooleanConfig(DataParams.CONFIG_APP_INSTALL_INSERT_STATE, value)
    }

    /**
     * 第一次 session_start 事件的插入数据库状态
     */
    fun isFirstSessionStartInserted() = DBQueue.get().asyncChained {
        getBooleanConfig(DataParams.CONFIG_FIRST_SESSION_START_INSERT_STATE, false)
    }

    fun setIsFirstSessionStartInserted(value: Boolean) = DBQueue.get().async {
        setBooleanConfig(DataParams.CONFIG_FIRST_SESSION_START_INSERT_STATE, value)
    }

    /** DataTower id */
    fun getDtId() = DBQueue.get().asyncChained {
         getStringConfig(DataParams.CONFIG_DT_ID)
    }

    fun setDtIdIfNeeded(value: String) = DBQueue.get().async {
        val ret = getStringConfig(DataParams.CONFIG_DT_ID)
        if (ret.isEmpty()) {
            setStringConfig(DataParams.CONFIG_DT_ID, value)
        }
    }

    fun getLatestNetTime() = DBQueue.get().asyncChained {
        getLongConfig(DataParams.LATEST_NET_TIME, TimeCalibration.TIME_NOT_VERIFY_VALUE)
    }

    fun setLatestNetTime(value: Long) = DBQueue.get().async {
        setLongConfig(DataParams.LATEST_NET_TIME, value)
    }

    fun getLatestGapTime() = DBQueue.get().asyncChained {
        getLongConfig(DataParams.LATEST_GAP_TIME, TimeCalibration.TIME_NOT_VERIFY_VALUE)
    }

    fun setLatestGapTime(value: Long) = DBQueue.get().async {
        setLongConfig(DataParams.LATEST_GAP_TIME, value)
    }

    /**
     *  acountId,自有用户系统id
     *
     * @return acountId
     *
     * Thread safety: Guarded by serial execution of [DBQueue].
     */
    private var accountIdCached = ""

    fun getAccountId() = DBQueue.get().asyncChained {
        if (accountIdCached.isEmpty()) {
            accountIdCached = getStringConfig(DataParams.CONFIG_ACCOUNT_ID)
        }
    }

    fun setAccountId(value: String) = DBQueue.get().async {
        if (accountIdCached == value) return@async
        accountIdCached = value
        setStringConfig(DataParams.CONFIG_ACCOUNT_ID, value)
    }

    // endregion

    /**
     * Adds a JSON string representing an event with properties or a person record
     * to the SQLiteDatabase.
     *
     * @param data the event JSON to record
     * @param eventSyn event id
     * @return the number of rows in the table, or DB_OUT_OF_MEMORY_ERROR/DB_UPDATE_ERROR
     * on failure
     */
     /**
     * Adds a JSON string representing an event with properties or a person record
     * to the SQLiteDatabase.
     *
     * @param data the event JSON to record
     * @param eventSyn event id
     * @return the number of rows in the table, or DB_OUT_OF_MEMORY_ERROR/DB_UPDATE_ERROR
     * on failure
     */
    fun addJSON(data: JSONObject?, eventSyn: String): Deferred<Int> {
        return DBQueue.get().async {
            PerfLogger.doPerfLog(PerfAction.WRITEEVENTTODBBEGIN, System.currentTimeMillis())
            val returns = Result.runCatching {
                mOperation?.insertData(data, eventSyn) ?: DataParams.DB_ADD_JSON_ERROR
            }.getOrNull() ?: DataParams.DB_ADD_JSON_ERROR
            PerfLogger.doPerfLog(PerfAction.WRITEEVENTTODBEND, System.currentTimeMillis())
            returns
        }
    }

    /**
     * Removes all events from table
     */
    /**
     * Removes all events from table
     */
    fun deleteAllEvents() = DBQueue.get().async {
        mOperation?.deleteAllEventData()
    }

            /**
     * Removes events with an _id &lt;= last_id from table
     *
     * @param eventSyn the last id to delete
     * @return the number of rows in the table
     */

    /**
     * Removes events with an _id &lt;= last_id from table
     *
     * @param eventSyn the last id to delete
     * @return the number of rows in the table
     */
    /* FIXME: Disabled function: nobody invokes it.
    fun cleanupEventsSync(eventSyn: String?, callback: AsyncGetDBData?) {
        eventSyn?.let { mOperation?.deleteEventByEventSyn(it) }
        MainQueue.get().postTask {
            callback?.let {
                it.onDataGet(true)
            }
        }
    }
     */

    /**
     * Removes events with an _id &lt;= last_id from table
     *
     * @param eventSyn the last id to delete
     * @return the number of rows in the table
     */
    /**
     * Removes events with an _id &lt;= last_id from table
     *
     * @param eventSyn the last id to delete
     * @return the number of rows in the table
     */
    fun cleanupBatchEvents(eventSyns: List<String>) =
        DBQueue.get().asyncCatching {
            mOperation?.deleteBatchEventByEventSyn(eventSyns)
        }

    /**
     * 从 Event 表中读取上报数据
     * @param limit 条数限制
     * @return 数据
     */
    /**
     * 从 Event 表中读取上报数据
     * @param limit 条数限制
     * @return 数据
     */
    fun readEventsDataFromDb(limit: Int) =
        DBQueue.get().asyncCatching {
            mOperation?.queryData(limit)
        }


    // region get/set<Type>Config

    private suspend fun getBooleanConfig(key: String, default: Boolean = false): Boolean {
        val value = mOperation?.queryConfig(key)
        return if (!value.isNullOrEmpty()) {
            value == "true" || (value == "null" && default)
        } else default
    }

    private suspend fun setBooleanConfig(key: String, value: Boolean) {
        mOperation?.insertConfig(key, value.toString())
    }


//    private suspend fun getIntConfig(key: String,default: Int = 0): Int{
//        val values = mOperation?.queryConfig(key)
//        return if (values != null && values.isNotEmpty() && values != "null") {
//            values.toInt()
//        } else default
//    }
//
//    private fun setIntConfig(
//        key: String,
//        value: Int
//    ) {
//        mOperation?.insertConfig(
//            key,
//            value.toString()
//        )
//    }


    private suspend fun getStringConfig(key: String): String {
        val value = mOperation?.queryConfig(key)
        return if (!value.isNullOrEmpty() && value != "null") value else ""
    }

    private suspend fun setStringConfig(key: String, value: String) {
        mOperation?.insertConfig(key, value)
    }

    private suspend fun getLongConfig(
        key: String,
        @Suppress("SameParameterValue")
        default: Long = 0L,
    ): Long {
        val value = mOperation?.queryConfig(key)
        return if (!value.isNullOrEmpty() && value != "null") {
            value.toLongOrNull() ?: default
        } else default
    }

    private suspend fun setLongConfig(key: String, value: Long) =
        mOperation?.insertConfig(key, value.toString())

    // endregion

    companion object {
        private var instance: EventDataAdapter? = null
        @Synchronized
        internal fun getInstance(
            context: Context
        ): EventDataAdapter? {
            if (instance == null) {
                instance = EventDataAdapter(context)
            }
            return instance
        }

        internal fun getInstance(): EventDataAdapter? {
            checkNotNull(instance) { "Call ROIQuerySDK.init first" }
            return instance
        }
    }


}
