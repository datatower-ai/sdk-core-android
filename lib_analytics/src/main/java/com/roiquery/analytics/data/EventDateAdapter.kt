package com.roiquery.analytics.data

import android.content.Context
import com.roiquery.analytics.data.db.EventDataOperation
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
     * 从 Event 表中读取上报数据
     *
     * @param tableName 表名
     * @param limit 条数限制
     * @return 数据
     */
    fun generateDataString(limit: Int): Array<String>? {
        return mOperation?.queryData(mDbParams!!.eventUri, limit)
    }

    /**
     *  acountId,自有用户系统id
     *
     * @return acountId
     */
    var accountId: String
        get() {
            val values = mOperation?.queryConfig(DataParams.CONFIG_ACCOUNT_ID)
            return if (values != null && values.isNotEmpty() && values != "null") {
                values
            } else ""
        }
        set(value) {
            mOperation?.insertConfig(
                DataParams.CONFIG_ACCOUNT_ID,
                value
            )
        }


    /**
     *  oaid
     *
     * @return oaid
     */
    var oaid: String
        get() {
            val values = mOperation?.queryConfig(DataParams.CONFIG_OAID)
            return if (values != null && values.isNotEmpty() && values != "null") {
                values
            } else ""
        }
        set(value) {
            mOperation?.insertConfig(
                DataParams.CONFIG_OAID,
                value
            )
        }

    /**
     *  gaid
     *
     * @return gaid
     */
    var gaid: String
        get() {
            val values = mOperation?.queryConfig(DataParams.CONFIG_GAID)
            return if (values != null && values.isNotEmpty() && values != "null") {
                values
            } else ""
        }
        set(value) {
            mOperation?.insertConfig(
                DataParams.CONFIG_GAID,
                value
            )
        }


    /**
     * 是否上报数据，默认是
     */
    var enableUpload: Boolean
        get() {
            val values = mOperation?.queryConfig(DataParams.CONFIG_ENABLE_UPLOADS)
            return if (values != null && values.isNotEmpty()) {
                values == "true" || values == "null"
            } else true
        }
        set(value) {
            mOperation?.insertConfig(
                DataParams.CONFIG_ENABLE_UPLOADS,
                value.toString()
            )
        }

    /**
     * 是否采集数据，默认是
     */
    var enableTrack: Boolean
        get() {
            val values = mOperation?.queryConfig(DataParams.CONFIG_ENABLE_TRACK)
            return if (values != null && values.isNotEmpty()) {
                values == "true" || values == "null"
            } else true
        }
        set(value) {
            mOperation?.insertConfig(
                DataParams.CONFIG_ENABLE_TRACK,
                value.toString()
            )
        }


    /**
     * 是否首次打开
     */
    var isFirstOpen: Boolean
        get() {
            val values = mOperation?.queryConfig(DataParams.CONFIG_FIRST_OPEN)
            return if (values != null && values.isNotEmpty()) {
                values == "true" || values == "null"
            } else true
        }
        set(value) {
            mOperation?.insertConfig(
                DataParams.CONFIG_FIRST_OPEN,
                value.toString()
            )
        }

    /**
     * app 是否在后台
     */
    var isAppForeground: Boolean
        get() {
            val values = mOperation?.queryConfig(DataParams.CONFIG_IS_FOREGROUND)
            return if (values != null && values.isNotEmpty()) {
                values == "true" || values == "null"
            } else true
        }
        set(value) {
            mOperation?.insertConfig(
                DataParams.CONFIG_IS_FOREGROUND,
                value.toString()
            )
        }


    companion object {
        private var instance: EventDateAdapter? = null
        internal fun getInstance(
            context: Context, packageName: String,

            ): EventDateAdapter? {
            if (instance == null) {
                instance = EventDateAdapter(context, packageName)
            }
            return instance
        }

        internal fun getInstance(): EventDateAdapter? {
            checkNotNull(instance) { "Call ROIQuerySDK.init first" }
            return instance
        }
    }


}