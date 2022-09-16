package com.roiquery.ad.api


import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.text.TextUtils
import com.roiquery.ad.AdPlatform
import com.roiquery.ad.AdReportConstant
import com.roiquery.ad.AdReportConstant.PROPERTY_AD_SHOW_ERROR_CODE
import com.roiquery.ad.AdReportConstant.PROPERTY_AD_SHOW_ERROR_MESSAGE
import com.roiquery.ad.AdType
import com.roiquery.ad.utils.AdEventProperty
import com.roiquery.ad.utils.AdPlatformUtils
import com.roiquery.analytics.ROIQuery
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.analytics.utils.AppInfoUtils
import com.roiquery.analytics.utils.AppLifecycleHelper.OnAppStatusListener
import com.roiquery.analytics.utils.EventUtils
import com.roiquery.analytics.utils.LogUtils
import com.roiquery.analytics.utils.transJSONObject2Map
import org.json.JSONObject

class AdReportImp private constructor(context: Context?) : IAdReport {

    private var mContext: Context? = context
    private var mIsMainProcess: Boolean = true

    private var mSequenessMap: MutableMap<String, AdEventProperty?> = mutableMapOf()
    private val mMaxSequenessSize = 10

    override fun reportLoadBegin(
        id: String,
        type: Int,
        platform: Int,
        seq: String,
        properties: MutableMap<String, Any>?
    ) {
        updateAdEventProperty(id, type, platform, "", seq, properties, "") ?: return
        adTrack(
            AdReportConstant.EVENT_AD_LOAD_BEGIN,
            generateAdReportJson(seq)
        )
    }

    override fun reportLoadEnd(
        id: String,
        type: Int,
        platform: Int,
        duration: Long,
        result: Boolean,
        seq: String,
        errorCode: Int,
        errorMessage: String,
        properties: MutableMap<String, Any>?
    ) {
        updateAdEventProperty(id, type, platform, "", seq, properties, "") ?: return
        adTrack(
            AdReportConstant.EVENT_AD_LOAD_END,
            generateAdReportJson(seq).apply {
                put(AdReportConstant.PROPERTY_LOAD_DURATION, duration)
                put(AdReportConstant.PROPERTY_LOAD_RESULT, result)
                put(AdReportConstant.PROPERTY_ERROR_CODE, errorCode)
                put(AdReportConstant.PROPERTY_ERROR_MESSAGE, errorMessage)
            }
        )
    }


    override fun reportEntrance(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        properties: MutableMap<String, Any>?,
        entrance: String?
    ) {
//        updateAdEventProperty(id, type, platform, location, seq, properties, entrance) ?: return
//        adTrack(
//            AdReportConstant.EVENT_AD_ENTRANCE,
//            generateAdReportJson(seq)
//        )
    }

    override fun reportToShow(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        properties: MutableMap<String, Any>?,
        entrance: String?
    ) {
        updateAdEventProperty(id, type, platform, location, seq, properties, entrance) ?: return
        adTrack(
            AdReportConstant.EVENT_AD_TO_SHOW,
            generateAdReportJson(seq)
        )
    }

    override fun reportShow(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        properties: MutableMap<String, Any>?,
        entrance: String?
    ) {
        updateAdEventProperty(id, type, platform, location, seq, properties, entrance)?.apply {
            showTS = SystemClock.elapsedRealtime()
        } ?: return
        adTrack(
            AdReportConstant.EVENT_AD_SHOW,
            generateAdReportJson(seq)
        )
    }

    override fun reportShowFailed(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        errorCode: Int,
        errorMessage: String,
        properties: MutableMap<String, Any>?,
        entrance: String?
    ) {
        updateAdEventProperty(id, type, platform, location, seq, properties, entrance)?.apply {
            showTS = SystemClock.elapsedRealtime()
            this.properties?.set(PROPERTY_AD_SHOW_ERROR_CODE, errorCode)
            this.properties?.set(PROPERTY_AD_SHOW_ERROR_MESSAGE, errorMessage)
        } ?: return
        adTrack(
            AdReportConstant.EVENT_AD_SHOW_FAILED,
            generateAdReportJson(seq)
        )
    }

    override fun reportImpression(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        properties: MutableMap<String, Any>?,
        entrance: String?
    ) {
//        updateAdEventProperty(id, type, platform, location, seq, properties, entrance)?.apply {
//            showTS = SystemClock.elapsedRealtime()
//        } ?: return
//        adTrack(
//            AdReportConstant.EVENT_AD_IMPRESSION,
//            generateAdReportJson(seq)
//        )
    }

