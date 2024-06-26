package ai.datatower.quality

import ai.datatower.analytics.data.EventDataAdapter
import ai.datatower.analytics.utils.LogUtils
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

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

    fun getDBItemCount(): Int? {
        return 0
    }
}