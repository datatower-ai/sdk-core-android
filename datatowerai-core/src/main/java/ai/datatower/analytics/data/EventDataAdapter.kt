package ai.datatower.analytics.data

import ai.datatower.analytics.taskqueue.DBQueue
import ai.datatower.analytics.taskqueue.asyncSequential
import ai.datatower.analytics.taskqueue.asyncSequentialCatching
import ai.datatower.analytics.taskqueue.asyncSequentialChained
import ai.datatower.analytics.taskqueue.launchSequential
import ai.datatower.analytics.utils.LogUtils
import ai.datatower.analytics.utils.TimeCalibration
import ai.datatower.quality.PerfAction
import ai.datatower.quality.PerfLogger
import android.content.Context
import android.util.Log
import org.json.JSONObject

class EventDataAdapter private constructor(
    context: Context,
){
    private var mOperation: EventDataOperation? = EventDataOperation(context.applicationContext)

    // region Property accessors

    /**
     * 是否上报数据，默认是
     */
    fun isUploadEnabled() = DBQueue.get().asyncSequentialChained {
        getBooleanConfig(DataParams.CONFIG_ENABLE_UPLOADS, true)
    }

    fun setIsUploadEnabled(value: Boolean) = DBQueue.get().asyncSequential {
        setBooleanConfig(DataParams.CONFIG_ENABLE_UPLOADS, value)
    }

    /**
     * install 事件的插入数据库状态
     */
    fun isAppInstallInserted() = DBQueue.get().asyncSequentialChained {
        getBooleanConfig(DataParams.CONFIG_APP_INSTALL_INSERT_STATE, false)
    }

    fun setIsAppInstallInserted(value: Boolean) = DBQueue.get().asyncSequential {
        setBooleanConfig(DataParams.CONFIG_APP_INSTALL_INSERT_STATE, value)
    }

    /**
     * 第一次 session_start 事件的插入数据库状态
     */
    fun isFirstSessionStartInserted() = DBQueue.get().asyncSequentialChained {
        getBooleanConfig(DataParams.CONFIG_FIRST_SESSION_START_INSERT_STATE, false)
    }

    fun setIsFirstSessionStartInserted(value: Boolean) = DBQueue.get().asyncSequential {
        setBooleanConfig(DataParams.CONFIG_FIRST_SESSION_START_INSERT_STATE, value)
    }

    /** DataTower id */
    fun getDtId() = DBQueue.get().asyncSequentialChained {
         getStringConfig(DataParams.CONFIG_DT_ID)
    }

    fun setDtIdIfNeeded(value: String) = DBQueue.get().asyncSequential {
        val ret = getStringConfig(DataParams.CONFIG_DT_ID)
        if (ret.isEmpty()) {
            setStringConfig(DataParams.CONFIG_DT_ID, value)
        }
    }

    fun getLatestNetTime() = DBQueue.get().asyncSequentialChained {
        getLongConfig(DataParams.LATEST_NET_TIME, TimeCalibration.TIME_NOT_VERIFY_VALUE)
    }

    fun setLatestNetTime(value: Long) = DBQueue.get().launchSequential {
        setLongConfig(DataParams.LATEST_NET_TIME, value)
    }

    fun getLatestGapTime() = DBQueue.get().asyncSequentialChained {
        getLongConfig(DataParams.LATEST_GAP_TIME, TimeCalibration.TIME_NOT_VERIFY_VALUE)
    }

    fun setLatestGapTime(value: Long) = DBQueue.get().launchSequential {
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

    fun getAccountId() = DBQueue.get().asyncSequentialChained {
        if (accountIdCached.isEmpty()) {
            accountIdCached = getStringConfig(DataParams.CONFIG_ACCOUNT_ID)
        }
        return@asyncSequentialChained accountIdCached
    }

    fun setAccountId(value: String) = DBQueue.get().launchSequential {
        if (accountIdCached == value) return@launchSequential
        accountIdCached = value
        setStringConfig(DataParams.CONFIG_ACCOUNT_ID, value)
    }

    /**
     *  访客 id
     *
     * Thread safety: Guarded by serial execution of [DBQueue].
     */
    private var distinctIdCached = ""

    fun getDistinctId() = DBQueue.get().asyncSequentialChained {
        if (distinctIdCached.isEmpty()) {
            distinctIdCached = getStringConfig(DataParams.CONFIG_DISTINCT_ID)
        }
        return@asyncSequentialChained distinctIdCached
    }

    fun setDistinctId(value: String) = DBQueue.get().launchSequential {
        if (distinctIdCached == value) return@launchSequential
        distinctIdCached = value
        setStringConfig(DataParams.CONFIG_DISTINCT_ID, value)
    }

    fun setStaticSuperProperties(properties: JSONObject) = DBQueue.get().launchSequential {
        setStringConfig(DataParams.CONFIG_STATIC_SUPER_PROPERTY, properties.toString())
    }

    fun getStaticSuperProperties() = DBQueue.get().asyncSequentialChained {
        val jsonStr = getStringConfig(DataParams.CONFIG_STATIC_SUPER_PROPERTY)
        return@asyncSequentialChained try {
            JSONObject(jsonStr)
        } catch (t: Throwable) {
            LogUtils.e("getStaticSuperProperties", t)
            JSONObject()
        }
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
    fun addJSON(data: JSONObject?, eventSyn: String) = DBQueue.get().asyncSequentialChained {
         PerfLogger.doPerfLog(PerfAction.WRITEEVENTTODBBEGIN, System.currentTimeMillis())
         val returns = Result.runCatching {
             mOperation?.insertData(data, eventSyn) ?: DataParams.DB_ADD_JSON_ERROR
         }.getOrNull() ?: DataParams.DB_ADD_JSON_ERROR
         PerfLogger.doPerfLog(PerfAction.WRITEEVENTTODBEND, System.currentTimeMillis())
         returns
     }

    /**
     * Removes all events from table
     */
    /**
     * Removes all events from table
     */
    fun deleteAllEvents() = DBQueue.get().launchSequential {
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
        DBQueue.get().asyncSequentialCatching {
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
        DBQueue.get().asyncSequentialCatching {
            mOperation?.queryData(limit)
        }

    fun queryDataCount() =
        DBQueue.get().asyncSequentialChained {
            mOperation?.queryDataCount()
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
            checkNotNull(instance) { "Call DT.init first" }
            return instance
        }
    }


}
