package com.roiquery.analytics.utils

import android.app.Application
import android.content.Context
import android.os.Build
import android.text.TextUtils
import androidx.annotation.RequiresApi

object ProcessUtils {
    fun isInMainProcess(context: Context): Boolean {
        val appContext = context.applicationContext
        val processName = getProcessName(appContext)
        return processName == null || TextUtils.equals(processName, appContext.packageName)
    }

    fun isInServiceProcess(context: Context): Boolean {
        val appContext = context.applicationContext
        val processName = getProcessName(appContext)
        return processName != null && processName.endsWith(":fssvc")
    }

    private var sProcessName: String? = null
    fun getProcessName(context: Context?): String? {
        return if (sProcessName != null) {
            sProcessName
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            processNameAPI28
        } else {
            val applicationContext = context?.applicationContext
            if (applicationContext is Application) {
                sProcessName = getProcessNameViaReflection(applicationContext)
                sProcessName
            } else {
                null
            }
        }
    }

    private val processNameAPI28: String?
         @RequiresApi(Build.VERSION_CODES.P)
         get() = try {
             sProcessName = Application.getProcessName()
             sProcessName
        } catch (var1: Exception) {
            null
        }

    private fun getProcessNameViaReflection(app: Application): String? {
        return try {
            val loadedApkField = app.javaClass.getField("mLoadedApk")
            loadedApkField.isAccessible = true
            val loadedApk = loadedApkField[app]
            val activityThreadField = loadedApk.javaClass.getDeclaredField("mActivityThread")
            activityThreadField.isAccessible = true
            val activityThread = activityThreadField[loadedApk]
            val getProcessName = activityThread.javaClass.getDeclaredMethod("getProcessName", null)
            getProcessName.invoke(activityThread, null) as String
        } catch (var6: Exception) {
            null
        }
    }
}
