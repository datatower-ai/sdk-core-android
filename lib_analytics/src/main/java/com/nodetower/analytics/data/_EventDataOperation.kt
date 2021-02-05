package com.nodetower.analytics.data

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.text.TextUtils
import com.nodetower.analytics.api.RoiqueryAnalyticsAPI
import com.nodetower.analytics.data.db.EventDataDBHelper
import com.nodetower.base.utils.LogUtils
import org.json.JSONObject
import java.io.File


internal  class _EventDataOperation(private val mContext: Context) {
    var TAG = "EventDataOperation"
//    var contentResolver: ContentResolver = mContext.contentResolver
    private var mDbHelper: DbHelper? = DbHelper(mContext)
    private val mDatabaseFile: File = mContext.getDatabasePath(DataParams.DATABASE_NAME)
    private var isDbWritable = true

    /**
     * 保存数据
     */
     fun insertData(uri: Uri?, jsonObject: JSONObject?): Int?{
        try {
            if (deleteDataLowMemory(uri) != 0)
                return DataParams.DB_OUT_OF_MEMORY_ERROR

            val cv = ContentValues().apply {
                put(
                    DataParams.KEY_DATA,
                    jsonObject.toString() + "\t" + jsonObject.toString().hashCode()
                )
                put(
                    DataParams.KEY_CREATED_AT,
                    System.currentTimeMillis()
                )
            }

        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
        return 0
     }

    /**
     * 保存数据
     */
     fun insertData(uri: Uri?, contentValues: ContentValues?): Int{
        try {
            if (deleteDataLowMemory(uri) != 0) {
                return DataParams.DB_OUT_OF_MEMORY_ERROR
            }
            mDbHelper?.insert(uri!!, contentValues)
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
        return 0
     }

    /**
     * 查询数据
     */
     fun queryData(uri: Uri?, limit: Int): Array<String>?{
        var cursor: Cursor? = null
        var data: String? = null
        var last_id: String? = null
        try {
            cursor = mDbHelper?.query(
                uri!!,
                null,
                null,
                null,
                DataParams.KEY_CREATED_AT.toString() + " ASC LIMIT " + limit
            )
            if (cursor != null) {
                val dataBuilder = StringBuilder()
//                val flush_time = ",\"_flush_time\":"
                var suffix = ","
                if (limit != 1) {
                    dataBuilder.append("[")
                }

                var keyData: String
                while (cursor.moveToNext()) {
                    if (cursor.isLast) {
                        if (limit != 1) {
                            suffix = "]"
                        }else{
                            suffix = ""
                        }
                        last_id = cursor.getString(cursor.getColumnIndex("_id"))
                    }
                    try {
                        keyData = cursor.getString(cursor.getColumnIndex(DataParams.KEY_DATA))
                        keyData = parseData(keyData)
                        if (!TextUtils.isEmpty(keyData)) {
                            dataBuilder
                                .append(keyData, 0, keyData.length - 1)
//                                .append(flush_time)
//                                .append(System.currentTimeMillis())
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
            arrayOf(last_id, data!!, DataParams.GZIP_DATA_EVENT)
        } else null
    }

    /**
     * 查询数据条数
     *
     * @param uri Uri
     * @return 条数
     */
    @JvmOverloads
    fun queryDataCount(
        uri: Uri?,
        projection: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        sortOrder: String? = null
    ): Int {
        var cursor: Cursor? = null
        try {
            cursor = mDbHelper?.query(uri!!, projection, selection, selectionArgs, sortOrder)
            return cursor?.count!!

        } catch (ex: Exception) {
            LogUtils.printStackTrace(ex)
        } finally {
            cursor?.close()
        }
        return 0
    }

    /**
     * 删除数据
     */
   open fun deleteData(uri: Uri?, id: String) {
        try {
            if (DataParams.DB_DELETE_ALL == id) {
                mDbHelper?.delete(uri!!, null, null)
            } else {
                mDbHelper?.delete(uri!!, "_id <= ?", arrayOf(id))
            }
        } catch (ex: Exception) {
            LogUtils.printStackTrace(ex)
        }
    }

    fun parseData(keyData: String): String {
        var keyData = keyData
        try {
            if (TextUtils.isEmpty(keyData)) return ""
            val index = keyData.lastIndexOf("\t")
            if (index > -1) {
                val crc = keyData.substring(index).replaceFirst("\t".toRegex(), "")
                keyData = keyData.substring(0, index)
                if (TextUtils.isEmpty(keyData) || TextUtils.isEmpty(crc)
                    || crc != keyData.hashCode().toString()
                ) {
                    return ""
                }
            }
        } catch (ex: Exception) {
            LogUtils.printStackTrace(ex)
        }
        return keyData
    }

    /**
     * 数据库存满时删除数据
     *
     * @param uri URI
     * @return 正常返回 0
     */
    fun deleteDataLowMemory(uri: Uri?): Int {
        if (belowMemThreshold()) {
            LogUtils.i(
                TAG,
                "There is not enough space left on the device to store events, so will delete 100 oldest events"
            )
            val eventsData = queryData(uri, 100) ?: return DataParams.DB_OUT_OF_MEMORY_ERROR
            val lastId = eventsData[0]
            deleteData(uri, lastId)
            if (queryDataCount(uri) <= 0) {
                return DataParams.DB_OUT_OF_MEMORY_ERROR
            }
        }
        return 0
    }

    private fun getMaxCacheSize(context: Context): Long {
        return try {
            RoiqueryAnalyticsAPI.getInstance(context).maxCacheSize
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
            32 * 1024 * 1024
        }
    }

    private fun belowMemThreshold(): Boolean {
        return if (mDatabaseFile.exists()) {
            mDatabaseFile.length() >= getMaxCacheSize(mContext)
        } else false
    }

}
