package com.nodetower.analytics.data.persistent

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import com.nodetower.analytics.data.DataOperation
import com.nodetower.analytics.data.DbParams
import com.nodetower.base.utils.LogUtils
import org.json.JSONObject

internal class PersistentDataOperation(context: Context?) :
    DataOperation(context!!) {
    override fun queryData(uri: Uri?, limit: Int): Array<String>? {
        return handleQueryUri(uri)
    }


    override fun insertData(uri: Uri?, jsonObject: JSONObject?): Int? {
        return jsonObject?.let { handleInsertUri(uri, it) }
    }

    override fun insertData(uri: Uri?, contentValues: ContentValues?): Int {
        contentResolver.insert(uri!!, contentValues)
        return 0
    }

    private fun handleInsertUri(uri: Uri?, jsonObject: JSONObject): Int {
        if (uri == null) return -1
        val contentValues = ContentValues()
        var path = uri.path
        if (!TextUtils.isEmpty(path)) {
            path = path!!.substring(1)
            when (path) {
                DbParams.TABLE_ACTIVITY_START_COUNT -> contentValues.put(
                    DbParams.TABLE_ACTIVITY_START_COUNT,
                    jsonObject.optInt(DbParams.VALUE)
                )
                DbParams.TABLE_APP_END_DATA -> contentValues.put(
                    DbParams.TABLE_APP_END_DATA,
                    jsonObject.optString(DbParams.VALUE)
                )
                DbParams.TABLE_APP_END_TIME -> contentValues.put(
                    DbParams.TABLE_APP_END_TIME,
                    jsonObject.optLong(DbParams.VALUE)
                )
                DbParams.TABLE_APP_START_TIME -> contentValues.put(
                    DbParams.TABLE_APP_START_TIME,
                    jsonObject.optLong(DbParams.VALUE)
                )
                DbParams.TABLE_SESSION_INTERVAL_TIME -> contentValues.put(
                    DbParams.TABLE_SESSION_INTERVAL_TIME,
                    jsonObject.optLong(DbParams.VALUE)
                )
                DbParams.TABLE_LOGIN_ID -> contentValues.put(
                    DbParams.TABLE_LOGIN_ID,
                    jsonObject.optString(DbParams.VALUE)
                )
                DbParams.TABLE_GAID -> contentValues.put(
                    DbParams.TABLE_GAID,
                    jsonObject.optString(DbParams.VALUE)
                )
                DbParams.TABLE_OAID -> contentValues.put(
                    DbParams.TABLE_OAID,
                    jsonObject.optString(DbParams.VALUE)
                )
                DbParams.TABLE_SUB_PROCESS_FLUSH_DATA -> contentValues.put(
                    DbParams.TABLE_SUB_PROCESS_FLUSH_DATA,
                    jsonObject.optBoolean(DbParams.VALUE)
                )
                DbParams.TABLE_FIRST_PROCESS_START -> contentValues.put(
                    DbParams.TABLE_FIRST_PROCESS_START,
                    jsonObject.optBoolean(DbParams.VALUE)
                )
                else -> return -1
            }
            contentResolver.insert(uri, contentValues)
        }
        return 0
    }

    private fun handleQueryUri(uri: Uri?): Array<String>? {
        if (uri == null) return null
        var path = uri.path
        if (TextUtils.isEmpty(path)) return null
        var cursor: Cursor? = null
        try {
            path = path!!.substring(1)
            cursor = contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.count > 0) {
                cursor.moveToNext()
                return when (path) {
                    DbParams.TABLE_ACTIVITY_START_COUNT, DbParams.TABLE_SUB_PROCESS_FLUSH_DATA, DbParams.TABLE_FIRST_PROCESS_START -> arrayOf(
                        cursor.getInt(0).toString()
                    )
                    DbParams.TABLE_APP_END_DATA, DbParams.TABLE_LOGIN_ID, DbParams.TABLE_OAID, DbParams.TABLE_GAID -> arrayOf(
                        cursor.getString(
                            0
                        )
                    )
                    DbParams.TABLE_APP_END_TIME, DbParams.TABLE_SESSION_INTERVAL_TIME, DbParams.TABLE_APP_START_TIME -> arrayOf(
                        cursor.getLong(0).toString()
                    )
                    else -> null
                }
            }
        } catch (ex: Exception) {
            LogUtils.printStackTrace(ex)
        } finally {
            cursor?.close()
        }
        return null
    }
}
