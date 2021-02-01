package com.nodetower.analytics.data

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import com.nodetower.analytics.api.RoiqueryAnalyticsAPI
import com.nodetower.base.utils.LogUtils
import org.json.JSONObject
import java.io.File


internal abstract class DataOperation(private val mContext: Context) {
    var TAG = "EventDataOperation"
    var contentResolver: ContentResolver = mContext.contentResolver
    private val mDatabaseFile: File = mContext.getDatabasePath(DataParams.DATABASE_NAME)

    /**
     * 保存数据
     */
    abstract fun insertData(uri: Uri?, jsonObject: JSONObject?): Int?

    /**
     * 保存数据
     */
    abstract fun insertData(uri: Uri?, contentValues: ContentValues?): Int

    /**
     * 查询数据
     */
    abstract fun queryData(uri: Uri?, limit: Int): Array<String>?

    /**
     * 查询数据条数
     *
     * @param uri Uri
     * @return 条数
     */
    @JvmOverloads
    fun queryDataCount(
        uri: Uri?,
        projection: Array<String?>? = null,
        selection: String? = null,
        selectionArgs: Array<String?>? = null,
        sortOrder: String? = null
    ): Int {
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(uri!!, projection, selection, selectionArgs, sortOrder)
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
                contentResolver.delete(uri!!, null, null)
            } else {
                contentResolver.delete(uri!!, "_id <= ?", arrayOf(id))
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
