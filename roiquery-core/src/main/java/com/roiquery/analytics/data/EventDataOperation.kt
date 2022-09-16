package com.roiquery.analytics.data

import android.content.Context
import android.text.TextUtils
import com.roiquery.analytics.ROIQueryCoroutineScope
import com.roiquery.analytics.data.room.ROIQueryAnalyticsDB
import com.roiquery.analytics.data.room.bean.Configs
import com.roiquery.analytics.data.room.bean.Events
import com.roiquery.analytics.utils.LogUtils
import com.roiquery.quality.ROIQueryErrorParams
import com.roiquery.quality.ROIQueryQualityHelper
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


internal class EventDataOperation(
    mContext: Context
) : ROIQueryCoroutineScope() {
    var TAG = "EventDataOperation"
    private var analyticsDB: ROIQueryAnalyticsDB? = ROIQueryAnalyticsDB.getInstance(context = mContext)

    private val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        run {
            LogUtils.e(exception.message)
            reportRoomDatabaseError(exception.message)
        }
    }

    /**
     * 保存数据
     * 插入成功 返回0 失败返回 error code
     */
    suspend fun insertData(jsonObject: JSONObject?, eventSyn: String) =
        suspendCoroutine<Int> {
            scope.launch(exceptionHandler) {

                if (!deleteDataWhenOverMaxRows())
                    it.resume(DataParams.DB_OUT_OF_ROW_ERROR)
                withContext(Dispatchers.IO) {
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
                        it.resume(if(result == -1L) DataParams.DB_INSERT_ERROR else DataParams.DB_INSERT_SUCCEED)
                    } catch (e: Exception){
                        reportRoomDatabaseError(e.message)
                        it.resume(DataParams.DB_INSERT_EXCEPTION)
                    }
                }
            }
        }


    /**
     * 保存配置
     */
    fun insertConfig(name: String, value: String?) {
        scope.launch(exceptionHandler) {
            value?.let { notEmptyValue ->
                analyticsDB?.getConfigDao()?.let {
                    val isExist = it.existsValue(name) > 0
                    withContext(Dispatchers.IO) {
                        if (isExist) {
                            it.update(name = name, value = notEmptyValue)
                        } else {
                            it.insert(Configs(name = name, value = value))
                        }
                    }
                }
            }
        }

    }


    /**
     * 查询配置
     */
    suspend fun queryConfig(name: String): String? {
        try {
            return analyticsDB?.getConfigDao()?.queryValueByName(name)
        } catch (e: Exception) {
            reportRoomDatabaseError(e.message)
        }
        return null
    }


    /**
     * 查询数据
     */
    suspend fun queryData(limit: Int) =
        suspendCoroutine<String> {
            val jsonData = StringBuilder()
            val suffix = ","
            jsonData.append("[")
            scope.launch(Dispatchers.IO) {
                try {
                    val queryEventData = analyticsDB?.getEventsDao()?.queryEventData(limit)
                    queryEventData?.let { it ->
                        val size = it.size
                        for (i in 0 until size) {
                            jsonData.append(parseData(it[i]))
                                .append(if (i != size - 1) suffix else "")
                        }
                    }
                } catch (e: Exception) {
                    reportRoomDatabaseError(e.message)
                } finally {
                    jsonData.append("]")
                    it.resume(jsonData.toString())
                }
            }
        }


    /**
     * 查询数据条数
     * @return 条数
     */
    suspend fun queryDataCount() = suspendCoroutine<Int> {
        scope.launch(Dispatchers.IO) {
            try {
                it.resume(
                    analyticsDB?.getEventsDao()?.dataCount() ?: 0
                )
            } catch (e: Exception) {
                reportRoomDatabaseError(e.message)
            }
        }

    }

    /**
     * 删除数据
     */
    suspend fun deleteEventByEventSyn(eventSyn: String) {
        try {
            analyticsDB?.getEventsDao()?.deleteEventByEventSyn(eventSyn)
        } catch (ex: Exception) {
            reportRoomDatabaseError(ex.message)
        }
    }


    private suspend fun deleteTheOldestData(num: Int) {
        try {
            analyticsDB?.getEventsDao()?.deleteTheOldestData(num)
        } catch (e: Exception) {
            reportRoomDatabaseError(e.message)
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
            reportRoomDatabaseError(ex.message)
        }
        return data
    }

    /**
     * 数据库存满时删除数据
     *
     * @return 正常返回 0
     */
    private suspend fun deleteDataWhenOverMaxRows() =
        suspendCoroutine<Boolean> {
            scope.launch(Dispatchers.IO) {
                if (queryDataCount() >= DataParams.CONFIG_MAX_ROWS) {
                    LogUtils.i(
                        TAG,
                        "There is not enough space left on the device to store events, so will delete ${DataParams.CONFIG_MAX_ROWS / 2} oldest events"
                    )

                    try {
                        deleteTheOldestData(DataParams.CONFIG_MAX_ROWS / 2)
                    } catch (e: Exception) {
                        reportRoomDatabaseError(e.message)
                        it.resume(false)
                    }
                    //数据库较满时，删除成功 it.resume(true)
                    //删除失败 it.resume(false)
                    it.resume(true)
                } else {
                    it.resume(true)
                }
            }
        }


    fun deleteAllEventData() {
        scope.launch(Dispatchers.IO) {
            try {
                analyticsDB?.getEventsDao()?.clearTable()
            } catch (e: Exception) {
                reportRoomDatabaseError(e.message)
            }
        }
    }

    private fun reportRoomDatabaseError(errorMsg: String?) {
        scope.launch(Dispatchers.Main) {
            try {
                LogUtils.w(errorMsg)
                ROIQueryQualityHelper.instance.reportQualityMessage(
                    ROIQueryErrorParams.ROOM_DATABASE_ERROR,
                    errorMsg
                )
            } catch (e: Exception) {
            }
        }
    }

}
