package com.roiquery.analytics.data

import android.content.Context
import android.text.TextUtils
import com.roiquery.analytics.Constant
import com.roiquery.analytics.ROIQueryCoroutineScope
import kotlinx.coroutines.*
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class EventDateAdapter private constructor(
    context: Context,
): ROIQueryCoroutineScope()  {
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
    suspend fun addJSON(data: JSONObject?, eventSyn: String)= coroutineScope {
        suspendCoroutine<Int> {
            scope.launch {
                try {
                    val code = mOperation?.insertData(data, eventSyn)!!
                    it.resume(code)
                } catch (e:Exception){
                    it.resume(DataParams.DB_ADD_JSON_ERROR)
                }
            }
        }
    }

    /**
     * Removes all events from table
     */
    fun deleteAllEvents() {
        mOperation?.deleteAllEventData()
    }

    /**
     * Removes events with an _id &lt;= last_id from table
     *
     * @param eventSyn the last id to delete
     * @return the number of rows in the table
     */
    fun cleanupEvents(eventSyn: String?) {
        scope.launch(Dispatchers.IO) {
            eventSyn?.let { mOperation?.deleteEventByEventSyn(it) }
        }
    }


    /**
     * 从 Event 表中读取上报数据
     * @param limit 条数限制
     * @return 数据
     */
    fun generateDataString(limit: Int):String?= runBlocking{
        mOperation?.queryData(limit)}


    /**
     *  acountId,自有用户系统id
     *
     * @return acountId
     */
    var accountId: String
        get() = runBlocking{ getStringConfig(DataParams.CONFIG_ACCOUNT_ID) }
        set(value) = setStringConfig(DataParams.CONFIG_ACCOUNT_ID,value)


    var cloudConfigAesKey: String
        get() = runBlocking{ getStringConfig(DataParams.CLOUD_CONFIG_AES_KEY) }
        set(value) = setStringConfig(DataParams.CLOUD_CONFIG_AES_KEY,value)

    /**
     *  ROIQuery id
     *
     * @return rqid
     */
    var rqid: String
        get() = runBlocking{ getStringConfig(DataParams.CONFIG_ROIQUERY_ID) }
        set(value) = setStringConfig(DataParams.CONFIG_ROIQUERY_ID,value)

    /**
     *  firebase app_instance_id
     *
     * @return fiid
     */
    var fiid: String
        get() = runBlocking{ getStringConfig(DataParams.CONFIG_FIREBASE_IID) }
        set(value) = setStringConfig(DataParams.CONFIG_FIREBASE_IID,value)


    /**
     *  firebase fcm_token
     *
     * @return fcm_token
     */
    var fcmToken: String
        get() = runBlocking{ getStringConfig(DataParams.CONFIG_FCM_TOKEN) }
        set(value) = setStringConfig(DataParams.CONFIG_FCM_TOKEN,value)


    /**
     *  AppsFlyers id
     *
     * @return afid
     */
    var afid: String
        get() = runBlocking{ getStringConfig(DataParams.CONFIG_APPSFLYER_ID) }
        set(value) = setStringConfig(DataParams.CONFIG_APPSFLYER_ID,value)


    /**
     *  kochava id
     *
     * @return koid
     */
    var koid: String
        get() = runBlocking{ getStringConfig(DataParams.CONFIG_KOCHAVA_ID) }
        set(value) = setStringConfig(DataParams.CONFIG_KOCHAVA_ID,value)

    /**
     *  appSet id
     *
     * @return appSetId
     */
    var appSetId: String
        get() = runBlocking{ getStringConfig(DataParams.CONFIG_APP_SET_ID) }
        set(value) = setStringConfig(DataParams.CONFIG_APP_SET_ID,value)
    /**
     *  oaid
     *
     * @return oaid
     */
    var oaid: String
        get() = runBlocking{ getStringConfig(DataParams.CONFIG_OAID) }
        set(value) = setStringConfig(DataParams.CONFIG_OAID,value)

    /**
     *  gaid
     *
     * @return gaid
     */
    var gaid: String
        set(value) = setStringConfig(DataParams.CONFIG_GAID,value)
        get() = runBlocking {   getStringConfig(DataParams.CONFIG_GAID)  }


    /**
     * 是否上报数据，默认是
     */
    var enableUpload: Boolean
        get() = runBlocking{ getBooleanConfig(DataParams.CONFIG_ENABLE_UPLOADS)}
        set(value) = setBooleanConfig(DataParams.CONFIG_ENABLE_UPLOADS,value)


    /**
     * 是否采集数据，默认是
     */
    var enableTrack: Boolean
        get() = runBlocking{ getBooleanConfig(DataParams.CONFIG_ENABLE_TRACK)}
        set(value) = setBooleanConfig(DataParams.CONFIG_ENABLE_TRACK,value)


    /**
     * 是否首次打开
     */
    var isFirstOpen: Boolean
        get() = runBlocking{ getBooleanConfig(DataParams.CONFIG_FIRST_OPEN) }
        set(value) = setBooleanConfig(DataParams.CONFIG_FIRST_OPEN,value)


    /**
     * app 是否在后台
     */
    var isAppForeground: Boolean
        get() = runBlocking{ getBooleanConfig(DataParams.CONFIG_IS_FOREGROUND)}
        set(value) = setBooleanConfig(DataParams.CONFIG_IS_FOREGROUND,value)

    var dtId : String
        set(value) = setStringConfig(DataParams.CONFIG_DT_ID,value)
        get() = runBlocking {   getStringConfig(DataParams.CONFIG_DT_ID)  }


    private suspend fun getBooleanConfig(key: String,default:Boolean = true): Boolean{
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


    private suspend fun getStringConfig(key: String): String{
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

    private suspend fun getLongConfig(key: String):Long {
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