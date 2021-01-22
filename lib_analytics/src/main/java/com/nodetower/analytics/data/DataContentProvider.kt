package com.nodetower.analytics.data


import android.content.*
import android.database.Cursor
import android.database.MatrixCursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.net.Uri
import com.nodetower.analytics.data.db.EventDataDBHelper
import com.nodetower.analytics.data.persistent.PersistentLoginId
import com.nodetower.base.utils.LogUtils



class DataContentProvider : ContentProvider() {
    private var dbHelper: EventDataDBHelper? = null
    private var contentResolver: ContentResolver? = null
//    private var persistentAppStartTime: PersistentAppStartTime? = null
//    private var persistentAppEndData: PersistentAppEndData? = null
//    private var persistentAppPaused: PersistentAppPaused? = null
    private var persistentLoginId: PersistentLoginId? = null
//    private var persistentFlushDataState: PersistentFlushDataState? = null
    private var isDbWritable = true
    private var isFirstProcessStarted = true
    private var startActivityCount = 0
    private var mSessionTime = 30 * 1000


    override fun onCreate(): Boolean {
        val context = context
        if (context != null) {
            //这里是为了使用 ProviderTestRule
            val packageName: String = try {
                context.applicationContext.packageName
            } catch (e: UnsupportedOperationException) {
                "com.sensorsdata.analytics.android.sdk.test"
            }
            val authority = "$packageName.AnalyticsDataContentProvider"
            contentResolver = context.contentResolver
            uriMatcher.addURI(authority, DbParams.TABLE_EVENTS, EVENTS)
            uriMatcher.addURI(authority, DbParams.TABLE_ACTIVITY_START_COUNT, ACTIVITY_START_COUNT)
            uriMatcher.addURI(authority, DbParams.TABLE_APP_START_TIME, APP_START_TIME)
            uriMatcher.addURI(authority, DbParams.TABLE_APP_END_DATA, APP_END_DATA)
            uriMatcher.addURI(authority, DbParams.TABLE_APP_END_TIME, APP_PAUSED_TIME)
            uriMatcher.addURI(
                authority,
                DbParams.TABLE_SESSION_INTERVAL_TIME,
                SESSION_INTERVAL_TIME
            )
            uriMatcher.addURI(authority, DbParams.TABLE_LOGIN_ID, LOGIN_ID)
            uriMatcher.addURI(authority, DbParams.TABLE_CHANNEL_PERSISTENT, CHANNEL_PERSISTENT)
            uriMatcher.addURI(authority, DbParams.TABLE_SUB_PROCESS_FLUSH_DATA, FLUSH_DATA)
            uriMatcher.addURI(authority, DbParams.TABLE_FIRST_PROCESS_START, FIRST_PROCESS_START)
            dbHelper = EventDataDBHelper(context)

//            PersistentLoader.initLoader(context)
//            persistentAppEndData =
//                PersistentLoader.loadPersistent(DbParams.TABLE_APP_END_DATA) as PersistentAppEndData
//            persistentAppStartTime =
//                PersistentLoader.loadPersistent(DbParams.TABLE_APP_START_TIME) as PersistentAppStartTime
//            persistentAppPaused =
//                PersistentLoader.loadPersistent(DbParams.TABLE_APP_END_TIME) as PersistentAppPaused
//            persistentLoginId =
//                PersistentLoader.loadPersistent(DbParams.TABLE_LOGIN_ID) as PersistentLoginId
//            persistentFlushDataState =
//                PersistentLoader.loadPersistent(DbParams.TABLE_SUB_PROCESS_FLUSH_DATA) as PersistentFlushDataState
        }
        return true
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        if (!isDbWritable) {
            return 0
        }
        var deletedCounts = 0
        try {
            val code = uriMatcher.match(uri)
            if (EVENTS == code) {
                try {
                    val database: SQLiteDatabase = dbHelper!!.writableDatabase
                    deletedCounts = database.delete(DbParams.TABLE_EVENTS, selection, selectionArgs)
                } catch (e: SQLiteException) {
                    isDbWritable = false
                    LogUtils.printStackTrace(e)
                }
            }
            //目前逻辑不处理其他 Code
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
        return deletedCounts
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // 不处理 values = null 或者 values 为空的情况
        if (!isDbWritable || values == null || values.size() == 0) {
            return uri
        }
        try {
            val code = uriMatcher.match(uri)
            if (code == EVENTS) {
                return insertEvent(uri, values)
            } else if (code == CHANNEL_PERSISTENT) {
                return insertChannelPersistent(uri, values)
            } else {
                insert(code, uri, values)
            }
            return uri
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
        return uri
    }

    private fun insertEvent(uri: Uri, values: ContentValues): Uri {
        val database: SQLiteDatabase
        try {
            database = dbHelper!!.writableDatabase
        } catch (e: SQLiteException) {
            isDbWritable = false
            LogUtils.printStackTrace(e)
            return uri
        }
        if (!values.containsKey(DbParams.KEY_DATA) || !values.containsKey(DbParams.KEY_CREATED_AT)) {
            return uri
        }
        val d = database.insert(DbParams.TABLE_EVENTS, "_id", values)
        return ContentUris.withAppendedId(uri, d)
    }

    private fun insertChannelPersistent(uri: Uri, values: ContentValues): Uri {
        val database: SQLiteDatabase
        try {
            database = dbHelper!!.writableDatabase
        } catch (e: SQLiteException) {
            isDbWritable = false
            LogUtils.printStackTrace(e)
            return uri
        }
        if (!values.containsKey(DbParams.KEY_CHANNEL_EVENT_NAME) || !values.containsKey(DbParams.KEY_CHANNEL_RESULT)) {
            return uri
        }
        val d = database.insertWithOnConflict(
            DbParams.TABLE_CHANNEL_PERSISTENT,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
        return ContentUris.withAppendedId(uri, d)
    }

    override fun bulkInsert(uri: Uri, values: Array<ContentValues>): Int {
        if (!isDbWritable) {
            return 0
        }
        val numValues: Int
        var database: SQLiteDatabase? = null
        try {
            try {
                database = dbHelper!!.writableDatabase
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

    override fun query(
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
            val code = uriMatcher.match(uri)
            cursor = if (code == EVENTS) {
                queryByTable(DbParams.TABLE_EVENTS, projection, selection, selectionArgs, sortOrder)
            } else if (code == CHANNEL_PERSISTENT) {
                queryByTable(
                    DbParams.TABLE_CHANNEL_PERSISTENT,
                    projection,
                    selection,
                    selectionArgs,
                    sortOrder
                )
            } else {
                query(code)
            }
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
            cursor = dbHelper!!.writableDatabase
                .query(tableName, projection, selection, selectionArgs, null, null, sortOrder)
        } catch (e: SQLiteException) {
            isDbWritable = false
            LogUtils.printStackTrace(e)
        }
        return cursor
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        return 0
    }

    /**
     * insert 处理
     *
     * @param code Uri code
     * @param uri Uri
     * @param values ContentValues
     */
    private fun insert(code: Int, uri: Uri, values: ContentValues) {
        when (code) {
            ACTIVITY_START_COUNT -> startActivityCount = values.getAsInteger(DbParams.TABLE_ACTIVITY_START_COUNT)

//            APP_START_TIME -> persistentAppStartTime.commit(values.getAsLong(DbParams.TABLE_APP_START_TIME))
//
//            APP_PAUSED_TIME -> persistentAppPaused.commit(values.getAsLong(DbParams.TABLE_APP_END_TIME))
//
//            APP_END_DATA -> persistentAppEndData.commit(values.getAsString(DbParams.TABLE_APP_END_DATA))

            SESSION_INTERVAL_TIME -> {
                mSessionTime = values.getAsInteger(DbParams.TABLE_SESSION_INTERVAL_TIME)
                contentResolver!!.notifyChange(uri, null)
            }

//            LOGIN_ID -> persistentLoginId.commit(values.getAsString(DbParams.TABLE_LOGIN_ID))
//
//            FLUSH_DATA -> persistentFlushDataState.commit(values.getAsBoolean(DbParams.TABLE_SUB_PROCESS_FLUSH_DATA))

            FIRST_PROCESS_START -> isFirstProcessStarted =
                values.getAsBoolean(DbParams.TABLE_FIRST_PROCESS_START)
            else -> {

            }
        }
    }

    /**
     * query 处理
     *
     * @param code Uri code
     * @return Cursor
     */
    private fun query(code: Int): Cursor {
        var column: String? = null
        var data: Any? = null
        when (code) {
            ACTIVITY_START_COUNT -> {
                data = startActivityCount
                column = DbParams.TABLE_ACTIVITY_START_COUNT
            }
//            APP_START_TIME -> {
//                data = persistentAppStartTime.get()
//                column = DbParams.TABLE_APP_START_TIME
//            }
//            APP_PAUSED_TIME -> {
//                data = persistentAppPaused.get()
//                column = DbParams.TABLE_APP_END_TIME
//            }
//            APP_END_DATA -> {
//                data = persistentAppEndData.get()
//                column = DbParams.TABLE_APP_END_DATA
//            }
            SESSION_INTERVAL_TIME -> {
                data = mSessionTime
                column = DbParams.TABLE_SESSION_INTERVAL_TIME
            }
            LOGIN_ID -> {
                data = persistentLoginId?.get()
                column = DbParams.TABLE_LOGIN_ID
            }
//            FLUSH_DATA -> synchronized(
//                SensorsDataContentProvider::class.java
//            ) {
//                if (persistentFlushDataState.get()) {
//                    data = 1
//                } else {
//                    data = 0
//                    persistentFlushDataState.commit(true)
//                }
//                column = DbParams.TABLE_SUB_PROCESS_FLUSH_DATA
//            }
            FIRST_PROCESS_START -> {
                data = if (isFirstProcessStarted) 1 else 0
                column = DbParams.TABLE_FIRST_PROCESS_START
            }
            else -> {
            }
        }
        val matrixCursor = MatrixCursor(arrayOf(column))
        matrixCursor.addRow(arrayOf(data))
        return matrixCursor
    }

    companion object {
        private const val EVENTS = 1
        private const val ACTIVITY_START_COUNT = 2
        private const val APP_START_TIME = 3
        private const val APP_END_DATA = 4
        private const val APP_PAUSED_TIME = 5
        private const val SESSION_INTERVAL_TIME = 6
        private const val LOGIN_ID = 7
        private const val CHANNEL_PERSISTENT = 8
        private const val FLUSH_DATA = 9
        private const val FIRST_PROCESS_START = 10
        private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
    }
}
