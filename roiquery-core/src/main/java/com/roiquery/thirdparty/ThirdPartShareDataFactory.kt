package com.roiquery.thirdparty

import com.roiquery.analytics.Constant
import com.roiquery.analytics.DTShareType
import com.roiquery.analytics.utils.LogUtils

class ThirdPartShareDataFactory {
    companion object {
        fun createThirdInstance(type: Int): SyncThirdPartDataImpl?{
           return when(type){
               DTShareType.ADJUST -> AdjustSyncThirdData()
               else -> {
                   LogUtils.d(Constant.LOG_TAG,"please impl type :$type")
                   null
               }
           }
        }
    }
}