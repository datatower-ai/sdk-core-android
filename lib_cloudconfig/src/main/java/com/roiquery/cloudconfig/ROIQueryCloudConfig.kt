package com.roiquery.cloudconfig

import com.roiquery.analytics.api.ROIQueryAnalytics
import com.unity3d.player.UnityPlayer
import org.json.JSONObject

class ROIQueryCloudConfig {

    companion object {
        private val remoteAppConfig by lazy { remoteConfig<String>() }
        private var isInitialized = false

        @JvmOverloads
        fun fetch(
            success: (() -> Unit)? = null,
            error: ((Throwable) -> Unit)? = null
        ) {
            assertInit()
            remoteAppConfig.fetch(success, error)
        }


        fun fetchForUnity() {
            assertInit()
            remoteAppConfig.fetch(success = {
                UnityPlayer.UnitySendMessage("name", "", "")
            })
        }

        private fun assertInit() {
            if (!isInitialized) {
                initRemoteConfig {
                    remoteResource<String>(
                        storage(ROIQueryAnalytics.getContext().filesDir.absolutePath + "/configs"),
                        network("https://demo7865768.mockable.io/messages.json")
                    ) {
                        resourceName = "cloud_config"
                    }
                }
                remoteAppConfig.setDefaultConfig("")
                isInitialized = true
            }
        }

        private fun getConfigJsonObject(): JSONObject? {
            assertInit()
            return remoteAppConfig.get()?.let {
                JSONObject(it)
            }
        }

        fun getConfigString(): String? {
            assertInit()
            return remoteAppConfig.get()
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