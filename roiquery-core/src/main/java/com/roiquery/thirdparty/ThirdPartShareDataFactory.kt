package com.roiquery.thirdparty

class ThirdPartShareDataFactory {
    companion object{
        fun createThirdInstance(type:ThirdSDKShareType):SyncThirdPartDataImpl{
           return when(type){
                ThirdSDKShareType.ADJUST-> AdjustSyncThirdData()
                ThirdSDKShareType.APPSFly->AppsFlyerSyncThirdData()
            }
        }
    }
}