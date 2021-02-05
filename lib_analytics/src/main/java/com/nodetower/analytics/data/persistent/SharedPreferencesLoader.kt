package com.nodetower.analytics.data.persistent

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.*


internal class SharedPreferencesLoader {
    private val mExecutor: Executor
    fun loadPreferences(context: Context, name: String?): Future<SharedPreferences> {
        val loadSharedPrefs = LoadSharedPreferences(context, name)
        val task: FutureTask<SharedPreferences> = FutureTask(loadSharedPrefs)
        mExecutor.execute(task)
        return task
    }

    private class LoadSharedPreferences  constructor(context: Context, prefsName: String?) :
        Callable<SharedPreferences> {
        private val mContext: Context = context
        private val mPrefsName: String? = prefsName
        override fun call(): SharedPreferences {
            return mContext.getSharedPreferences(mPrefsName, Context.MODE_PRIVATE)
        }
    }
    init {
        mExecutor = Executors.newSingleThreadExecutor()
    }
}
