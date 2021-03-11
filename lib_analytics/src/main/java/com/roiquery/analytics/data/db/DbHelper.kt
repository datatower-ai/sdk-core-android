package com.roiquery.analytics.data.db


import android.content.*
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri
import com.roiquery.analytics.data.DataParams
import com.roiquery.analytics.utils.LogUtils


class DbHelper(context: Context?) :
    SQLiteOpenHelper(context, DataParams.DATABASE_NAME, null, DataParams.DATABASE_VERSION) {

    private var isDbWritable = true

    override fun onCreate(db: SQLiteDatabase) {
        LogUtils.i(TAG, "Creating a new Analytics DB")
        db.execSQL(CREATE_EVENTS_TABLE)
        db.execSQL(CREATE_CONFIGS_TABLE)
        db.execSQL(EVENTS_TIME_INDEX)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        LogUtils.i(TAG, "Upgrading app, replacing Analytics DB")
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", DataParams.TABLE_EVENTS))
        db.execSQL(CREATE_EVENTS_TABLE)
        db.execSQL(CREATE_CONFIGS_TABLE)
        db.execSQL(EVENTS_TIME_INDEX)
    }


    companion object {
        private const val TAG = "SQLiteOpenHelper"
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

        private val CREATE_CONFIGS_TABLE = String.format(
            "CREATE TABLE %s (_id INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, %s TEXT NOT NULL);",
            DataParams.TABLE_CONFIGS,
            DataParams.KEY_CONFIG_NAME,
            DataParams.KEY_CONFIG_VALUE
        )

    }

    /* event */

     fun delete(selection: String?, selectionArgs: Array<String>?): Int {
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

     fun insertConfig(values: ContentValues): Long {
        val database: SQLiteDatabase
        try {
            database = writableDatabase
        } catch (e: SQLiteException) {
            isDbWritable = false
            LogUtils.printStackTrace(e)
            return DataParams.DB_INSERT_ERROR
        }
         return if (!values.containsKey(DataParams.KEY_CONFIG_NAME) || !values.containsKey(
                 DataParams.KEY_CONFIG_VALUE
             )) {
             DataParams.DB_INSERT_ERROR
        }else database.insert(DataParams.TABLE_CONFIGS, DataParams.KEY_CONFIG_NAME, values)
    }

     fun updateConfig(name:String,values: ContentValues): Int {
        val database: SQLiteDatabase
        try {
            database = writableDatabase
        } catch (e: SQLiteException) {
            isDbWritable = false
            LogUtils.printStackTrace(e)
            return DataParams.DB_UPDATE_CONFIG_ERROR
        }
         return if (!values.containsKey(DataParams.KEY_CONFIG_VALUE)) {
             DataParams.DB_UPDATE_CONFIG_ERROR
        }else database.update(DataParams.TABLE_CONFIGS, values, DataParams.KEY_CONFIG_NAME + "=?",arrayOf(name))
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
            cursor = queryByTable(DataParams.TABLE_EVENTS, projection, selection, selectionArgs, sortOrder)
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
        return cursor
    }

     fun queryByTable(
        tableName: String,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        var cursor: Cursor? = null
        try {
            cursor = writableDatabase
                .query(tableName, projection, selection, selectionArgs, null, null, sortOrder)
        } catch (e: SQLiteException) {
            isDbWritable = false
            LogUtils.printStackTrace(e)
        }
        return cursor
    }

}
