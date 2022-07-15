package com.roiquery.analytics.data

import android.content.Context
import android.text.TextUtils
import com.roiquery.analytics.Constant
import com.roiquery.analytics.data.db.EventDataOperation
import org.json.JSONObject
import java.lang.NumberFormatException


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
     *  app 首次打开时间
     *
     * @return acountId
     */
    var firstOpenTime: Long
        get() = getLongConfig(DataParams.CONFIG_FIRST_OPEN_TIME)
        set(value) = setLongConfig(DataParams.CONFIG_FIRST_OPEN_TIME,value)

    /**
     *  acountId,自有用户系统id
     *
     * @return acountId
     */
    var accountId: String
        get() = getStringConfig(DataParams.CONFIG_ACCOUNT_ID)
        set(value) = setStringConfig(DataParams.CONFIG_ACCOUNT_ID,value)


    var cloudConfigAesKey: String
        get() = getStringConfig(DataParams.CLOUD_CONFIG_AES_KEY)
        set(value) = setStringConfig(DataParams.CLOUD_CONFIG_AES_KEY,value)

    /**
     *  服务器与本地时间差
     *
     * @return event_session
     */
    var timeOffset: String
        get() {
            val timeLocalOffset = getStringConfig(DataParams.TIME_SERVER_LOCAL_OFFSET)
            return if (TextUtils.isEmpty(timeLocalOffset)) Constant.TIME_OFFSET_DEFAULT_VALUE else timeLocalOffset
        }
        set(value) = setStringConfig(DataParams.TIME_SERVER_LOCAL_OFFSET,value)

    /**
     *  app_engagement 最后发生时间
     *
     * @return event_session
     */
    var lastEngagementTime: String
        get() = getStringConfig(DataParams.LAST_APP_ENGAGEMENT_TIME)
        set(value) = setStringConfig(DataParams.LAST_APP_ENGAGEMENT_TIME,value)

    /**
     *  event_session
     *
     * @return event_session
     */
    var eventSession: String
        get() = getStringConfig(DataParams.CONFIG_EVENT_SESSION)
        set(value) = setStringConfig(DataParams.CONFIG_EVENT_SESSION,value)

    /**
     *  ROIQuery id
     *
     * @return rqid
     */
    var rqid: String
        get() = getStringConfig(DataParams.CONFIG_ROIQUERY_ID)
        set(value) = setStringConfig(DataParams.CONFIG_ROIQUERY_ID,value)

    /**
     *  firebase app_instance_id
     *
     * @return fiid
     */
    var fiid: String
        get() = getStringConfig(DataParams.CONFIG_FIREBASE_IID)
        set(value) = setStringConfig(DataParams.CONFIG_FIREBASE_IID,value)


    /**
     *  firebase fcm_token
     *
     * @return fcm_token
     */
    var fcmToken: String
        get() = getStringConfig(DataParams.CONFIG_FCM_TOKEN)
        set(value) = setStringConfig(DataParams.CONFIG_FCM_TOKEN,value)


    /**
     *  AppsFlyers id
     *
     * @return afid
     */
    var afid: String
        get() = getStringConfig(DataParams.CONFIG_APPSFLYER_ID)
        set(value) = setStringConfig(DataParams.CONFIG_APPSFLYER_ID,value)


    /**
     *  kochava id
     *
     * @return koid
     */
    var koid: String
        get() = getStringConfig(DataParams.CONFIG_KOCHAVA_ID)
        set(value) = setStringConfig(DataParams.CONFIG_KOCHAVA_ID,value)

    /**
     *  developerOwen id
     *
     * @return developerOwenId
     */
    var developerOwenId: String
        get() = getStringConfig(DataParams.CONFIG_DEVELOPER_OWNED_ID)
        set(value) = setStringConfig(DataParams.CONFIG_DEVELOPER_OWNED_ID,value)
    /**
     *  oaid
     *
     * @return oaid
     */
    var oaid: String
        get() = getStringConfig(DataParams.CONFIG_OAID)
        set(value) = setStringConfig(DataParams.CONFIG_OAID,value)

    /**
     *  gaid
     *
     * @return gaid
     */
    var gaid: String
        get() = getStringConfig(DataParams.CONFIG_GAID)
        set(value) = setStringConfig(DataParams.CONFIG_GAID,value)

    /**
     * 是否上报数据，默认是
     */
    var enableUpload: Boolean
        get() = getBooleanConfig(DataParams.CONFIG_ENABLE_UPLOADS)
        set(value) = setBooleanConfig(DataParams.CONFIG_ENABLE_UPLOADS,value)


    /**
     * 是否采集数据，默认是
     */
    var enableTrack: Boolean
        get() = getBooleanConfig(DataParams.CONFIG_ENABLE_TRACK)
        set(value) = setBooleanConfig(DataParams.CONFIG_ENABLE_TRACK,value)


    /**
     * 是否首次打开
     */
    var isFirstOpen: Boolean
        get() = getBooleanConfig(DataParams.CONFIG_FIRST_OPEN)
        set(value) = setBooleanConfig(DataParams.CONFIG_FIRST_OPEN,value)



    /**
     * 上报了attribute事件的个数
     */
    var attributedCount: Int
        get() = getIntConfig(DataParams.CONFIG_ATTRIBUTE_COUNT,0)
        set(value) = setIntConfig(DataParams.CONFIG_ATTRIBUTE_COUNT,value)


    /**
     * app 是否在后台
     */
    var isAppForeground: Boolean
        get() = getBooleanConfig(DataParams.CONFIG_IS_FOREGROUND)
        set(value) = setBooleanConfig(DataParams.CONFIG_IS_FOREGROUND,value)


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


    private fun getIntConfig(key: String,default: Int = 0): Int{
        val values = mOperation?.queryConfig(key)
        return if (values != null && values.isNotEmpty() && values != "null") {
            values.toInt()
        } else default
    }

    private fun setIntConfig(
        key: String,
        value: Int
    ) {
        mOperation?.insertConfig(
            key,
            value.toString()
        )
    }


    private fun getStringConfig(key: String): String{
        val values = mOperation?.queryConfig(key)
        return if (values != null && values.isNotEmpty() && values != "null") {
            values
        } else ""
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

    private fun getLongConfig(key: String):Long {
        val value = mOperation?.queryConfig(key)
        var longValue = 0L
        try {
            if (value != null && value.isNotEmpty() && value != "null") {
                longValue = value.toLong()
            }
        } catch (e: NumberFormatException) {
        }
        return longValue
    }

    private fun setLongConfig(key:String,value: Long){
        mOperation?.insertConfig(key,value.toString())
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