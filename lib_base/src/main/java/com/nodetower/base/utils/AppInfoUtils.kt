package com.nodetower.base.utils

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Process
import android.text.TextUtils


object AppInfoUtils {
    /**
     * 获取应用名称
     *
     * @param context Context
     * @return 应用名称
     */
    fun getAppName(context: Context?): CharSequence {
        if (context == null) return ""
        try {
            val packageManager: PackageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            return appInfo.loadLabel(packageManager)
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
        return ""
    }

    /**
     * 获取 App 的 ApplicationId
     *
     * @param context Context
     * @return ApplicationId
     */
    fun getProcessName(context: Context?): String {
        if (context == null) return ""
        try {
            return context.applicationInfo.processName
        } catch (ex: Exception) {
            LogUtils.printStackTrace(ex)
        }
        return ""
    }

    /**
     * 获取 App 版本号
     *
     * @param context Context
     * @return App 的版本号
     */
    fun getAppVersionName(context: Context?): String {
        if (context == null) return ""
        try {
            val packageManager: PackageManager = context.packageManager
            val packageInfo: PackageInfo =
                packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionName
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
        return ""
    }

    /**
     * 获取主进程的名称
     *
     * @param context Context
     * @return 主进程名称
     */
    fun getMainProcessName(context: Context?): String {
        if (context == null) {
            return ""
        }
        try {
            return context.applicationContext.applicationInfo.processName
        } catch (ex: Exception) {
            LogUtils.printStackTrace(ex)
        }
        return ""
    }

    /**
     * 判断当前进程名称是否为主进程
     *
     * @param context Context
     * @param mainProcessName 进程名
     * @return 是否主进程
     */
    fun isMainProcess(context: Context?, mainProcessName: String): Boolean {
        if (context == null) {
            return false
        }
        if (TextUtils.isEmpty(mainProcessName)) {
            return true
        }
        val currentProcess = getCurrentProcessName(context.applicationContext)
        return TextUtils.isEmpty(currentProcess) || mainProcessName == currentProcess
    }

    /**
     * 获得当前进程的名字
     *
     * @param context Context
     * @return 进程名称
     */
    private fun getCurrentProcessName(context: Context): String? {
        try {
            val pid = Process.myPid()
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningAppProcessInfoList = activityManager.runningAppProcesses
            if (runningAppProcessInfoList != null) {
                for (appProcess in runningAppProcessInfoList) {
                    if (appProcess != null) {
                        if (appProcess.pid == pid) {
                            return appProcess.processName
                        }
                    }
                }
            }
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
        return null
    }
}
