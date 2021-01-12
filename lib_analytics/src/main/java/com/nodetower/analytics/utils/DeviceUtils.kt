package com.nodetower.analytics.utils


import android.content.Context
import android.graphics.Point
import android.os.Build
import android.text.TextUtils
import android.view.Surface
import android.view.WindowManager
import com.nodetower.base.utils.LogUtils
import java.util.*


object DeviceUtils {
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealSize(point)
                screenWidth = point.x
                screenHeight = point.y
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                display.getSize(point)
                screenWidth = point.x
                screenHeight = point.y
            } else {
                screenWidth = display.width
                screenHeight = display.height
            }
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
}
