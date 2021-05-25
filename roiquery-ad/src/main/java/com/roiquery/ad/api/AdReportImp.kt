package com.roiquery.ad.api


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

class AdReportImp : IAdReport {

    private var mContext: Context? = null
    private var mIsMainProcess: Boolean = true

    private var mCurrentLocation: String = ""
    private var mCurrentAdType: Int = AD_TYPE.IDLE
    private var mCurrentAdPlatform: Int = AD_PLATFORM.IDLE
    private var mCurrentAdId: String = ""
    private var mCurrentSeq: String = ""
    private var mCurrentEntrance: String = ""

    private var mShowTS: Long = 0
    private var mClickTS: Long = 0
    private var mLeftApplicationTS: Long = 0
    private var mAppBackgroundedTS: Long = 0
    private var mAppForegroundedTS: Long = 0

    private var mSequenessMap: MutableMap<String, AdEventProperty?> = mutableMapOf()


    override fun reportEntrance(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        entrance: String?,
    ) {
        reset()
        set(id, type, platform, location, seq, entrance)
        adTrack(
            AdReportConstant.EVENT_AD_ENTRANCE,
            seq,
            generateAdReportJson(id, type, platform, location, seq, entrance = entrance)
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
        set(id, type, platform, location, seq, entrance)
        adTrack(
            AdReportConstant.EVENT_AD_TO_SHOW,
            seq,
            generateAdReportJson(id, type, platform, location, seq, entrance = entrance)
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

        mShowTS = SystemClock.elapsedRealtime()
        set(id, type, platform, location, seq, entrance)
        adTrack(
            AdReportConstant.EVENT_AD_SHOW,
            seq,
            generateAdReportJson(id, type, platform, location, seq, entrance = entrance)
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

        mClickTS = SystemClock.elapsedRealtime()
        set(id, type, platform, location, seq, entrance)
        adTrack(
            AdReportConstant.EVENT_AD_CLICK,
            seq,
            generateAdReportJson(id, type, platform, location, seq, entrance = entrance)
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
        set(id, type, platform, location, seq, entrance)
        adTrack(
            AdReportConstant.EVENT_AD_REWARDED,
            seq,
            generateAdReportJson(id, type, platform, location, seq, entrance = entrance)
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
        mLeftApplicationTS = SystemClock.elapsedRealtime()

        set(id, type, platform, location, seq, entrance)
        adTrack(
            AdReportConstant.EVENT_AD_LEFT_APP,
            seq,
            generateAdReportJson(id, type, platform, location, seq, entrance = entrance)
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
        set(id, type, platform, location, seq, entrance)
        adTrack(
            AdReportConstant.EVENT_AD_PAID,
            seq,
            generateAdReportJson(id, type, platform, location, seq, value, currency, precision, entrance)
        )
    }

    override fun reportImpression(
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
        set(
            id,
            AdTypeUtils.getType(mediation,type),
            AdPlatformUtils.getPlatform(mediation,platform),
            location,
            seq,
            entrance
        )

        adTrack(
            AdReportConstant.EVENT_AD_PAID,
            seq,
            JSONObject().apply {
                put(AdReportConstant.PROPERTY_AD_ID, id)
                put(AdReportConstant.PROPERTY_AD_TYPE, type)
                put(AdReportConstant.PROPERTY_AD_PLATFORM, platform)
                put(AdReportConstant.PROPERTY_AD_ENTRANCE, entrance ?: "")
                put(AdReportConstant.PROPERTY_AD_LOCATION, location)
                put(AdReportConstant.PROPERTY_AD_SEQ, seq)

                put(AdReportConstant.PROPERTY_AD_VALUE_MICROS, value)
                put(AdReportConstant.PROPERTY_AD_CURRENCY_CODE, currency)
                put(AdReportConstant.PROPERTY_AD_PRECISION_TYPE, precision)
            }
        )

    }


    override fun reportReturnApp() {
        mAppForegroundedTS = SystemClock.elapsedRealtime()
        if (mClickTS == 0L || mLeftApplicationTS == 0L || mAppForegroundedTS <= mLeftApplicationTS) {
            reset()
            return
        }
        adTrack(
            AdReportConstant.EVENT_AD_RETURN_APP,
            mCurrentSeq,
            generateAdReportJson(
                mCurrentAdId,
                mCurrentAdType,
                mCurrentAdPlatform,
                mCurrentLocation,
                mCurrentSeq,
                entrance = mCurrentEntrance
            ).apply {
                put(AdReportConstant.PROPERTY_AD_CLICK_GAP, mClickTS - mShowTS)
                put(
                    AdReportConstant.PROPERTY_AD_RETURN_GAP,
                    mAppForegroundedTS - mLeftApplicationTS
                )
            }
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
        set(id, type, platform, location, seq, entrance)
        adTrack(
            AdReportConstant.EVENT_AD_CLOSE,
            seq,
            generateAdReportJson(id, type, platform, location, seq, entrance = entrance)
        )
    }

    private fun adTrack(
        eventName: String,
        seq: String,
        properties: JSONObject?,
    ) {
        try {
            checkSeqError(seq)
        } catch (e: Exception) {
            e.printStackTrace()
            reset()
            return
        }
//        LogUtils.json("$TAG:$eventName", properties)

        ROIQueryAnalytics.track(eventName, properties)
    }

    private fun generateAdReportJson(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        value: String = "",
        currency: String = "",
        precision: String = "",
        entrance: String?,
    ) = JSONObject().apply {
        put(AdReportConstant.PROPERTY_AD_ID, id)
        put(AdReportConstant.PROPERTY_AD_TYPE, type)
        put(AdReportConstant.PROPERTY_AD_PLATFORM, platform)
        put(AdReportConstant.PROPERTY_AD_ENTRANCE, entrance ?: "")
        put(AdReportConstant.PROPERTY_AD_LOCATION, location)
        put(AdReportConstant.PROPERTY_AD_SEQ, seq)

        if (value.isNotEmpty() || currency.isNotEmpty() || precision.isNotEmpty()) {
            put(AdReportConstant.PROPERTY_AD_VALUE_MICROS, value)
            put(AdReportConstant.PROPERTY_AD_CURRENCY_CODE, currency)
            put(AdReportConstant.PROPERTY_AD_PRECISION_TYPE, precision)
        }

    }


    private constructor(context: Context?) {
        mContext = context
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
        checkSeqError()
        if (!TextUtils.equals(mCurrentSeq, seq)) {
            throw Exception()
        }
    }

    private fun checkSeqError() {
        if (TextUtils.isEmpty(mCurrentSeq)) {
            throw Exception()
        }
    }

    private fun set(
        id: String,
        type: Int,
        platform: Int,
        location: String,
        seq: String,
        entrance: String?
    ){
        mCurrentAdId = id
        mCurrentAdType = type
        mCurrentAdPlatform = platform
        mCurrentLocation = location
        mCurrentSeq = seq
        entrance?.let { mCurrentEntrance = it }
    }

    private fun reset() {
        mCurrentLocation = ""
        mCurrentAdType = AD_TYPE.IDLE
        mCurrentAdPlatform = AD_PLATFORM.IDLE
        mCurrentAdId = ""
        mCurrentSeq = ""
        mCurrentEntrance = ""
        mShowTS = 0L
        mClickTS = 0L
        mLeftApplicationTS = 0L
        mAppBackgroundedTS = 0L
        mAppForegroundedTS = 0L
    }

    private fun makeAdEventProperty(seq: String){
        if (mSequenessMap.size > 3){
            mSequenessMap.remove(mSequenessMap.keys.last())
        }
        mSequenessMap[seq] = AdEventProperty()
    }

    private fun updateAdEventProperty(seq: String){
        mSequenessMap[seq]?.let {

        }
    }


    companion object {

        const val TAG = "AdReport"

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