    override fun reportOpen(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        properties: MutableMap<String, Any>?,
        entrance: String?
    ) {
//        updateAdEventProperty(id, type, platform, location, seq, properties, entrance) ?: return
//        adTrack(
//            AdReportConstant.EVENT_AD_OPEN,
//            generateAdReportJson(seq)
//        )
    }

    override fun reportClose(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        properties: MutableMap<String, Any>?,
        entrance: String?,
    ) {
        updateAdEventProperty(id, type, platform, location, seq, properties, entrance) ?: return
        adTrack(
            AdReportConstant.EVENT_AD_CLOSE,
            generateAdReportJson(seq)
        )
    }

    override fun reportClick(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        properties: MutableMap<String, Any>?,
        entrance: String?,
    ) {
        updateAdEventProperty(id, type, platform, location, seq, properties, entrance)?.apply {
            clickTS = SystemClock.elapsedRealtime()
        } ?: return
        adTrack(
            AdReportConstant.EVENT_AD_CLICK,
            generateAdReportJson(seq)
        )
    }

    override fun reportRewarded(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        properties: MutableMap<String, Any>?,
        entrance: String?,
    ) {
        updateAdEventProperty(id, type, platform, location, seq, properties, entrance) ?: return
        adTrack(
            AdReportConstant.EVENT_AD_REWARDED,
            generateAdReportJson(seq)
        )
    }

    override fun reportConversion(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        conversionSource: String,
        properties: MutableMap<String, Any>?,
        entrance: String?
    ) {
        updateAdEventProperty(id, type, platform, location, seq, properties, entrance) ?: return
        adTrack(
            AdReportConstant.EVENT_AD_CONVERSION,
            generateAdReportJson(seq).apply {
                put(AdReportConstant.PROPERTY_AD_CONVERSION_SOURCE, conversionSource)
            }
        )
    }

    override fun reportLeftApp(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        properties: MutableMap<String, Any>?,
        entrance: String?,
    ) {
        updateAdEventProperty(id, type, platform, location, seq, properties, entrance)?.apply {
            leftApplicationTS = SystemClock.elapsedRealtime()
            isLeftApplication = true
        } ?: return
        adTrack(
            AdReportConstant.EVENT_AD_LEFT_APP,
            generateAdReportJson(seq)
        )
    }

    override fun reportPaid(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        value: String,
        currency: String,
        precision: String,
        properties: MutableMap<String, Any>?,
        entrance: String?
    ) {
        LogUtils.d(
            "reportPaid",
            "id: $id,\n" +
                    "        type: $type,\n" +
                    "        platform: $platform,\n" +
                    "        location: $location,\n" +
                    "        seq: $seq,\n" +
                    "        value: $value,\n" +
                    "        currency: $currency,\n" +
                    "        precision: $precision,\n" +
                    "        entrance: $entrance?"
        )
        val property =
            updateAdEventProperty(id, type, platform, location, seq, properties, entrance)?.apply {
                this.value = value
                this.currency = currency
                this.precision = precision
            } ?: return
        adTrack(
            AdReportConstant.EVENT_AD_PAID,
            generateAdReportJson(seq).apply {
                put(
                    AdReportConstant.PROPERTY_AD_MEDIAITON,
                    if (properties?.containsKey(AdReportConstant.PROPERTY_AD_MEDIAITON) == true) properties[AdReportConstant.PROPERTY_AD_MEDIAITON] else property.mediation
                )
                put(
                    AdReportConstant.PROPERTY_AD_MEDIAITON_ID,
                    if (properties?.containsKey(AdReportConstant.PROPERTY_AD_MEDIAITON_ID) == true) properties[AdReportConstant.PROPERTY_AD_MEDIAITON_ID] else property.mediationId
                )
                put(
                    AdReportConstant.PROPERTY_AD_VALUE_MICROS,
                    if (properties?.containsKey(AdReportConstant.PROPERTY_AD_VALUE_MICROS) == true) properties[AdReportConstant.PROPERTY_AD_VALUE_MICROS] else property.value
                )
                put(
                    AdReportConstant.PROPERTY_AD_CURRENCY_CODE,
                    if (properties?.containsKey(AdReportConstant.PROPERTY_AD_CURRENCY_CODE) == true) properties[AdReportConstant.PROPERTY_AD_CURRENCY_CODE] else property.currency
                )
                put(
                    AdReportConstant.PROPERTY_AD_PRECISION_TYPE,
                    if (properties?.containsKey(AdReportConstant.PROPERTY_AD_PRECISION_TYPE) == true) properties[AdReportConstant.PROPERTY_AD_PRECISION_TYPE] else property.precision
                )
                put(
                    AdReportConstant.PROPERTY_AD_COUNTRY,
                    if (properties?.containsKey(AdReportConstant.PROPERTY_AD_COUNTRY) == true) properties[AdReportConstant.PROPERTY_AD_COUNTRY] else property.country
                )
            }
        )
    }

