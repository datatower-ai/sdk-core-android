package com.roiquery.quality

import com.roiquery.analytics.utils.LogUtils

enum class PerfAction {
    SDKINITBEGIN,
    SDKINITEND,
    GETDTIDBEGIN,
    GETDTIDEND,
    GETSRVTIMEBEGIN,
    GETSRVTIMEEND,
    GETCONFIGBEGIN,
    GETCONFIGEND,
    TRACKBEGIN,
    WRITEEVENTTODBBEGIN,
    WRITEEVENTTODBEND,
    READEVENTDATAFROMDBBEGIN,
    READEVENTDATAFROMDBEND,
    UPLOADDATABEGIN,
    UPLOADDATAEND,
    DELETEDBBEGIN,
    DELETEDBEND,
    TRACKEND,
}

object PerfLogger {

    const val tag = "PerfLog";
    private val timeRecord = HashMap<String, Long>();

    fun doPerfLog(action:PerfAction, time:Long,) {
    }
}