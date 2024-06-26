package ai.datatower.ad.api


import ai.datatower.ad.AdPlatform
import ai.datatower.ad.AdReportConstant
import ai.datatower.ad.AdReportConstant.PROPERTY_AD_SHOW_ERROR_CODE
import ai.datatower.ad.AdReportConstant.PROPERTY_AD_SHOW_ERROR_MESSAGE
import ai.datatower.ad.AdType
import ai.datatower.ad.utils.AdEventProperty
import ai.datatower.ad.utils.AdPlatformUtils
import ai.datatower.analytics.DTAnalytics
import ai.datatower.analytics.utils.EventUtils
import ai.datatower.analytics.utils.LogUtils
import android.annotation.SuppressLint
import android.content.Context
import android.os.SystemClock
import android.text.TextUtils
import org.json.JSONObject

class AdReportImp : IAdReport {

    private var mSequenessMap: MutableMap<String, AdEventProperty?> = mutableMapOf()
    private val mMaxSequenessSize = 10

    override fun reportLoadBegin(
        id: String,
        type: Int,
        platform: Int,
        seq: String,
        properties: MutableMap<String, Any>?,
        mediation: Int,
        mediationId: String
    ) {
        updateAdEventProperty(id, type, platform, "", seq, properties, "",
            mediation = mediation, mediationId = mediationId
        ) ?: return
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
        properties: MutableMap<String, Any>?,
        mediation: Int,
        mediationId: String
    ) {
        updateAdEventProperty(id, type, platform, "", seq, properties, "",
            mediation = mediation, mediationId = mediationId
        ) ?: return
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
        entrance: String?,
        mediation: Int,
        mediationId: String
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
        entrance: String?,
        mediation: Int,
        mediationId: String
    ) {
        updateAdEventProperty(id, type, platform, location, seq, properties, entrance,
            mediation = mediation, mediationId = mediationId
        ) ?: return
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
        entrance: String?,
        mediation: Int,
        mediationId: String
    ) {
        updateAdEventProperty(id, type, platform, location, seq, properties, entrance,
            mediation = mediation, mediationId = mediationId
        )?.apply {
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
        entrance: String?,
        mediation: Int,
        mediationId: String
    ) {
        updateAdEventProperty(id, type, platform, location, seq, properties, entrance,
            mediation = mediation, mediationId = mediationId
        )?.apply {
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
        entrance: String?,
        mediation: Int,
        mediationId: String
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
        entrance: String?,
        mediation: Int,
        mediationId: String
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
        mediation: Int,
        mediationId: String
    ) {
        updateAdEventProperty(id, type, platform, location, seq, properties, entrance,
            mediation = mediation, mediationId = mediationId
        ) ?: return
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
        mediation: Int,
        mediationId: String
    ) {
        updateAdEventProperty(id, type, platform, location, seq, properties, entrance,
            mediation = mediation, mediationId = mediationId
        )?.apply {
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
        mediation: Int,
        mediationId: String
    ) {
        updateAdEventProperty(id, type, platform, location, seq, properties, entrance,
            mediation = mediation, mediationId = mediationId
        ) ?: return
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
        entrance: String?,
        mediation: Int,
        mediationId: String
    ) {
        updateAdEventProperty(id, type, platform, location, seq, properties, entrance,
            mediation = mediation, mediationId = mediationId
        ) ?: return
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
        mediation: Int,
        mediationId: String
    ) {
        updateAdEventProperty(id, type, platform, location, seq, properties, entrance,
            mediation = mediation, mediationId = mediationId
        )?.apply {
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
        value: Double,
        currency: String,
        precision: String,
        properties: MutableMap<String, Any>?,
        entrance: String?,
        mediation: Int,
        mediationId: String
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
                    "        entrance: $entrance?\n" +
                    "        mediation: $mediation,\n" +
                    "        mediationId: $mediationId"
        )
        val property =
            updateAdEventProperty(id, type, platform, location, seq, properties, entrance,
                mediation = mediation, mediationId = mediationId
            )?.apply {
                this.value = value
                this.currency = currency
                this.precision = precision
            } ?: return
        adTrack(
            AdReportConstant.EVENT_AD_PAID,
            generateAdReportJson(seq).apply {
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
        value: Double,
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
            entrance,
            mediation = mediation,
            mediationId = mediationId
        )?.apply {
            this.value = value
            this.currency = currency
            this.precision = precision
            this.country = country
        } ?: return
        adTrack(
            AdReportConstant.EVENT_AD_PAID,
            generateAdReportJson(seq).apply {
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
        value: Double,
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
                    "        precision: $precision,\n" +
                    "        mediation: $mediation,\n" +
                    "        mediationId: $mediationId"

        )
        val property = updateAdEventProperty(id, type, platform, location, seq, properties,
            mediation = mediation, mediationId = mediationId
        )?.apply {
            this.value = value
            this.precision = precision
            this.country = country
        } ?: return
        adTrack(
            AdReportConstant.EVENT_AD_PAID,
            generateAdReportJson(seq).apply {
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
        DTAnalytics.trackInternal(eventName, properties)
    }

    private fun generateAdReportJson(seq: String) =
        JSONObject().apply {
            mSequenessMap[seq]?.let { adEventProperty ->
                put(AdReportConstant.PROPERTY_AD_ID, adEventProperty.adId)
                put(AdReportConstant.PROPERTY_AD_TYPE, adEventProperty.adType)
                put(AdReportConstant.PROPERTY_AD_PLATFORM, adEventProperty.adPlatform)
                put(AdReportConstant.PROPERTY_AD_MEDIAITON, adEventProperty.mediation)
                put(AdReportConstant.PROPERTY_AD_MEDIAITON_ID, adEventProperty.mediationId)
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
        entrance: String? = "",
        mediation: Int,
        mediationId: String
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
            it.mediation = mediation
            it.mediationId = mediationId
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
            return instance ?: synchronized(this) {
                instance ?: AdReportImp().also { instance = it }
            }
        }

    }


}