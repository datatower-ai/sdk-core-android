package com.roiquery.cloudconfig

import com.unity3d.player.UnityPlayer

class ROIQueryCloudConfig {

    companion object {
        private val remoteAppConfig by lazy { remoteConfig<String>() }


        fun fetch() {

        }
//
//        fun fetch(name: String) {
//            remoteAppConfig.fetch(success = {
//                UnityPlayer.UnitySendMessage(name,)
//            })
//        }
//
//        fun getInt(
//            key: String,
//            defaultValue: String = ""
//        ): Int {
//
//        }

    }
}