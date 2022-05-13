package com.roiquery.analytics.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Process
import android.text.TextUtils
import android.webkit.WebSettings


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
     * 获取 App 版本名
     *
     * @param context Context
     * @return App 的版本名
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
     * 获取 App 版本号
     *
     * @param context Context
     * @return App 的版本号
     */
    fun getAppVersionCode(context: Context?): Int {
        if (context == null) return -1
        try {
            val packageManager: PackageManager = context.packageManager
            val packageInfo: PackageInfo =
                packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionCode
        } catch (e: Exception) {
            LogUtils.printStackTrace(e)
        }
        return -1
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
    fun getCurrentProcessName(context: Context): String? {
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

    fun getAppVersionName(context: Context, pkg: String): String{
        var vn = ""
        try {
            val packageManager: PackageManager = context.applicationContext.packageManager
            val info = packageManager.getPackageInfo(pkg, 0)
            vn = info.versionName
        }catch (e: Exception){

        }
        return vn
    }


    fun getAppVersionCode(context: Context, pkg: String): Int {
        var vc = 1
        try {
            val packageManager: PackageManager = context.applicationContext.packageManager
            val info = packageManager.getPackageInfo(pkg, 0)
            vc = info.versionCode
        }catch (e: Exception){

        }
        return vc
    }

    /**
     * 桌面应用
     *
     * @param context
     * @return
     */
    fun getLauncherPackageName(context: Context): String? {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val res = context.packageManager.resolveActivity(intent, 0) ?: return "\$unknown"
        return if (res.activityInfo == null) {
            "\$unknown"
        } else res.activityInfo.packageName
    }

     fun getDefaultUserAgent(context: Context): String? {
        var ua: String? = null
        try {
            ua = System.getProperty("http.agent")
            if (TextUtils.isEmpty(ua)) {
                val localMethod =
                    WebSettings::class.java.getDeclaredMethod(
                        "getDefaultUserAgent", *arrayOf<Class<*>>(
                            Context::class.java
                        )
                    )
                ua = localMethod.invoke(WebSettings::class.java, *arrayOf<Any>(context)) as String
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return if (TextUtils.isEmpty(ua)) "" else ua
    }
}
