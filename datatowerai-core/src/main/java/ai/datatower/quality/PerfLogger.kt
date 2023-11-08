@file:JvmSynthetic
package ai.datatower.quality

import ai.datatower.analytics.utils.LogUtils

internal enum class PerfAction {
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

internal object PerfLogger {

    const val tag = "PerfLog";
    private val timeRecord = HashMap<String, Long>();

    fun doPerfLog(action:PerfAction, time:Long,) {
        if (action.name.endsWith("END")) {
            var relatedKey = action.name.substring(0,  action.name.length - 3)
            relatedKey += "BEGIN"
            if (timeRecord[relatedKey] != null) {
                val timeStart = timeRecord[relatedKey]
                timeStart?.apply {
                    val cost = time - this
                    LogUtils.i(tag, "action ${action.name} cost $cost");
                }
                timeRecord.remove(relatedKey)
            } else {
                LogUtils.e(tag, "Error, no log action $relatedKey");
            }

        } else if (action.name.endsWith("BEGIN")){
            if (timeRecord[action.name] != null) {
                LogUtils.e(tag, "Error, duplicate log action ${action.name}");
//                return
            }
            timeRecord[action.name] = time;
        }

        LogUtils.i(tag, action.name);
    }
}