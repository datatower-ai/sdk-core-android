package com.roiquery.analytics.utils

import android.net.ParseException
import android.text.TextUtils
import com.instacart.library.truetime.TrueTime
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


/**
 * 线程安全的日期格式化工具类
 * create on 2019/4/3
 *
 */
object TimeUtils {
    const val YYYY_MM_DD = "yyyy-MM-dd"
    private const val YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss.SSS"
    private val formatMaps: MutableMap<String?, ThreadLocal<SimpleDateFormat?>> = HashMap()


    fun getTrueTime(): String {
        if (TrueTime.isInitialized()) {
            return try {
                TrueTime.now().time.toString()
            } catch (e: Exception) {
                LogUtils.printStackTrace(e)
                System.currentTimeMillis().toString()
            }
        }
        return System.currentTimeMillis().toString()
    }

    /**
     * format Date 输出文本格式
     * patten 默认使用 YYYY_MM_DD_HH_MM_SS_SSS
     * 例：2019-04-12 11:22:00.408
     * Locale 默认使用 Default
     *
     * @param timeMillis 时间戳
     * @param patten 时间展示模板
     * @return 日期展示字符串
     */
    fun formatTime(timeMillis: Long, patten: String?): String {
        var patten = patten
        var formatString = ""
        if (TextUtils.isEmpty(patten)) {
            patten = YYYY_MM_DD_HH_MM_SS_SSS
        }
        val simpleDateFormat: SimpleDateFormat = getDateFormat(patten, Locale.getDefault())
            ?: return formatString
        try {
            formatString = simpleDateFormat.format(timeMillis)
        } catch (e: IllegalArgumentException) {
            LogUtils.printStackTrace(e)
        }
        return formatString
    }

    /**
     * format Date 输出文本格式
     * patten 默认使用 YYYY_MM_DD_HH_MM_SS_SSS
     * 例：2019-04-12 11:22:00.408
     * Locale 默认使用 Default
     *
     * @param date 日期
     * @return 日期展示字符串
     */
    fun formatDate(date: Date?): String {
        return formatDate(date, YYYY_MM_DD_HH_MM_SS_SSS)
    }

    /**
     * format Date 输出文本格式
     * Locale 默认使用 Default
     *
     * @param date 日期
     * @param patten 时间展示模板
     * @return 日期展示字符串
     */
    @JvmStatic
    fun formatDate(date: Date?, patten: String?): String {
        return formatDate(date, patten, Locale.getDefault())
    }

    /**
     * format Date 输出文本格式
     * patten 默认使用 YYYY_MM_DD_HH_MM_SS_SSS
     * 例：2019-04-12 11:22:00.408
     *
     * @param date 日期
     * @param locale 位置
     * @return 日期展示字符串
     */
    fun formatDate(date: Date?, locale: Locale?): String {
        return formatDate(date, YYYY_MM_DD_HH_MM_SS_SSS, locale)
    }

    /**
     * format Date 输出文本格式
     *
     * @param date 日期
     * @param patten 时间展示模板
     * @param locale 位置
     * @return 日期展示字符串
     */
    fun formatDate(date: Date?, patten: String?, locale: Locale?): String {
        var patten = patten
        if (TextUtils.isEmpty(patten)) {
            patten = YYYY_MM_DD_HH_MM_SS_SSS
        }
        var formatString = ""
        val simpleDateFormat: SimpleDateFormat = getDateFormat(patten, locale)
            ?: return formatString
        try {
            formatString = simpleDateFormat.format(date)
        } catch (e: IllegalArgumentException) {
            LogUtils.printStackTrace(e)
        }
        return formatString
    }

    /**
     * 验证日期是否合法
     *
     * @param date Date
     * @return 是否合法
     */
    fun isDateValid(date: Date): Boolean {
        try {
            val simpleDateFormat: SimpleDateFormat? =
                getDateFormat(YYYY_MM_DD_HH_MM_SS_SSS, Locale.getDefault())
            val baseDate: Date = simpleDateFormat!!.parse("2015-05-15 10:24:00.000")
            return date.after(baseDate)
        } catch (e: ParseException) {
            LogUtils.printStackTrace(e)
        }
        return false
    }

    /**
     * 验证日期是否合法，目前校验比较粗糙，仅要求数据在 "2015-05-15 10:24:00.000" 以后
     *
     * @param time Time
     * @return 是否合法
     */
    fun isDateValid(time: Long): Boolean {
        try {
            val simpleDateFormat: SimpleDateFormat? =
                getDateFormat(YYYY_MM_DD_HH_MM_SS_SSS, Locale.getDefault())
            val baseDate: Date = simpleDateFormat!!.parse("2015-05-15 10:24:00.000") ?: return false
            return baseDate.getTime() < time
        } catch (e: ParseException) {
            LogUtils.printStackTrace(e)
        }
        return false
    }

    /**
     * 将 JSONObject 中的 Date 类型数据格式化
     *
     * @param jsonObject JSONObject
     * @return JSONObject
     */
    fun formatDate(jsonObject: JSONObject?): JSONObject {
        if (jsonObject == null) {
            return JSONObject()
        }
        try {
            val iterator = jsonObject.keys()
            while (iterator.hasNext()) {
                val key = iterator.next()
                val value = jsonObject[key]
                if (value is Date) {
                    jsonObject.put(key, formatDate(value as Date, Locale.CHINA))
                }
            }
        } catch (e: JSONException) {
            LogUtils.printStackTrace(e)
        }
        return jsonObject
    }

    /**
     * 获取时区偏移值
     *
     * @return 时区偏移值，单位：分钟
     */
    val zoneOffset: Int?
        get() {
            try {
                val cal: Calendar = Calendar.getInstance(Locale.getDefault())
                val zoneOffset: Int = cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)
                return -zoneOffset / (1000 * 60)
            } catch (ex: Exception) {
                LogUtils.printStackTrace(ex)
            }
            return null
        }

    @Synchronized
    private fun getDateFormat(patten: String?, locale: Locale?): SimpleDateFormat? {
        var dateFormatThreadLocal: ThreadLocal<SimpleDateFormat?>? = formatMaps[patten]
        if (null == dateFormatThreadLocal) {
            dateFormatThreadLocal = object : ThreadLocal<SimpleDateFormat?>() {
                override fun initialValue(): SimpleDateFormat {
                    var simpleDateFormat: SimpleDateFormat? = null
                    try {
                        if (locale == null) {
                            simpleDateFormat = SimpleDateFormat(patten, Locale.getDefault())
                        } else {
                            simpleDateFormat = SimpleDateFormat(patten, locale)
                        }
                    } catch (e: Exception) {
                        LogUtils.printStackTrace(e)
                    }
                    return simpleDateFormat!!
                }
            }
            if (null != dateFormatThreadLocal.get()) {
                formatMaps[patten] = dateFormatThreadLocal
            }
        }
        return dateFormatThreadLocal.get()
    }
}
