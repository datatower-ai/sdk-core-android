package com.roiquery.thirdparty

import com.roiquery.analytics.utils.LogUtils

class AdjustSyncThirdData: SyncThirdPartDataImpl{
    override fun synThirdDTIdData(id: String) {
        try {
            val mAdjustClazz = Class.forName("com.adjust.sdk.Adjust")
            val addSessionParameterMethod = mAdjustClazz
                .getMethod("addSessionCallbackParameter", String::class.java, String::class.java)
            addSessionParameterMethod
                .invoke(
                    null,
                    Constant.SHARE_DATA_DT_ID,
                    id
                )
            LogUtils.i(com.roiquery.analytics.Constant.LOG_TAG,"Adjust share data succeed")
        } catch (e: Exception) {
            LogUtils.i(com.roiquery.analytics.Constant.LOG_TAG,"Adjust share data error: ${e.message}")
        }
    }

}