    override fun reportPaid(
        id: String,
        type: Int,
        platform: String,
        adgroupName: String,
        adgroupType: String,
        location: String,
        seq: String,
        mediation: Int,
        mediationId: String,
        value: String,
        currency: String,
        precision: String,
        country: String,
        properties: MutableMap<String, Any>?,
        entrance: String?
    ) {
        LogUtils.d(
            "reportImpression",
            "id: $id,\n" +
                    "        type: $type,\n" +
                    "        platform: $platform,\n" +
                    "        adgroupType: $adgroupType,\n" +
                    "        location: $location,\n" +
                    "        seq: $seq,\n" +
                    "        mediation: $mediation,\n" +
                    "        mediationId: $mediationId,\n" +
                    "        value: $value,\n" +
                    "        currency: $currency,\n" +
                    "        precision: $precision,\n" +
                    "        country: $country,\n" +
                    "        entrance: $entrance?"
        )

        val property = updateAdEventProperty(
            AdPlatformUtils.getId(mediationId, id, adgroupType),
            type,
            AdPlatformUtils.getPlatform(mediation, platform, id, adgroupName, adgroupType).value,
            location,
            seq,
            properties,
            entrance
        )?.apply {
            this.mediation = mediation
            this.mediationId = mediationId
            this.value = value
            this.currency = currency
            this.precision = precision
            this.country = country
        } ?: return
        adTrack(
            AdReportConstant.EVENT_AD_PAID,
            generateAdReportJson(seq).apply {
                put(AdReportConstant.PROPERTY_AD_MEDIAITON, property.mediation)
                put(AdReportConstant.PROPERTY_AD_MEDIAITON_ID, property.mediationId)
                put(AdReportConstant.PROPERTY_AD_VALUE_MICROS, property.value)
                put(AdReportConstant.PROPERTY_AD_CURRENCY_CODE, property.currency)
                put(AdReportConstant.PROPERTY_AD_PRECISION_TYPE, property.precision)
                put(AdReportConstant.PROPERTY_AD_COUNTRY, property.country)
            }
        )

    }

    override fun reportPaid(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        mediation: Int,
        mediationId: String,
        value: String,
        precision: String,
        country: String,
        properties: MutableMap<String, Any>?
    ) {
        LogUtils.d(
            "reportPaid",
            "id: $id,\n" +
                    "        type: $type,\n" +
                    "        platform: $platform,\n" +
                    "        location: $location,\n" +
                    "        seq: $seq,\n" +
                    "        mediation: $mediation,\n" +
                    "        mediationId: $mediationId,\n" +
                    "        value: $value,\n" +
                    "        country: $country,\n" +
                    "        precision: $precision,\n"

        )
        val property = updateAdEventProperty(id, type, platform, location, seq, properties)?.apply {
            this.value = value
            this.precision = precision
            this.country = country
            this.mediation = mediation
            this.mediationId = mediationId
        } ?: return
        adTrack(
            AdReportConstant.EVENT_AD_PAID,
            generateAdReportJson(seq).apply {
                put(AdReportConstant.PROPERTY_AD_MEDIAITON, property.mediation)
                put(AdReportConstant.PROPERTY_AD_MEDIAITON_ID, property.mediationId)
                put(AdReportConstant.PROPERTY_AD_VALUE_MICROS, property.value)
                put(AdReportConstant.PROPERTY_AD_PRECISION_TYPE, property.precision)
                put(AdReportConstant.PROPERTY_AD_COUNTRY, property.country)
            }
        )
    }


