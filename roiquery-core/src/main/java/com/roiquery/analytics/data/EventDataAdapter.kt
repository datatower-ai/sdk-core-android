package com.roiquery.analytics.data

import android.content.Context
import com.roiquery.analytics.taskqueue.DBQueue
import com.roiquery.analytics.taskqueue.MainQueue
import com.roiquery.analytics.taskqueue.postTaskAsync
import com.roiquery.analytics.utils.TimeCalibration
import com.roiquery.quality.PerfAction
import com.roiquery.quality.PerfLogger
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import org.json.JSONObject

interface AsyncGetDBData {
    fun onDataGet(data: Any?)
}

class EventDateAdapter private constructor(
    context: Context,
){
    private var mOperation: EventDataOperation? = EventDataOperation(context.applicationContext)
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
    fun deleteAllEvents() = DBQueue.get().async {
        mOperation?.deleteAllEventData()
    }

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
    fun cleanupBatchEvents(eventSyns: List<String>) =
        DBQueue.get().postTaskAsync {
            mOperation?.deleteBatchEventByEventSyn(eventSyns)
        }

    /**
     * 从 Event 表中读取上报数据
     * @param limit 条数限制
     * @return 数据
     */
    fun readEventsDataFromDb(limit: Int) =
        DBQueue.get().postTaskAsync {
            mOperation?.queryData(limit)
        }

    /**
     *  acountId,自有用户系统id
     *
     * @return acountId
     */
    private var accountId: String = ""
        get() {
            if (field.isEmpty()) {
                field = getStringConfig(DataParams.CONFIG_ACCOUNT_ID)
            }
            return field
        }

    fun getAccountId(callback: AsyncGetDBData) {
        DBQueue.get().postTask {

            val value = accountId
            callback?.let {
                MainQueue.get().postTask {
                    it.onDataGet(value)
                }
            }
        }
    }

    fun setAccountId(value: String) {
        DBQueue.get().postTask {
            if (accountId == value) {
                return@postTask
            }
            accountId = value
            setStringConfig(DataParams.CONFIG_ACCOUNT_ID, value)
        }
    }

    /**
     * 是否上报数据，默认是
     */
    var enableUpload: Boolean
        get() = getBooleanConfig(DataParams.CONFIG_ENABLE_UPLOADS,true)
        set(value) = setBooleanConfig(DataParams.CONFIG_ENABLE_UPLOADS,value)

    /**
     * install 事件的插入数据库状态
     */
    var isAppInstallInserted: Boolean
        get() = getBooleanConfig(DataParams.CONFIG_APP_INSTALL_INSERT_STATE,false)
        set(value) = setBooleanConfig(DataParams.CONFIG_APP_INSTALL_INSERT_STATE, value)

    /**
     * 第一次 session_start 事件的插入数据库状态
     */
    var isFirstSessionStartInserted: Boolean
        get() = getBooleanConfig(DataParams.CONFIG_FIRST_SESSION_START_INSERT_STATE,false)
        set(value) = setBooleanConfig(DataParams.CONFIG_FIRST_SESSION_START_INSERT_STATE, value)

    /**
     * DataTower id
     */
    var dtId : String
        set(value) = setStringConfig(DataParams.CONFIG_DT_ID,value)
        get() = getStringConfig(DataParams.CONFIG_DT_ID)

    var latestNetTime :Long
        set(value) = setLongConfig(DataParams.LATEST_NET_TIME,value)
        get() = getLongConfig(DataParams.LATEST_NET_TIME,TimeCalibration.TIME_NOT_VERIFY_VALUE)

    var latestGapTime:Long
        set(value) = setLongConfig(DataParams.LATEST_GAP_TIME,value)
        get() = getLongConfig(DataParams.LATEST_GAP_TIME,TimeCalibration.TIME_NOT_VERIFY_VALUE)

    private fun getBooleanConfig(key: String,default:Boolean = true): Boolean{
        val values = mOperation?.queryConfig(key)
        return if (values != null && values.isNotEmpty()) {
            values == "true" || (values == "null" && default)
        } else default
    }

    private fun setBooleanConfig(
        key: String,
        value: Boolean
    ) {
        mOperation?.insertConfig(
            key,
            value.toString()
        )
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


    private fun getStringConfig(key: String): String{
        val values = mOperation?.queryConfig(key)
        val s = if (values != null && values.isNotEmpty() && values != "null") {
            values
        } else ""

        return s
    }

    private fun setStringConfig(
        key: String,
        value: String
    ) {
        mOperation?.insertConfig(
            key,
            value
        )
    }

    private fun getLongConfig(key: String,default:Long = 0L):Long {

        val value = mOperation?.queryConfig(key)
        try {
            if (value != null && value.isNotEmpty() && value != "null") {
                return  value.toLong()
            }
        } catch (e: NumberFormatException) {

        }
        return default
    }

    private fun setLongConfig(key:String,value: Long){
        mOperation?.insertConfig(key,value.toString())
    }

    companion object {
        private var instance: EventDateAdapter? = null
        internal fun getInstance(
            context: Context
        ): EventDateAdapter? {
            if (instance == null) {
                instance = EventDateAdapter(context)
            }
            return instance
        }

        internal fun getInstance(): EventDateAdapter? {
            checkNotNull(instance) { "Call ROIQuerySDK.init first" }
            return instance
        }
    }


}
