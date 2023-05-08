package com.roiquery.analytics.data

import android.content.Context
import android.text.TextUtils
import com.roiquery.analytics.data.room.ROIQueryAnalyticsDB
import com.roiquery.analytics.data.room.bean.Configs
import com.roiquery.analytics.data.room.bean.Events
import com.roiquery.analytics.utils.LogUtils
import com.roiquery.quality.ROIQueryErrorParams
import com.roiquery.quality.ROIQueryQualityHelper
import org.json.JSONObject


internal class EventDataOperation(
    mContext: Context
)  {
    var TAG = "EventDataOperation"
    private var analyticsDB: ROIQueryAnalyticsDB? =
        ROIQueryAnalyticsDB.getInstance(context = mContext)
    private var eventCount = -1


    fun insertData(jsonObject: JSONObject?, eventSyn: String): Int {
        //删除已满数据异常
        if (!deleteDataWhenOverMaxRows()) {
            return DataParams.DB_OUT_OF_ROW_ERROR
        }
        try {
            //return the SQLite row id or -1 if no row is inserted
            val result = analyticsDB?.getEventsDao()?.insertEvent(
                Events(
                    createdAt = System.currentTimeMillis(),
                    data =
                    jsonObject.toString() + "\t" + jsonObject.toString()
                        .hashCode(),
                    eventSyn = eventSyn
                )
            )
            return if (result != -1L) {
                eventCount += 1
                DataParams.DB_INSERT_SUCCEED
            } else {
                DataParams.DB_INSERT_ERROR
            }
        } catch (exception: Exception) {
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.CODE_INSERT_DB_EXCEPTION,
                exception.message, ROIQueryErrorParams.INSERT_DB_EXCEPTION
            )
            return DataParams.DB_INSERT_EXCEPTION
        }
    }


    /**
     * 保存配置
     */
    fun insertConfig(name: String, value: String?) {
        try {
            value?.let { notEmptyValue ->
                analyticsDB?.getConfigDao()?.let {
                    val isExist = it.existsValue(name) > 0
                    if (isExist) {
                        it.update(name = name, value = notEmptyValue)
                    } else {
                        it.insert(Configs(name = name, value = value))
                    }
                }
            }
        } catch (exception: Exception) {
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.CODE_INSERT_DB_NORMAL_ERROR,
                "insertConfig：$name " + exception.message, ROIQueryErrorParams.INSERT_DB_NORMAL_ERROR
            )
        }
    }


    /**
     * 查询配置
     */
    fun queryConfig(name: String): String? {
        return try {
            analyticsDB?.getConfigDao()?.queryValueByName(name)
        } catch (exception: Exception) {
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.CODE_QUERY_DB_EXCEPTION,
                "queryConfig: $name " + exception.message
            )
            null
        }
    }



    /**
     * 查询数据
     */
    fun queryData(limit: Int): String {
        val jsonData = StringBuilder()
        val suffix = ","
        jsonData.append("[")
        try {
            val queryEventData = analyticsDB?.getEventsDao()?.queryEventData(limit)
            queryEventData?.let { it ->
                val size = it.size
                for (i in 0 until size) {
                    jsonData.append(parseData(it[i]))
                        .append(if (i != size - 1) suffix else "")
                }
            }
        } catch (exception: Exception) {
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.CODE_QUERY_DB_EXCEPTION,
                "queryData:" + exception.message
            )
        } finally {
            jsonData.append("]")
        }
        return jsonData.toString()
    }



    /**
     * 查询数据条数
     * @return 条数
     */
    private fun queryDataCount(): Int {
        var count = 0
        try {
            count = analyticsDB?.getEventsDao()?.dataCount() ?: 0
        } catch (exception: Exception) {
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.CODE_QUERY_DB_EXCEPTION,
                exception.message
            )
        }
        return count
    }


    /**
     * 删除数据
     */
    fun deleteEventByEventSyn(eventSyn: String) {
        try {
            analyticsDB?.getEventsDao()?.deleteEventByEventSyn(eventSyn)
            if (eventCount != 0) {
                eventCount -= 1
            }
        } catch (exception: Exception) {
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.CODE_DELETE_DB_EXCEPTION,
                exception.message, ROIQueryErrorParams.DELETE_DB_EXCEPTION
            )
        }
    }

    fun deleteBatchEventByEventSyn(eventSyns: List<String>) {
        try {
            analyticsDB?.getEventsDao()?.deleteBatchEventByEventSyn(eventSyns)
            if (eventCount != 0) {
                eventCount -= 1
            }
        } catch (exception: Exception) {
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.CODE_DELETE_DB_EXCEPTION,
                exception.message, ROIQueryErrorParams.DELETE_DB_EXCEPTION
            )
        }
    }



    private fun deleteTheOldestData(num: Int) {
        try {
            analyticsDB?.getEventsDao()?.deleteTheOldestData(num)
            if (eventCount != 0) {
                eventCount -= num
            }
        } catch (exception: Exception) {
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.CODE_DELETE_DB_EXCEPTION,
                exception.message, ROIQueryErrorParams.DELETE_DB_EXCEPTION
            )
        }
    }


    private fun parseData(keyData: String): String {
        var data = keyData
        try {
            if (TextUtils.isEmpty(data)) return ""
            val index = data.lastIndexOf("\t")
            if (index > -1) {
                val crc = data.substring(index).replaceFirst("\t".toRegex(), "")
                data = data.substring(0, index)
                if (TextUtils.isEmpty(data) || TextUtils.isEmpty(crc)
                    || crc != data.hashCode().toString()
                ) {
                    return ""
                }
            }
        } catch (ex: Exception) {
        }
        return data
    }



    /**
     * 数据库存满时,删除数据
     *
     * @return 正常返回 true, 删除异常返回false
     */
    private fun deleteDataWhenOverMaxRows(): Boolean {
        try {
            if (eventCount < 0) {
                eventCount = queryDataCount()
            }
            if (eventCount >= DataParams.CONFIG_MAX_ROWS) {
                LogUtils.i(
                    TAG,
                    "There is not enough space left on the device to store events, so will delete ${DataParams.CONFIG_MAX_ROWS / 2} oldest events"
                )
                deleteTheOldestData(DataParams.CONFIG_MAX_ROWS / 2)
                //数据库较满时，删除成功
                eventCount -= DataParams.CONFIG_MAX_ROWS / 2
            }
            return true
        } catch (exception: Exception) {
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.CODE_INSERT_DB_OUT_OF_ROW_ERROR,
                exception.message, ROIQueryErrorParams.INSERT_DB_OUT_OF_ROW_ERROR
            )
        }
        return false
    }

    fun deleteAllEventData() {
        try {
            analyticsDB?.getEventsDao()?.clearTable()
            eventCount = 0
        } catch (e: Exception) {
            ROIQueryQualityHelper.instance.reportQualityMessage(
                ROIQueryErrorParams.CODE_DELETE_DB_EXCEPTION,
                "deleteAllEventData:" + e.message, ROIQueryErrorParams.DELETE_DB_EXCEPTION
            )
        }
    }


}
