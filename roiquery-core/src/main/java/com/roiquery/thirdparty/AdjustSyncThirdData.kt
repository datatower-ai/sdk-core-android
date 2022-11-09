package com.roiquery.thirdparty

import com.roiquery.analytics.utils.LogUtils

class AdjustSyncThirdData: SyncThirdPartDataImpl{
    override fun synThirdDTIdData(id: String) {
        LogUtils.d("开始同步Adjust数据")
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
            LogUtils.i("Adjust数据同步成功")
        } catch (e: Exception) {
            LogUtils.i("Adjust数据同步异常:" + e.message)
        }
    }

}