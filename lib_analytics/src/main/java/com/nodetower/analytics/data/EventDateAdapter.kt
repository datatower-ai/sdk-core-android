package com.nodetower.analytics.data

import android.content.Context
import com.nodetower.analytics.data.db.EventDataOperation
import com.nodetower.base.utils.LogUtils
import org.json.JSONException
import org.json.JSONObject


class EventDateAdapter private constructor(
    context: Context,
    packageName: String,
) {
    private val mDbParams: DataParams? = DataParams.getInstance(packageName)
    private var mOperation: EventDataOperation? = EventDataOperation(context.applicationContext)

    /**
     * Adds a JSON string representing an event with properties or a person record
     * to the SQLiteDatabase.
     *
     * @param j the JSON to record
     * @return the number of rows in the table, or DB_OUT_OF_MEMORY_ERROR/DB_UPDATE_ERROR
     * on failure
     */
    fun addJSON(j: JSONObject?): Int {
        val code = mOperation?.insertData(mDbParams?.eventUri, j)!!
        return if (code == 0) {
            mOperation!!.queryDataCount(mDbParams?.eventUri)
        } else code
    }

    /**
     * Removes all events from table
     */
    fun deleteAllEvents() {
        mOperation?.deleteData(mDbParams?.eventUri, DataParams.DB_DELETE_ALL)
    }

    /**
     * Removes events with an _id &lt;= last_id from table
     *
     * @param last_id the last id to delete
     * @return the number of rows in the table
     */
    fun cleanupEvents(last_id: String?): Int {
        mOperation?.deleteData(mDbParams!!.eventUri, last_id!!)
        return mOperation!!.queryDataCount(mDbParams?.eventUri)
    }


    /**
     * 存储 LoginId
     *
     * @param loginId 登录 Id
     */
    fun commitAccountId(loginId: String?) {
        try {
            mOperation?.insertConfig(
                DataParams.CONFIG_ACCOUNT_ID,
                loginId
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
    val accountId: String
        get() {
            val values = mOperation?.queryConfig(DataParams.CONFIG_ACCOUNT_ID)
            return if (values != null && values.isNotEmpty() && values != "null") {
                values
            } else ""
        }

    /**
     * 存储 oaid
     *
     * @param oaid 登录 oaid
     */
    fun commitOaid(oaid: String?) {
        try {
            mOperation?.insertConfig(
                DataParams.CONFIG_OAID,
                oaid
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
            val values = mOperation?.queryConfig(DataParams.CONFIG_OAID)
            return if (values != null && values.isNotEmpty() && values != "null") {
                values
            } else ""
        }

    /**
     * 存储 gaid
     *
     * @param gaid
     */
    fun commitGaid(gaid: String?) {
        try {
            mOperation?.insertConfig(
                DataParams.CONFIG_GAID,
                gaid
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
            val values = mOperation?.queryConfig(DataParams.CONFIG_GAID)
            return if (values != null && values.isNotEmpty() && values != "null") {
                values
            } else ""
        }

    /**
     * 存储 上报数据进程名
     *
     * @param name
     */
    fun commitEnableUpload(enable: Boolean?) {
        try {
            mOperation?.insertConfig(
                DataParams.CONFIG_ENABLE_UPLOADS,
                enable.toString()
            )
        } catch (e: JSONException) {
            LogUtils.printStackTrace(e)
        }
    }

    /**
     * 可否上报
     *
     * @return 上报数据进程名
     */
    fun enableUpload(): Boolean {
        val values = mOperation?.queryConfig(DataParams.CONFIG_ENABLE_UPLOADS)
        return if (values != null && values.isNotEmpty()) {
            values == "true" || values == "null"
        } else true
    }


    fun commitFirstOpen(enable: Boolean?) {
        try {
            mOperation?.insertConfig(
                DataParams.CONFIG_FIRST_OPEN,
                enable.toString()
            )
        } catch (e: JSONException) {
            LogUtils.printStackTrace(e)
        }
    }


    fun isFirstOpen(): Boolean {
        val values = mOperation?.queryConfig(DataParams.CONFIG_FIRST_OPEN)
        return if (values != null && values.isNotEmpty()) {
            values == "true" || values == "null"
        } else true
    }

    /**
     * 从 Event 表中读取上报数据
     *
     * @param tableName 表名
     * @param limit 条数限制
     * @return 数据
     */
    fun generateDataString(limit: Int): Array<String>? {
        return mOperation?.queryData(mDbParams!!.eventUri, limit)
    }

    companion object {
        private var instance: EventDateAdapter? = null
        fun getInstance(
            context: Context, packageName: String,

            ): EventDateAdapter? {
            if (instance == null) {
                instance = EventDateAdapter(context, packageName)
            }
            return instance
        }

        fun getInstance(): EventDateAdapter? {
            checkNotNull(instance) { "The static method getInstance(Context context, String packageName) should be called before calling getInstance()" }
            return instance
        }
    }


}