package com.roiquery.cloudconfig

import android.content.Context
import com.roiquery.cloudconfig.core.ConfigFetchListener
import com.roiquery.cloudconfig.core.ResourceRemoteRepository
import org.json.JSONObject

class ROIQueryCloudConfig {

    companion object {
        private val mRemoteAppConfig by lazy { remoteConfig<String>() }
        private var mIsInitialized = false
        internal var mLogger: ((String) -> Unit)? = null


        fun init(
            context: Context,
            remoteResource: ResourceRemoteRepository,
            aesKey: String,
            setAesKey: (String) -> Unit,
            logger: ((String) -> Unit)?
        ) {
            if (!mIsInitialized) {
                mLogger = logger
                initRemoteConfig {
                    remoteResource<String>(
                        storage(context, context.filesDir.absolutePath + "/configs"),
                        remoteResource,
                        aesKey,
                        setAesKey
                    ) {
                        resourceName = "cloud_config"
                    }
                    this.logger = logger
                }
                mRemoteAppConfig.setDefaultConfig("{}")
                fetch()
                mIsInitialized = true
            }
        }

        private fun getConfigJsonObject(): JSONObject? {
            return try {
                if (mIsInitialized) mRemoteAppConfig.get()?.let {
                    JSONObject(it)
                } else JSONObject()
            } catch (e: Exception) {
                mLogger?.invoke(e.message ?: "")
                JSONObject()
            }

        }

        fun getConfigString(): String? {
            return if (mIsInitialized) mRemoteAppConfig.get() else ""
        }

        @JvmStatic
        fun fetch(listener: ConfigFetchListener? = null) {
            mRemoteAppConfig.fetch({
                listener?.let {
                    listener.onSuccess()
                }
            }) { error ->
                listener?.let {
                    error.message?.let { it1 -> listener.onError(it1) }
                }
            }
        }

        @JvmOverloads
        fun getInt(
            key: String,
            defaultValue: Int = 0
        ) = try {
            getConfigJsonObject()?.getInt(key) ?: defaultValue
        } catch (e: Exception) {
            defaultValue
        }


        @JvmOverloads
        fun getString(
            key: String,
            defaultValue: String = ""
        ) = try {
            getConfigJsonObject()?.getString(key) ?: defaultValue
        } catch (e: Exception) {
            defaultValue
        }


        @JvmOverloads
        fun getBoolean(
            key: String,
            defaultValue: Boolean = false
        ) = try {
            getConfigJsonObject()?.getBoolean(key) ?: defaultValue
        } catch (e: Exception) {
            defaultValue
        }


        @JvmOverloads
        fun getDouble(
            key: String,
            defaultValue: Double = 0.0
        ) = try {
            getConfigJsonObject()?.getDouble(key) ?: defaultValue
        } catch (e: Exception) {
            defaultValue
        }

        @JvmOverloads
        fun getLong(
            key: String,
            defaultValue: Long = 0L
        ) = try {
            getConfigJsonObject()?.getLong(key) ?: defaultValue
        } catch (e: Exception) {
            defaultValue
        }


    }
}