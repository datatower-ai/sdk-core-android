package com.nodetower.analytics.data

import android.content.Context
import com.nodetower.base.utils.LogUtils
import org.json.JSONException
import org.json.JSONObject


class EventDateAdapter private constructor(
    context: Context,
    packageName: String,
) {
    private val mDbParams: DataParams? = DataParams.getInstance(packageName)
    private var mOperation: _EventDataOperation? = _EventDataOperation(context.applicationContext)

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
    fun commitLoginId(loginId: String?) {
        try {
            mOperation?.insertData(
                mDbParams!!.loginIdUri,
                JSONObject().put(DataParams.VALUE, loginId)
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
            val values = mOperation?.queryData(mDbParams?.loginIdUri, 1)
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
            mOperation?.insertData(
                mDbParams!!.oaidUri,
                JSONObject().put(DataParams.VALUE, oaid)
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
            val values = mOperation?.queryData(mDbParams?.oaidUri, 1)
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
            mOperation?.insertData(
                mDbParams?.gaidUri,
                JSONObject().put(DataParams.VALUE, gaid)
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
            val values = mOperation?.queryData(mDbParams?.gaidUri, 1)
            return if (values != null && values.isNotEmpty()) {
                values[0]
            } else ""
        }

    /**
     * 从 Event 表中读取上报数据
     *
     * @param tableName 表名
     * @param limit 条数限制
     * @return 数据
     */
    fun generateDataString(tableName: String?, limit: Int): Array<String>? {
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