package com.nodetower.analytics.data

import android.content.ContentValues
import android.content.Context
import com.nodetower.analytics.data.db.EventDataOperation
import com.nodetower.analytics.data.persistent.PersistentDataOperation
import com.nodetower.base.utils.LogUtils
import org.json.JSONException
import org.json.JSONObject


class DbAdapter private constructor(
    context: Context,
    packageName: String,
) {
    private val mDbParams: DbParams? = DbParams.getInstance(packageName)
    private var mTrackEventOperation: DataOperation? = null
    private var mPersistentOperation: DataOperation? = null

    /**
     * Adds a JSON string representing an event with properties or a person record
     * to the SQLiteDatabase.
     *
     * @param j the JSON to record
     * @return the number of rows in the table, or DB_OUT_OF_MEMORY_ERROR/DB_UPDATE_ERROR
     * on failure
     */
    fun addJSON(j: JSONObject?): Int {
        val code = mTrackEventOperation?.insertData(mDbParams?.eventUri, j)!!
        return if (code == 0) {
            mTrackEventOperation!!.queryDataCount(mDbParams?.eventUri)
        } else code
    }

    /**
     * Removes all events from table
     */
    fun deleteAllEvents() {
        mTrackEventOperation?.deleteData(mDbParams?.eventUri, DbParams.DB_DELETE_ALL)
    }

    /**
     * Removes events with an _id &lt;= last_id from table
     *
     * @param last_id the last id to delete
     * @return the number of rows in the table
     */
    fun cleanupEvents(last_id: String?): Int {
        mTrackEventOperation?.deleteData(mDbParams!!.eventUri, last_id!!)
        return mTrackEventOperation!!.queryDataCount(mDbParams?.eventUri)
    }


    /**
     * 设置 Activity Start 的时间戳
     *
     * @param appStartTime Activity Start 的时间戳
     */
    fun commitAppStartTime(appStartTime: Long) {
        try {
            mPersistentOperation?.insertData(
                mDbParams!!.appStartTimeUri,
                JSONObject().put(DbParams.VALUE, appStartTime)
            )
        } catch (e: JSONException) {
            LogUtils.printStackTrace(e)
        }
    }

    /**
     * 获取 Activity Start 的时间戳
     *
     * @return Activity Start 的时间戳
     */
    val appStartTime: Long
        get() {
            try {
                val values = mPersistentOperation?.queryData(
                    mDbParams!!.appStartTimeUri, 1
                )
                if (values != null && values.isNotEmpty()) {
                    return values[0].toLong()
                }
            } catch (ex: Exception) {
                LogUtils.printStackTrace(ex)
            }
            return 0
        }

    /**
     * 设置 Activity Pause 的时间戳
     *
     * @param appPausedTime Activity Pause 的时间戳
     */
    fun commitAppEndTime(appPausedTime: Long) {
        try {
            mPersistentOperation?.insertData(
                mDbParams!!.appPausedUri,
                JSONObject().put(DbParams.VALUE, appPausedTime)
            )
        } catch (e: JSONException) {
            LogUtils.printStackTrace(e)
        }
    }

    /**
     * 获取 Activity Pause 的时间戳
     *
     * @return Activity Pause 的时间戳
     */
    val appEndTime: Long
        get() {
            try {
                val values = mPersistentOperation?.queryData(
                    mDbParams!!.appPausedUri, 1
                )
                if (values != null && values.isNotEmpty()) {
                    return values[0].toLong()
                }
            } catch (ex: Exception) {
                LogUtils.printStackTrace(ex)
            }
            return 0
        }

    /**
     * 设置 Activity End 的信息
     *
     * @param appEndData Activity End 的信息
     */
    fun commitAppEndData(appEndData: String?) {
        try {
            mPersistentOperation?.insertData(
                mDbParams!!.appEndDataUri,
                JSONObject().put(DbParams.VALUE, appEndData)
            )
        } catch (e: JSONException) {
            LogUtils.printStackTrace(e)
        }
    }

    /**
     * 获取 Activity End 的信息
     *
     * @return Activity End 的信息
     */
    val appEndData: String
        get() {
            val values = mPersistentOperation?.queryData(mDbParams!!.appEndDataUri, 1)
            return if (values != null && values.isNotEmpty()) {
                values[0]
            } else ""
        }

    /**
     * 存储 LoginId
     *
     * @param loginId 登录 Id
     */
    fun commitLoginId(loginId: String?) {
        try {
            mPersistentOperation?.insertData(
                mDbParams!!.loginIdUri,
                JSONObject().put(DbParams.VALUE, loginId)
            )
        } catch (e: JSONException) {
            LogUtils.printStackTrace(e)
        }
    }

    /**
     * 获取 LoginId
     *
     * @return LoginId
     */
    val loginId: String
        get() {
            val values = mPersistentOperation?.queryData(mDbParams?.loginIdUri, 1)
            return if (values != null && values.isNotEmpty()) {
                values[0]
            } else ""
        }
    /**
     * 存储 oaid
     *
     * @param oaid 登录 oaid
     */
    fun commitOaid(oaid: String?) {
        try {
            mPersistentOperation?.insertData(
                mDbParams!!.oaidUri,
                JSONObject().put(DbParams.VALUE, oaid)
            )
        } catch (e: JSONException) {
            LogUtils.printStackTrace(e)
        }
    }

    /**
     * 获取 oaid
     *
     * @return oaid
     */
    val oaid: String
        get() {
            val values = mPersistentOperation?.queryData(mDbParams?.oaidUri, 1)
            return if (values != null && values.isNotEmpty()) {
                values[0]
            } else ""
        }

    /**
     * 存储 gaid
     *
     * @param gaid
     */
    fun commitGaid(gaid: String?) {
        try {
            mPersistentOperation?.insertData(
                mDbParams!!.gaidUri,
                JSONObject().put(DbParams.VALUE, gaid)
            )
        } catch (e: JSONException) {
            LogUtils.printStackTrace(e)
        }
    }

    /**
     * 获取 gaid
     *
     * @return gaid
     */
    val gaid: String
        get() {
            val values = mPersistentOperation?.queryData(mDbParams?.gaidUri, 1)
            return if (values != null && values.isNotEmpty()) {
                values[0]
            } else ""
        }

    /**
     * 设置 Session 的时长
     *
     * @param sessionIntervalTime Session 的时长
     */
    fun commitSessionIntervalTime(sessionIntervalTime: Int) {
        try {
            mPersistentOperation?.insertData(
                mDbParams!!.sessionTimeUri,
                JSONObject().put(DbParams.VALUE, sessionIntervalTime)
            )
        } catch (e: JSONException) {
            LogUtils.printStackTrace(e)
        }
    }

    /**
     * 获取 Session 的时长
     *
     * @return Session 的时长
     */
    val sessionIntervalTime: Int
        get() {
            try {
                val values = mPersistentOperation?.queryData(
                    mDbParams?.sessionTimeUri, 1
                )
                if (values != null && values.isNotEmpty()) {
                    return values[0].toInt()
                }
            } catch (ex: Exception) {
                LogUtils.printStackTrace(ex)
            }
            return 0
        }

    /**
     * 查询表中是否有对应的事件
     *
     * @param eventName 事件名
     * @return false 表示已存在，true 表示不存在，是首次
     */
    fun isFirstChannelEvent(eventName: String?): Boolean {
        return mTrackEventOperation!!.queryDataCount(
            mDbParams?.channelPersistentUri,
            null,
            DbParams.KEY_CHANNEL_EVENT_NAME + " = ? ",
            arrayOf(eventName),
            null
        ) <= 0
    }

    /**
     * 添加渠道事件
     *
     * @param eventName 事件名
     */
    fun addChannelEvent(eventName: String?) {
        val values = ContentValues()
        values.put(DbParams.KEY_CHANNEL_EVENT_NAME, eventName)
        values.put(DbParams.KEY_CHANNEL_RESULT, true)
        mTrackEventOperation!!.insertData(mDbParams?.channelPersistentUri, values)
    }

    /**
     * 保存子进程上报数据的状态
     *
     * @param flushState 上报状态
     */
    fun commitSubProcessFlushState(flushState: Boolean) {
        try {
            mPersistentOperation?.insertData(
                mDbParams!!.subProcessUri,
                JSONObject().put(DbParams.VALUE, flushState)
            )
        } catch (e: JSONException) {
            LogUtils.printStackTrace(e)
        }
    }

    /**
     * 获取子进程上报数据状态
     *
     * @return 上报状态
     */
    val isSubProcessFlushing: Boolean
        get() {
            try {
                val values = mPersistentOperation?.queryData(
                    mDbParams!!.subProcessUri, 1
                )
                if (values != null && values.isNotEmpty()) {
                    return values[0].toInt() == 1
                }
            } catch (ex: Exception) {
                LogUtils.printStackTrace(ex)
            }
            return true
        }

    /**
     * 保存首个启动进程的标记
     *
     * @param isFirst 是否首个进程
     */
    fun commitFirstProcessState(isFirst: Boolean) {
        try {
            mPersistentOperation?.insertData(
                mDbParams!!.firstProcessUri,
                JSONObject().put(DbParams.VALUE, isFirst)
            )
        } catch (e: JSONException) {
            LogUtils.printStackTrace(e)
        }
    }

    /**
     * 获取是否首个启动进程的标记
     *
     * @return 是否首个进程
     */
    val isFirstProcess: Boolean
        get() {
            try {
                val values = mPersistentOperation?.queryData(
                    mDbParams!!.firstProcessUri, 1
                )
                if (values != null && values.isNotEmpty()) {
                    return values[0].toInt() == 1
                }
            } catch (ex: Exception) {
                LogUtils.printStackTrace(ex)
            }
            return true
        }

    /**
     * 从 Event 表中读取上报数据
     *
     * @param tableName 表名
     * @param limit 条数限制
     * @return 数据
     */
    fun generateDataString(tableName: String?, limit: Int): Array<String>? {
        return mTrackEventOperation?.queryData(mDbParams!!.eventUri, limit)
    }

    companion object {
        private var instance: DbAdapter? = null
        fun getInstance(
            context: Context, packageName: String,

        ): DbAdapter? {
            if (instance == null) {
                instance = DbAdapter(context, packageName)
            }
            return instance
        }

        fun getInstance(): DbAdapter? {
            checkNotNull(instance) { "The static method getInstance(Context context, String packageName) should be called before calling getInstance()" }
            return instance
        }
    }

    init {
        mTrackEventOperation = EventDataOperation(context.applicationContext)
        mPersistentOperation = PersistentDataOperation(context.applicationContext)
    }
}