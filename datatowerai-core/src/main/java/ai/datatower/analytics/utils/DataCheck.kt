package ai.datatower.analytics.utils

import org.json.JSONObject

object DataCheck {
    fun JSONObject.findFirstNonJsonArray(): String? {
        keys().forEach {
            optJSONArray(it) ?: return it
        }
        return null
    }
}