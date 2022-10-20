package com.roiquery.analytics.api

import android.content.Context
import android.util.Log
import com.github.gzuliyujiang.oaid.DeviceID
import com.github.gzuliyujiang.oaid.IGetter
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.roiquery.analytics.Constant
import com.roiquery.analytics.ROIQueryAnalytics.Companion.userSet
import com.roiquery.analytics.ROIQueryCoroutineScope
import com.roiquery.analytics.config.AnalyticsConfig
import com.roiquery.analytics.data.EventDateAdapter
import com.roiquery.analytics.taskscheduler.TaskScheduler
import com.roiquery.analytics.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.MessageDigest

class PropertyManager private constructor() : ROIQueryCoroutineScope() {
    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            PropertyManager()
        }
    }

    private var mContext: Context? = null

    // 事件信息，包含事件的基本数据
    private var mEventInfo: MutableMap<String, Any?>? = null

    // 事件通用属性
    private var mCommonProperties: MutableMap<String, Any?>? = null

    //本地数据适配器，包括sp、db的操作
    private var mDataAdapter: EventDateAdapter? = null

    fun init(context: Context?, dateAdapter: EventDateAdapter?, initConfig: AnalyticsConfig?) {
        mContext = context
        mDataAdapter = dateAdapter
        initEventInfo()
        initCommonProperties(initConfig)
        getOAID()
        scope.launch {
            getGAID()
            initDTId()
        }
    }

    private fun initDTId() {
        if (mDataAdapter?.dtId?.isNotEmpty()==true){
            return
        }
        val appId = AbstractAnalytics.mConfigOptions?.mAppId
        val gaid: String? =
            if (mDataAdapter?.gaid?.isEmpty() == true) mEventInfo?.get(Constant.EVENT_INFO_GAID) as String? else mDataAdapter?.gaid
        val androidId: String? = mContext?.let { DeviceUtils.getAndroidID(it) }
        val dtIdOriginal = (gaid ?: androidId).plus("+$appId")
        val dtId = DataEncryption.instance.str2Sha1Str(dtIdOriginal)
        updateEventInfo(
            Constant.EVENT_INFO_DT_ID, dtId
        )
        if (dtId.isNotEmpty()){
            mDataAdapter?.dtId = dtId
        }
    }


    /**
     * 获取并配置 事件一些基本属性
     *
     * @return
     */
    private fun initEventInfo() {
        mEventInfo = EventUtils.getEventInfo(mContext!!, mDataAdapter)
    }


    private fun updateEventInfo(key: String, value: String) {
        mEventInfo?.put(key, value)
    }

    fun getEventInfo() = mEventInfo?.toMutableMap() ?: mutableMapOf()

    /**
     * 获取并配置 事件通用属性
     *
     * @return
     */
    private fun initCommonProperties(initConfig: AnalyticsConfig?) {
        mCommonProperties = EventUtils.getCommonProperties(mContext!!, mDataAdapter)
        initConfig?.let { config ->
            if (config.mCommonProperties != null) {
                val iterator = config.mCommonProperties!!.keys()
                while (iterator.hasNext()) {
                    val key = iterator.next()
                    try {
                        val value = config.mCommonProperties!![key]
                        updateCommonProperties(key, value.toString())
                    } catch (e: Exception) {
                        LogUtils.printStackTrace(e)
                    }
                }
            }
        }
    }


    private fun updateCommonProperties(key: String, value: String) {
        mCommonProperties?.put(key, value)
    }

    fun getCommonProperties() = mCommonProperties?.toMutableMap() ?: mutableMapOf()


    /**
     * oaid 获取，异步
     */
    private fun getOAID() {
        if (mDataAdapter?.oaid?.isNotEmpty() == true || !DeviceID.supportedOAID(mContext)) {
            return
        }
        TaskScheduler.execute {
            try {
                DeviceID.getOAID(mContext, object : IGetter {
                    override fun onOAIDGetComplete(oaid: String) {
                        mDataAdapter?.oaid = oaid
                        updateEventInfo(Constant.EVENT_INFO_OAID, oaid)
                        Thread.sleep(2000)
                        AnalyticsImp.getInstance(mContext)
                            .trackUser(Constant.PRESET_EVENT_USER_SET, JSONObject().apply {
                                put(Constant.USER_PROPERTY_LATEST_OAID, oaid)
                            })
                    }

                    override fun onOAIDGetError(exception: java.lang.Exception) {
                        LogUtils.d(exception)
                    }
                })
            } catch (e: Exception) {
                LogUtils.d(e)
            }
        }
    }

    /**
     * gaid 获取，异步
     */
    private suspend fun getGAID() {
        if (mDataAdapter?.gaid?.isNotEmpty() == true) {
            return
        }
        withContext(Dispatchers.IO) {
            try {
                val info = AdvertisingIdClient.getAdvertisingIdInfo(mContext!!)
                val id = info.id ?: ""
                mDataAdapter?.gaid = id
                updateEventInfo(Constant.EVENT_INFO_GAID, id)
                Thread.sleep(2000)
                AnalyticsImp.getInstance(mContext)
                    .trackUser(Constant.PRESET_EVENT_USER_SET, JSONObject().apply {
                        put(Constant.USER_PROPERTY_LATEST_GAID, id)
                    })
            } catch (exception: Exception) {
                LogUtils.d("getGAID", "onException:" + exception.message.toString())
            }
        }
    }


    fun updateSdkVersionProperty(jsonObject: JSONObject, typeKye: String, versionKey: String) {
        //接入 SDK 的类型可能是 Android 或 Unity ，因此这里需动态获取
        getCommonProperties()[Constant.COMMON_PROPERTY_SDK_TYPE]?.toString()?.let {
            if (it.isNotEmpty()) {
                jsonObject.put(
                    Constant.USER_PROPERTY_ACTIVE_SDK_TYPE,
                    it
                )
            }
        }
        //SDK 版本
        getCommonProperties()[Constant.COMMON_PROPERTY_SDK_VERSION]?.toString()?.let {
            if (it.isNotEmpty()) {
                jsonObject.put(
                    Constant.USER_PROPERTY_ACTIVE_SDK_VERSION,
                    it
                )
            }
        }
    }

    fun updateNetworkType(networkType: NetworkUtil.NetworkType?) {
        updateCommonProperties(
            Constant.COMMON_PROPERTY_NETWORK_TYPE,
            NetworkUtil.convertNetworkTypeToString(networkType)
        )
    }

    fun updateACID(acid: String) {
        updateEventInfo(Constant.EVENT_INFO_ACID, acid)
    }

    fun updateFireBaseInstanceId(fiid: String) {
        updateCommonProperties(Constant.COMMON_PROPERTY_FIREBASE_IID, fiid)
        userSet(JSONObject().apply {
            put(Constant.USER_PROPERTY_LATEST_FIREBASE_IID, fiid)
        })
    }

    fun updateFCMToken(fcmToken: String) {
        updateCommonProperties(Constant.COMMON_PROPERTY_FCM_TOKEN, fcmToken)
        userSet(JSONObject().apply {
            put(Constant.USER_PROPERTY_LATEST_FCM_TOKEN, fcmToken)
        })
    }

    fun updateAFID(afid: String) {
        updateCommonProperties(Constant.COMMON_PROPERTY_APPSFLYER_ID, afid)
        userSet(JSONObject().apply {
            put(Constant.USER_PROPERTY_LATEST_APPSFLYER_ID, afid)
        })
    }

    fun updateKOID(koid: String) {
        updateCommonProperties(Constant.COMMON_PROPERTY_KOCHAVA_ID, koid)
        userSet(JSONObject().apply {
            put(Constant.USER_PROPERTY_LATEST_KOCHAVA_ID, koid)
        })
    }

    fun updateAppSetId(appSetId: String) {
        updateCommonProperties(Constant.COMMON_PROPERTY_APP_SET_ID, appSetId)
        userSet(JSONObject().apply {
            put(Constant.USER_PROPERTY_LATEST_APP_SET_ID, appSetId)
        })
    }

}