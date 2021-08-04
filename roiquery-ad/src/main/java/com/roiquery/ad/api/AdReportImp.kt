package com.roiquery.ad.api


import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.text.TextUtils
import com.roiquery.ad.AD_PLATFORM
import com.roiquery.ad.AD_TYPE
import com.roiquery.ad.AdReportConstant
import com.roiquery.ad.utils.AdEventProperty
import com.roiquery.ad.utils.AdPlatformUtils
import com.roiquery.ad.utils.AdTypeUtils
import com.roiquery.analytics.ROIQueryAnalytics
import com.roiquery.analytics.utils.AppInfoUtils
import com.roiquery.analytics.utils.AppLifecycleHelper.OnAppStatusListener
import com.roiquery.analytics.utils.LogUtils
import org.json.JSONObject

class AdReportImp private constructor(context: Context?) : IAdReport {

    private var mContext: Context? = context
    private var mIsMainProcess: Boolean = true

    private var mSequenessMap: MutableMap<String, AdEventProperty?> = mutableMapOf()
    private val mMaxSequenessSize = 10


    override fun reportEntrance(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        entrance: String?,
    ) {
        updateAdEventProperty(id, type, platform, location, seq, entrance)
        adTrack(
            AdReportConstant.EVENT_AD_ENTRANCE,
            generateAdReportJson(seq)
        )
    }

    override fun reportToShow(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        entrance: String?,
    ) {
        updateAdEventProperty(id, type, platform, location, seq, entrance)
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
        entrance: String?,
    ) {
        updateAdEventProperty(id, type, platform, location, seq, entrance)?.apply {
            showTS = SystemClock.elapsedRealtime()
        }
        adTrack(
            AdReportConstant.EVENT_AD_SHOW,
            generateAdReportJson(seq)
        )
    }

    override fun reportImpression(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        entrance: String?
    ) {
        updateAdEventProperty(id, type, platform, location, seq, entrance)?.apply {
            showTS = SystemClock.elapsedRealtime()
        }
        adTrack(
            AdReportConstant.EVENT_AD_IMPRESSION,
            generateAdReportJson(seq)
        )
    }

    override fun reportOpen(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        entrance: String?
    ) {
        updateAdEventProperty(id, type, platform, location, seq, entrance)
        adTrack(
            AdReportConstant.EVENT_AD_OPEN,
            generateAdReportJson(seq)
        )
    }

    override fun reportClose(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        entrance: String?,
    ) {
        updateAdEventProperty(id, type, platform, location, seq, entrance)
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
        entrance: String?,
    ) {
        updateAdEventProperty(id, type, platform, location, seq, entrance)?.apply {
            clickTS = SystemClock.elapsedRealtime()
        }
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
        entrance: String?,
    ) {
        updateAdEventProperty(id, type, platform, location, seq, entrance)
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
        entrance: String?
    ) {
        updateAdEventProperty(id, type, platform, location, seq, entrance)
        adTrack(
            AdReportConstant.EVENT_AD_CONVERSION,
            generateAdReportJson(seq).apply {
                put(AdReportConstant.PROPERTY_AD_CONVERSION_SOURCE,conversionSource)
            }
        )
    }

    override fun reportLeftApp(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        entrance: String?,
    ) {
        updateAdEventProperty(id, type, platform, location, seq, entrance)?.apply {
            leftApplicationTS = SystemClock.elapsedRealtime()
            isLeftApplication = true
        }
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
        val property = updateAdEventProperty(id, type, platform, location, seq, entrance)?.apply {
            this.value = value
            this.currency = currency
            this.precision = precision
        }
        adTrack(
            AdReportConstant.EVENT_AD_PAID,
            generateAdReportJson(seq).apply {
                put(AdReportConstant.PROPERTY_AD_MEDIAITON, property?.mediation)
                put(AdReportConstant.PROPERTY_AD_MEDIAITON_ID, property?.mediationId)
                put(AdReportConstant.PROPERTY_AD_VALUE_MICROS, property?.value)
                put(AdReportConstant.PROPERTY_AD_CURRENCY_CODE, property?.currency)
                put(AdReportConstant.PROPERTY_AD_PRECISION_TYPE, property?.precision)
                put(AdReportConstant.PROPERTY_AD_COUNTRY, property?.country)
            }
        )
    }

    override fun reportPaid(
        id: String,
        type: String,
        platform: String,
        location: String,
        seq: String,
        mediation: Int,
        mediationId: String,
        value: String,
        currency: String,
        precision: String,
        country: String,
        entrance: String?
    ) {
        LogUtils.d(
            "reportImpression",
            "id: $id,\n" +
                    "        type: $type,\n" +
                    "        platform: $platform,\n" +
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
            id,
            AdTypeUtils.getType(mediation, type),
            AdPlatformUtils.getPlatform(mediation, platform),
            location,
            seq,
            entrance
        )?.apply {
            this.mediation = mediation
            this.mediationId = mediationId
            this.value = value
            this.currency = currency
            this.precision = precision
            this.country = country
        }
        adTrack(
            AdReportConstant.EVENT_AD_PAID,
            generateAdReportJson(seq).apply {
                put(AdReportConstant.PROPERTY_AD_MEDIAITON, property?.mediation)
                put(AdReportConstant.PROPERTY_AD_MEDIAITON_ID, property?.mediationId)
                put(AdReportConstant.PROPERTY_AD_VALUE_MICROS, property?.value)
                put(AdReportConstant.PROPERTY_AD_CURRENCY_CODE, property?.currency)
                put(AdReportConstant.PROPERTY_AD_PRECISION_TYPE, property?.precision)
                put(AdReportConstant.PROPERTY_AD_COUNTRY, property?.country)
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
        ROIQueryAnalytics.track(eventName, properties)
    }

    private fun generateAdReportJson(seq: String) =
        JSONObject().apply {
            LogUtils.d("mSequenessMap",mSequenessMap.size)
            mSequenessMap[seq]?.let { adEventProperty ->
                put(AdReportConstant.PROPERTY_AD_ID, adEventProperty.adId)
                put(AdReportConstant.PROPERTY_AD_TYPE, adEventProperty.adType)
                put(AdReportConstant.PROPERTY_AD_PLATFORM, adEventProperty.adPlatform)
                put(AdReportConstant.PROPERTY_AD_ENTRANCE, adEventProperty.entrance)
                put(AdReportConstant.PROPERTY_AD_LOCATION, adEventProperty.location)
                put(AdReportConstant.PROPERTY_AD_SEQ, seq)
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
        entrance: String?
    ): AdEventProperty? {
        try {
            checkSeqError(seq)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        if (!mSequenessMap.containsKey(seq)) {
            makeAdEventProperty(seq)
        }
        mSequenessMap[seq]?.let {
            if (id.isNotEmpty()) {
                it.adId = id
            }
            if (type != AD_TYPE.IDLE){
                it.adType = type
            }
            if (platform != AD_PLATFORM.IDLE){
                it.adPlatform = platform
            }
            it.seq = seq
            it.location = location
            it.entrance = entrance ?: ""
        }
        return mSequenessMap[seq]
    }

    private fun getLeftApplicationEventProperty():AdEventProperty?{
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