package ai.datatower.analytics.utils


import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.view.Surface
import android.view.WindowManager
import android.webkit.WebSettings
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import java.util.*


object DeviceUtils {
    private const val TAG = "DeviceUtils"

    private val sManufacturer: ArrayList<String?> = object : ArrayList<String?>() {
        init {
            add("HUAWEI")
            add("OPPO")
            add("vivo")
        }
    }
    val oS: String
        get() = if (Build.VERSION.RELEASE == null) "UNKNOWN" else Build.VERSION.RELEASE
    val manufacturer: String
        get() {
            val manufacturer =
                if (Build.MANUFACTURER == null) "UNKNOWN" else Build.MANUFACTURER.trim { it <= ' ' }
            try {
                if (!TextUtils.isEmpty(manufacturer)) {
                    for (item in sManufacturer) {
                        if (item.equals(manufacturer, ignoreCase = true)) {
                            return item!!
                        }
                    }
                }
            } catch (e: Exception) {
                LogUtils.printStackTrace(e)
            }
            return manufacturer
        }
    val model: String
        get() = if (TextUtils.isEmpty(Build.MODEL)) "UNKNOWN" else Build.MODEL.trim { it <= ' ' }
    val brand: String
        get() = if (TextUtils.isEmpty(Build.BRAND)) "UNKNOWN" else Build.BRAND.trim { it <= ' ' }





    /**
     * 获取 dpi
     *
     * @param context
     */
    fun getDensityDpi(context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.densityDpi
    }


    /**
     * 获取屏幕的宽高信息
     *
     * @param context Context
     * @return 宽高信息
     */
    fun getDeviceSize(context: Context): IntArray {
        val size = IntArray(2)
        try {
            val screenWidth: Int
            val screenHeight: Int
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val rotation = display.rotation
            val point = Point()
            display.getRealSize(point)
            screenWidth = point.x
            screenHeight = point.y
            size[0] = getNaturalWidth(rotation, screenWidth, screenHeight)
            size[1] = getNaturalHeight(rotation, screenWidth, screenHeight)
        } catch (e: Exception) {
            //context.getResources().getDisplayMetrics()这种方式获取屏幕高度不包括底部虚拟导航栏
            if (context.resources != null) {
                val displayMetrics = context.resources.displayMetrics
                size[0] = displayMetrics.widthPixels
                size[1] = displayMetrics.heightPixels
            }
        }
        return size
    }

    /**
     * 根据设备 rotation，判断屏幕方向，获取自然方向宽
     *
     * @param rotation 设备方向
     * @param width 逻辑宽
     * @param height 逻辑高
     * @return 自然尺寸
     */
    private fun getNaturalWidth(rotation: Int, width: Int, height: Int): Int {
        return if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) width else height
    }

    /**
     * 根据设备 rotation，判断屏幕方向，获取自然方向高
     *
     * @param rotation 设备方向
     * @param width 逻辑宽
     * @param height 逻辑高
     * @return 自然尺寸
     */
    private fun getNaturalHeight(rotation: Int, width: Int, height: Int): Int {
        return if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) height else width
    }


    /**
     * 获取 Android ID
     *
     * @param context Context
     * @return androidID
     */
    @SuppressLint("HardwareIds")
    fun getAndroidID(context: Context): String {
        var androidID = ""
        try {
            androidID =
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: java.lang.Exception) {
            LogUtils.printStackTrace(e)
        }
        return androidID
    }



    fun getMcc(context: Context):String {
        val tel = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val networkOperator = tel.networkOperator
        if (!TextUtils.isEmpty(networkOperator)) {
            return networkOperator.substring(0, 3)
        }
        return ""

    }

    fun getMnc(context: Context):String {
        val tel = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val networkOperator = tel.networkOperator
        if (!TextUtils.isEmpty(networkOperator)) {
            return networkOperator.substring(3)
        }
        return ""
    }

//    fun getLocalCountry(context: Context):String {
//        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) context.resources.configuration.locales[0].country
//            else context.resources.configuration.locale.country
//     }

    fun getLocalCountry(context: Context): String {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        var country = tm.simCountryIso
        if (TextUtils.isEmpty(country)) {
            country = getLocale().country
        }
        if (TextUtils.isEmpty(country)) {
            country = ""
        }
        return country.uppercase(Locale.US)
    }

    private fun getLocale(): Locale {
        val locale: Locale = try {
            val listCompat: LocaleListCompat = ConfigurationCompat.getLocales(Resources.getSystem().configuration)
            listCompat.get(0)
        } catch (e: Exception) {
            Locale.getDefault()
        }
        return locale
    }

    fun getLocaleLanguage():String{
        return Locale.getDefault().language
    }


    /**
     * 获取userAgent
     */
    fun getUserAgent(context: Context): String {
        var ua: String = ""
        try {
            ua = WebSettings.getDefaultUserAgent(context)
        } catch (_: Exception) {
        }
        return ua
    }

    /**
     * 检测权限F
     *
     * @param context Context
     * @param permission 权限名称
     * @return true:已允许该权限; false:没有允许该权限
     */
    fun checkHasPermission(context: Context, permission: String): Boolean {
        return try {
            var contextCompat: Class<*>? = null
            try {
                contextCompat = Class.forName("android.support.v4.content.ContextCompat")
            } catch (e: java.lang.Exception) {
                //ignored
            }
            if (contextCompat == null) {
                try {
                    contextCompat = Class.forName("androidx.core.content.ContextCompat")
                } catch (e: java.lang.Exception) {
                    //ignored
                }
            }
            if (contextCompat == null) {
                return true
            }
            val checkSelfPermissionMethod = contextCompat.getMethod(
                "checkSelfPermission",
                Context::class.java,
                String::class.java
            )
            val result =
                checkSelfPermissionMethod.invoke(null, *arrayOf(context, permission)) as Int
            if (result != PackageManager.PERMISSION_GRANTED) {
                LogUtils.i(
                    TAG,
                    """You can fix this by adding the following to your AndroidManifest.xml file:
<uses-permission android:name="$permission" />"""
                )
                return false
            }
            true
        } catch (e: java.lang.Exception) {
            LogUtils.i(TAG, e.toString())
            true
        }
    }

}
