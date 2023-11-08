package ai.datatower.thirdparty

import ai.datatower.analytics.Constant
import ai.datatower.analytics.DTThirdPartyShareType
import ai.datatower.analytics.utils.LogUtils

class ThirdPartShareDataFactory {
    companion object {
        fun createThirdInstance(type: Int): SyncThirdPartDataImpl?{
           return when(type){
               DTThirdPartyShareType.ADJUST -> AdjustSyncThirdData()
               else -> {
                   LogUtils.d(Constant.LOG_TAG,"please impl type :$type")
                   null
               }
           }
        }
    }
}