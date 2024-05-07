package ai.datatower.analytics.data

import ai.datatower.analytics.data.room.DTAnalyticsDB
import ai.datatower.analytics.data.room.bean.Configs
import ai.datatower.analytics.data.room.bean.Events
import ai.datatower.analytics.utils.LogUtils
import ai.datatower.quality.DTErrorParams
import ai.datatower.quality.DTQualityHelper
import android.content.Context
import android.text.TextUtils
import android.util.Log
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


internal class EventDataOperation(
    mContext: Context
)  {
    companion object {
        const val TAG = "EventDataOperation"
    }

    private var analyticsDB: DTAnalyticsDB? =
        DTAnalyticsDB.getInstance(context = mContext)
    private var eventCount = -1


    suspend fun insertData(jsonObject: JSONObject?, eventSyn: String): Int = suspendCoroutine {
        //删除已满数据异常
        if (!deleteDataWhenOverMaxRows()) {
            it.resume(DataParams.DB_OUT_OF_ROW_ERROR)
            return@suspendCoroutine
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
            val returns = if (result != -1L) {
                eventCount += 1
                DataParams.DB_INSERT_SUCCEED
            } else {
                DataParams.DB_INSERT_ERROR
            }
            it.resume(returns)
        } catch (exception: Exception) {
            DTQualityHelper.instance.reportQualityMessage(
                DTErrorParams.CODE_INSERT_DB_EXCEPTION,
                exception.message, DTErrorParams.INSERT_DB_EXCEPTION
            )
            it.resume(DataParams.DB_INSERT_EXCEPTION)
        }
    }


    /**
     * 保存配置
     */
    suspend fun insertConfig(name: String, value: String?): Unit = suspendCoroutine {
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
            DTQualityHelper.instance.reportQualityMessage(
                DTErrorParams.CODE_INSERT_DB_NORMAL_ERROR,
                "insertConfig：$name " + exception.message, DTErrorParams.INSERT_DB_NORMAL_ERROR
            )
        }
        it.resume(Unit)
    }


    /**
     * 查询配置
     */
    suspend fun queryConfig(name: String): String? = suspendCoroutine {
        val value = try {
            analyticsDB?.getConfigDao()?.queryValueByName(name)
        } catch (exception: Exception) {
            DTQualityHelper.instance.reportQualityMessage(
                DTErrorParams.CODE_QUERY_DB_EXCEPTION,
                "queryConfig: $name " + exception.message
            )
            null
        }
        it.resume(value)
    }



    /**
     * 查询数据
     */
    suspend fun queryData(limit: Int): String = suspendCoroutine {
        val jsonData = StringBuilder()
        val suffix = ","
        jsonData.append("[")
        try {
            val queryEventData = analyticsDB?.getEventsDao()?.queryEventData(limit)
            queryEventData?.let {
                val size = it.size
                for (i in 0 until size) {
                    jsonData.append(parseData(it[i]))
                        .append(if (i != size - 1) suffix else "")
                }
            }
        } catch (exception: Exception) {
            DTQualityHelper.instance.reportQualityMessage(
                DTErrorParams.CODE_QUERY_DB_EXCEPTION,
                "queryData:" + exception.message
            )
        } finally {
            jsonData.append("]")
        }
        it.resume(jsonData.toString())
    }



    /**
     * 查询数据条数
     * @return 条数
     */
    fun queryDataCount(): Int {
        var count = 0
        try {
            count = analyticsDB?.getEventsDao()?.dataCount() ?: 0
        } catch (exception: Exception) {
            DTQualityHelper.instance.reportQualityMessage(
                DTErrorParams.CODE_QUERY_DB_EXCEPTION,
                exception.message
            )
        }
        return count
    }


    /**
     * 删除数据
     */
    /* FIXME: Function disabled because nobody calls it.
    fun deleteEventByEventSyn(eventSyn: String) {
        try {
            analyticsDB?.getEventsDao()?.deleteEventByEventSyn(eventSyn)
            if (eventCount != 0) {
                eventCount -= 1
            }
        } catch (exception: Exception) {
            DTQualityHelper.instance.reportQualityMessage(
                DTErrorParams.CODE_DELETE_DB_EXCEPTION,
                exception.message, DTErrorParams.DELETE_DB_EXCEPTION
            )
        }
    }
     */

    suspend fun deleteBatchEventByEventSyn(eventSyns: List<String>) = suspendCoroutine<Unit> {
        try {
            analyticsDB?.getEventsDao()?.deleteBatchEventByEventSyn(eventSyns)
            if (eventCount != 0) {
                eventCount -= 1
            }
        } catch (exception: Exception) {
            DTQualityHelper.instance.reportQualityMessage(
                DTErrorParams.CODE_DELETE_DB_EXCEPTION,
                exception.message, DTErrorParams.DELETE_DB_EXCEPTION
            )
        }
        it.resume(Unit)
    }



    private fun deleteTheOldestData(num: Int) {
        try {
            analyticsDB?.getEventsDao()?.deleteTheOldestData(num)
            if (eventCount != 0) {
                eventCount -= num
            }
        } catch (exception: Exception) {
            DTQualityHelper.instance.reportQualityMessage(
                DTErrorParams.CODE_DELETE_DB_EXCEPTION,
                exception.message, DTErrorParams.DELETE_DB_EXCEPTION
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
        } catch (_: Exception) {
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
            DTQualityHelper.instance.reportQualityMessage(
                DTErrorParams.CODE_INSERT_DB_OUT_OF_ROW_ERROR,
                exception.message, DTErrorParams.INSERT_DB_OUT_OF_ROW_ERROR
            )
        }
        return false
    }

    suspend fun deleteAllEventData(): Unit = suspendCoroutine {
        try {
            analyticsDB?.getEventsDao()?.clearTable()
            eventCount = 0
        } catch (e: Exception) {
            DTQualityHelper.instance.reportQualityMessage(
                DTErrorParams.CODE_DELETE_DB_EXCEPTION,
                "deleteAllEventData:" + e.message, DTErrorParams.DELETE_DB_EXCEPTION
            )
        }
        it.resume(Unit)
    }

}
