package com.nodetower.analytics.data


import android.content.*
import android.database.Cursor
import android.database.MatrixCursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import com.nodetower.analytics.data.db.EventDataDBHelper
import com.nodetower.analytics.data.persistent.PersistentGaid
import com.nodetower.analytics.data.persistent.PersistentLoader
import com.nodetower.analytics.data.persistent.PersistentLoginId
import com.nodetower.analytics.data.persistent.PersistentOaid
import com.nodetower.base.utils.LogUtils


class DbHelper(context: Context?) :
    SQLiteOpenHelper(context, DataParams.DATABASE_NAME, null, DataParams.DATABASE_VERSION) {


    private var isDbWritable = true

    override fun onCreate(db: SQLiteDatabase) {
        LogUtils.i(TAG, "Creating a new Analytics DB")
        db.execSQL(CREATE_EVENTS_TABLE)
        db.execSQL(EVENTS_TIME_INDEX)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        LogUtils.i(TAG, "Upgrading app, replacing Analytics DB")
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", DataParams.TABLE_EVENTS))
        db.execSQL(CREATE_EVENTS_TABLE)
        db.execSQL(EVENTS_TIME_INDEX)
    }


    companion object {
        private const val TAG = "SA.SQLiteOpenHelper"
        private val CREATE_EVENTS_TABLE = String.format(
            "CREATE TABLE %s (_id INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, %s INTEGER NOT NULL);",
            DataParams.TABLE_EVENTS,
            DataParams.KEY_DATA,
            DataParams.KEY_CREATED_AT
        )
        private val EVENTS_TIME_INDEX = String.format(
            "CREATE INDEX IF NOT EXISTS time_idx ON %s (%s);",
            DataParams.TABLE_EVENTS,
            DataParams.KEY_CREATED_AT
        )

    }

     fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        if (!isDbWritable) {
            return 0
        }
        var deletedCounts = 0
         try {
             val database: SQLiteDatabase = writableDatabase
             deletedCounts = database.delete(DataParams.TABLE_EVENTS, selection, selectionArgs)
         } catch (e: SQLiteException) {
             isDbWritable = false
             LogUtils.printStackTrace(e)
         }
        return deletedCounts
    }


     fun insert(uri: Uri, values: ContentValues?): Uri? {
        // 不处理 values = null 或者 values 为空的情况
        if (!isDbWritable || values == null || values.size() == 0) {
            return uri
        }
        try {
            return insertEvent(uri, values)
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
        return uri
    }

    private fun insertEvent(uri: Uri, values: ContentValues): Uri {
        val database: SQLiteDatabase
        try {
            database = writableDatabase
        } catch (e: SQLiteException) {
            isDbWritable = false
            LogUtils.printStackTrace(e)
            return uri
        }
        if (!values.containsKey(DataParams.KEY_DATA) || !values.containsKey(DataParams.KEY_CREATED_AT)) {
            return uri
        }
        val d = database.insert(DataParams.TABLE_EVENTS, "_id", values)
        return ContentUris.withAppendedId(uri, d)
    }

    private fun insertChannelPersistent(uri: Uri, values: ContentValues): Uri {
        val database: SQLiteDatabase
        try {
            database = writableDatabase
        } catch (e: SQLiteException) {
            isDbWritable = false
            LogUtils.printStackTrace(e)
            return uri
        }
        if (!values.containsKey(DataParams.KEY_CHANNEL_EVENT_NAME) || !values.containsKey(DataParams.KEY_CHANNEL_RESULT)) {
            return uri
        }
        val d = database.insertWithOnConflict(
            DataParams.TABLE_CHANNEL_PERSISTENT,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
        return ContentUris.withAppendedId(uri, d)
    }

     fun bulkInsert(uri: Uri, values: Array<ContentValues>): Int {
        if (!isDbWritable) {
            return 0
        }
        val numValues: Int
        var database: SQLiteDatabase? = null
        try {
            try {
                database = writableDatabase
            } catch (e: SQLiteException) {
                isDbWritable = false
                LogUtils.printStackTrace(e)
                return 0
            }
            database.beginTransaction()
            numValues = values.size
            for (i in 0 until numValues) {
                insert(uri, values[i])
            }
            database.setTransactionSuccessful()
        } finally {
            database?.endTransaction()
        }
        return numValues
    }

     fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        if (!isDbWritable) {
            return null
        }
        var cursor: Cursor? = null
        try {
            cursor =queryByTable(DataParams.TABLE_EVENTS, projection, selection, selectionArgs, sortOrder)
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
        return cursor
    }

    private fun queryByTable(
        tableName: String,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        var cursor: Cursor? = null
        try {
            cursor =writableDatabase
                .query(tableName, projection, selection, selectionArgs, null, null, sortOrder)
        } catch (e: SQLiteException) {
            isDbWritable = false
            LogUtils.printStackTrace(e)
        }
        return cursor
    }




}
