package com.roiquery.quality

import com.roiquery.analytics.BuildConfig
import org.slf4j.Logger
import org.slf4j.Marker
import org.slf4j.event.Level
import org.slf4j.helpers.AbstractLogger

class DtInternalUseOnlyLogger(private val delegate: Logger) : AbstractLogger() {
    override fun isTraceEnabled(): Boolean = delegate.isTraceEnabled

    override fun isTraceEnabled(marker: Marker?): Boolean = delegate.isTraceEnabled(marker)

    override fun isDebugEnabled(): Boolean = delegate.isDebugEnabled

    override fun isDebugEnabled(marker: Marker?): Boolean = delegate.isDebugEnabled(marker)

    override fun isInfoEnabled(): Boolean = delegate.isInfoEnabled

    override fun isInfoEnabled(marker: Marker?): Boolean = delegate.isInfoEnabled(marker)

    override fun isWarnEnabled(): Boolean = delegate.isWarnEnabled

    override fun isWarnEnabled(marker: Marker?): Boolean = delegate.isWarnEnabled(marker)

    override fun isErrorEnabled(): Boolean = delegate.isErrorEnabled

    override fun isErrorEnabled(marker: Marker?): Boolean = delegate.isErrorEnabled(marker)

    override fun getFullyQualifiedCallerName(): String? = null

    override fun handleNormalizedLoggingCall(
        level: Level?,
        marker: Marker?,
        messagePattern: String?,
        arguments: Array<out Any>?,
        throwable: Throwable?
    ) {
        if (!BuildConfig.IS_INTERNAL_BUILD) return
        delegate.atLevel(level)
            .addMarker(marker)
            .setCause(throwable)
            .log(messagePattern, *(arguments ?: arrayOf()))
    }
}
