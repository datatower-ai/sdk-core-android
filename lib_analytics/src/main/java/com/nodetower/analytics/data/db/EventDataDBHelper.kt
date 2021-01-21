package com.nodetower.analytics.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.nodetower.analytics.data.DbParams
import com.nodetower.base.utils.LogUtils


internal class EventDataDBHelper(context: Context?) :
    SQLiteOpenHelper(context, DbParams.DATABASE_NAME, null, DbParams.DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        LogUtils.i(TAG, "Creating a new Analytics DB")
        db.execSQL(CREATE_EVENTS_TABLE)
        db.execSQL(EVENTS_TIME_INDEX)
//        db.execSQL(CHANNEL_EVENT_PERSISTENT_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        LogUtils.i(TAG, "Upgrading app, replacing Analytics DB")
        db.execSQL(String.format("DROP TABLE IF EXISTS %s", DbParams.TABLE_EVENTS))
        db.execSQL(CREATE_EVENTS_TABLE)
        db.execSQL(EVENTS_TIME_INDEX)
//        db.execSQL(CHANNEL_EVENT_PERSISTENT_TABLE)
    }

    companion object {
        private const val TAG = "SA.SQLiteOpenHelper"
        private val CREATE_EVENTS_TABLE = String.format(
            "CREATE TABLE %s (_id INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT NOT NULL, %s INTEGER NOT NULL);",
            DbParams.TABLE_EVENTS,
            DbParams.KEY_DATA,
            DbParams.KEY_CREATED_AT
        )
        private val EVENTS_TIME_INDEX = String.format(
            "CREATE INDEX IF NOT EXISTS time_idx ON %s (%s);",
            DbParams.TABLE_EVENTS,
            DbParams.KEY_CREATED_AT
        )
        private val CHANNEL_EVENT_PERSISTENT_TABLE = String.format(
            "CREATE TABLE %s (%s TEXT PRIMARY KEY, %s INTEGER)",
            DbParams.TABLE_CHANNEL_PERSISTENT,
            DbParams.KEY_CHANNEL_EVENT_NAME,
            DbParams.KEY_CHANNEL_RESULT
        )
    }
}
