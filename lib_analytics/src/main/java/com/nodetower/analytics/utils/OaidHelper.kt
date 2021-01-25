package com.nodetower.analytics.utils


import android.content.Context
import android.text.TextUtils
import com.nodetower.base.utils.LogUtils
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.CountDownLatch


object OaidHelper {
    private const val TAG = "SA.DeviceUtils"

    // OAID
    private var mOAID = ""
    private var mCountDownLatch: CountDownLatch? = null
    private var mIdentifyListener: Class<*>? = null
    private var mIdSupplier: Class<*>? = null
    private var jLibrary: Class<*>? = null
    private var mMidSDKHelper: Class<*>? = null

    /**
     * 获取 OAID 接口，注意该接口是同步接口，可能会导致线程阻塞，建议在子线程中使用
     *
     * @param context Context
     * @return OAID
     */
    @JvmStatic
    fun getOAID(context: Context): String {
        try {
            mCountDownLatch = CountDownLatch(1)
            initInvokeListener()
            if (mMidSDKHelper == null || mIdentifyListener == null || mIdSupplier == null) {
                LogUtils.d(TAG, "OAID 读取类创建失败")
                return ""
            }
            if (TextUtils.isEmpty(mOAID)) {
                getOAIDReflect(context, 2)
            } else {
                return mOAID
            }
            try {
                mCountDownLatch!!.await()
            } catch (e: InterruptedException) {
                LogUtils.printStackTrace(e)
            }
            LogUtils.d(TAG, "CountDownLatch await")
            return mOAID
        } catch (ex: Exception) {
            LogUtils.printStackTrace(ex)
        }
        return ""
    }

    /**
     * 通过反射获取 OAID，结果返回的 ErrorCode 如下：
     * 1008611：不支持的设备厂商
     * 1008612：不支持的设备
     * 1008613：加载配置文件出错
     * 1008614：获取接口是异步的，结果会在回调中返回，回调执行的回调可能在工作线程
     * 1008615：反射调用出错
     *
     * @param context Context
     * @param retryCount 重试次数
     */
    private fun getOAIDReflect(context: Context, retryCount: Int) {
        var retryCount = retryCount
        try {
            if (retryCount == 0) {
                return
            }
            val INIT_ERROR_RESULT_DELAY = 1008614 //获取接口是异步的，结果会在回调中返回，回调执行的回调可能在工作线程
            // 初始化 Library
            if (jLibrary != null) {
                val initEntry = jLibrary!!.getDeclaredMethod(
                    "InitEntry",
                    Context::class.java
                )
                initEntry.invoke(null, context)
            }
            val handler = IdentifyListenerHandler()
            val initSDK = mMidSDKHelper!!.getDeclaredMethod(
                "InitSdk",
                Context::class.java,
                Boolean::class.javaPrimitiveType, mIdentifyListener
            )
            val errCode = initSDK.invoke(
                null, context, true, Proxy.newProxyInstance(
                    context.classLoader, arrayOf(
                        mIdentifyListener
                    ), handler
                )
            ) as Int
            LogUtils.d(TAG, "MdidSdkHelper ErrorCode : $errCode")
            if (errCode != INIT_ERROR_RESULT_DELAY) {
                getOAIDReflect(context, --retryCount)
                if (retryCount == 0) {
                    mCountDownLatch!!.countDown()
                }
            }

            /*
             * 此处是为了适配三星部分手机，根据 MSA 工作人员反馈，对于三星部分机型的支持有 bug，导致
             * 返回 1008614 错误码，但是不会触发回调。所以此处的逻辑是，两秒之后主动放弃阻塞。
             */Thread {
                try {
                    Thread.sleep(2000)
                } catch (e: InterruptedException) {
                    //ignore
                }
                mCountDownLatch!!.countDown()
            }.start()
        } catch (ex: Exception) {
            LogUtils.printStackTrace(ex)
            getOAIDReflect(context, --retryCount)
            if (retryCount == 0) {
                mCountDownLatch!!.countDown()
            }
        }
    }

    private fun initInvokeListener() {
        try {
            mMidSDKHelper = Class.forName("com.bun.miitmdid.core.MdidSdkHelper")
        } catch (e: ClassNotFoundException) {
            LogUtils.printStackTrace(e)
            return
        }
        // 尝试 1.0.22 版本
        try {
            mIdentifyListener = Class.forName("com.bun.miitmdid.interfaces.IIdentifierListener")
            mIdSupplier = Class.forName("com.bun.miitmdid.interfaces.IdSupplier")
            return
        } catch (ex: Exception) {
            // ignore
        }

        // 尝试 1.0.13 - 1.0.21 版本
        try {
            mIdentifyListener = Class.forName("com.bun.supplier.IIdentifierListener")
            mIdSupplier = Class.forName("com.bun.supplier.IdSupplier")
            jLibrary = Class.forName("com.bun.miitmdid.core.JLibrary")
            return
        } catch (ex: Exception) {
            // ignore
        }

        // 尝试 1.0.5 - 1.0.13 版本
        try {
            mIdentifyListener = Class.forName("com.bun.miitmdid.core.IIdentifierListener")
            mIdSupplier = Class.forName("com.bun.miitmdid.supplier.IdSupplier")
            jLibrary = Class.forName("com.bun.miitmdid.core.JLibrary")
        } catch (ex: Exception) {
            // ignore
        }
    }

    internal class IdentifyListenerHandler : InvocationHandler {
        @Throws(Throwable::class)
        override fun invoke(proxy: Any, method: Method, args: Array<Any>): Any? {
            try {
                if ("OnSupport" == method.name) {
                    if (args[0] as Boolean) {
                        val getOAID = mIdSupplier!!.getDeclaredMethod("getOAID")
                        mOAID = getOAID.invoke(args[1]) as String
                        LogUtils.d(TAG, "oaid:" + mOAID)
                    }
                    mCountDownLatch!!.countDown()
                }
            } catch (ex: Exception) {
                mCountDownLatch!!.countDown()
            }
            return null
        }
    }
}