    override fun reportReturnApp() {
        val property = getLeftApplicationEventProperty()?.apply {
            appForegroundedTS = SystemClock.elapsedRealtime()
            isLeftApplication = false
        }
        if (property == null || property.clickTS == 0L || property.leftApplicationTS == 0L || property.appForegroundedTS <= property.leftApplicationTS) {
            return
        }
        adTrack(
            AdReportConstant.EVENT_AD_RETURN_APP,
            generateAdReportJson(property.seq).apply {
                put(AdReportConstant.PROPERTY_AD_CLICK_GAP, property.clickTS - property.showTS)
                put(
                    AdReportConstant.PROPERTY_AD_RETURN_GAP,
                    property.appForegroundedTS - property.leftApplicationTS
                )
            }
        )
    }


    private fun adTrack(
        eventName: String,
        properties: JSONObject?,
    ) {
        ROIQueryAnalytics.trackInternal(eventName, properties)
    }

    private fun generateAdReportJson(seq: String) =
        JSONObject().apply {
            LogUtils.d("mSequenessMap", mSequenessMap.size)
            mSequenessMap[seq]?.let { adEventProperty ->
                put(AdReportConstant.PROPERTY_AD_ID, adEventProperty.adId)
                put(AdReportConstant.PROPERTY_AD_TYPE, adEventProperty.adType)
                put(AdReportConstant.PROPERTY_AD_PLATFORM, adEventProperty.adPlatform)
                put(AdReportConstant.PROPERTY_AD_ENTRANCE, adEventProperty.entrance)
                put(AdReportConstant.PROPERTY_AD_LOCATION, adEventProperty.location)
                put(AdReportConstant.PROPERTY_AD_SEQ, seq)

                adEventProperty.properties?.let {
                    it.forEach { property ->
                        put(property.key, property.value)
                    }
                }

            }
        }

    init {
        initAppStatusListener()
    }


    /**
     * 监听应用生命周期
     */
    private fun initAppStatusListener() {
        mIsMainProcess =
            AppInfoUtils.getMainProcessName(mContext) == AppInfoUtils.getMainProcessName(mContext)
        if (!mIsMainProcess) {
            return
        }
        ROIQueryAnalytics.addAppStatusListener(object : OnAppStatusListener {
            override fun onAppForeground() {
                reportReturnApp()
                LogUtils.d("AdReport", "onAppForegrounded")
            }

            override fun onAppBackground() {
                LogUtils.d("AdReport", "onAppForegrounded")
            }
        })
    }

    private fun checkSeqError(seq: String?) {
        if (TextUtils.isEmpty(seq)) {
            throw Exception()
        }
    }

    private fun makeAdEventProperty(seq: String) {
        if (mSequenessMap.size > mMaxSequenessSize && mSequenessMap.keys.iterator().hasNext()) {
            mSequenessMap.remove(mSequenessMap.keys.last())
        }
        mSequenessMap[seq] = AdEventProperty()
    }

    private fun updateAdEventProperty(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        properties: MutableMap<String, Any>?,
        entrance: String? = ""
    ): AdEventProperty? {
        try {
            checkSeqError(seq)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        if (!EventUtils.isValidProperty(JSONObject().apply {
                properties?.forEach { property ->
                    put(property.key, property.value)
                }
            })) {
            return null
        }
        if (!mSequenessMap.containsKey(seq)) {
            makeAdEventProperty(seq)
        }
        mSequenessMap[seq]?.let {
            if (id.isNotEmpty()) {
                it.adId = id
            }
            if (type != AdType.IDLE.value) {
                it.adType = type
            }
            if (platform != AdPlatform.IDLE.value) {
                it.adPlatform = platform
            }
            if (location.isNotEmpty()) {
                it.location = location
            }
            it.seq = seq
            it.properties = properties
            it.entrance = entrance ?: ""
        }
        return mSequenessMap[seq]
    }

    private fun getLeftApplicationEventProperty(): AdEventProperty? {
        for (mutableEntry in mSequenessMap) {
            if (mutableEntry.value?.isLeftApplication!!) {
                return mutableEntry.value
            }
        }
        return null
    }


    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: AdReportImp? = null
        internal fun getInstance(context: Context? = null): AdReportImp {
            var pContext = context
            if (pContext == null) {
                pContext = ROIQueryAnalytics.getContext()
            }
            return instance ?: synchronized(this) {
                instance ?: AdReportImp(pContext).also { instance = it }
            }
        }

    }


}