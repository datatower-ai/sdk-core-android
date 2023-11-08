package ai.datatower.analytics.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager


object AppInfoUtils {


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


}
