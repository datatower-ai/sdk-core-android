package com.nodetower.analytics.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.text.TextUtils
import com.nodetower.analytics.data.DataOperation
import com.nodetower.analytics.data.DbParams
import com.nodetower.base.utils.LogUtils
import org.json.JSONObject


internal class EventDataOperation(context: Context) :
    DataOperation(context) {

    override fun insertData(uri: Uri?, jsonObject: JSONObject?): Int {
        try {
            if (deleteDataLowMemory(uri) != 0)
                return DbParams.DB_OUT_OF_MEMORY_ERROR

            val cv = ContentValues().apply {
                put(
                    DbParams.KEY_DATA,
                    jsonObject.toString() + "\t" + jsonObject.toString().hashCode()
                )
                put(
                    DbParams.KEY_CREATED_AT,
                    System.currentTimeMillis()
                )
            }

            contentResolver.insert(uri!!, cv)
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
        return 0
    }

    override fun insertData(uri: Uri?, contentValues: ContentValues?): Int {
        try {
            if (deleteDataLowMemory(uri) != 0) {
                return DbParams.DB_OUT_OF_MEMORY_ERROR
            }
            contentResolver.insert(uri!!, contentValues)
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
        return 0
    }

    override fun queryData(uri: Uri?, limit: Int): Array<String>? {
        var cursor: Cursor? = null
        var data: String? = null
        var last_id: String? = null
        try {
            cursor = contentResolver.query(
                uri!!,
                null,
                null,
                null,
                DbParams.KEY_CREATED_AT.toString() + " ASC LIMIT " + limit
            )
            if (cursor != null) {
                val dataBuilder = StringBuilder()
                val flush_time = ",\"_flush_time\":"
                var suffix = ","
                dataBuilder.append("[")
                var keyData: String
                while (cursor.moveToNext()) {
                    if (cursor.isLast) {
                        suffix = "]"
                        last_id = cursor.getString(cursor.getColumnIndex("_id"))
                    }
                    try {
                        keyData = cursor.getString(cursor.getColumnIndex(DbParams.KEY_DATA))
                        keyData = parseData(keyData)
                        if (!TextUtils.isEmpty(keyData)) {
                            dataBuilder
                                .append(keyData, 0, keyData.length - 1)
                                .append(flush_time)
                                .append(System.currentTimeMillis())
                                .append("}").append(suffix)
                        }
                    } catch (e: Exception) {
                        LogUtils.printStackTrace(e)
                    }
                }
                data = dataBuilder.toString()
            }
        } catch (e: SQLiteException) {
            LogUtils.i(
                TAG,
                "Could not pull records for analytics out of database events. Waiting to send.",
                e
            )
            last_id = null
            data = null
        } finally {
            cursor?.close()
        }
        return if (last_id != null) {
            arrayOf(last_id, data!!, DbParams.GZIP_DATA_EVENT)
        } else null
    }

    init {
        TAG = this.javaClass.simpleName
    }
}
