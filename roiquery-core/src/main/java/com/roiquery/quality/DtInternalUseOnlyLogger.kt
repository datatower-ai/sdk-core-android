package com.roiquery.quality

import org.slf4j.Logger
import org.slf4j.Marker
import org.slf4j.MarkerFactory
import org.slf4j.event.Level
import org.slf4j.helpers.AbstractLogger

class DtInternalUseOnlyLogger(private val delegate: Logger) : AbstractLogger() {
    override fun isTraceEnabled(): Boolean = delegate.isTraceEnabled

    override fun isTraceEnabled(marker: Marker?): Boolean =
        isInternalLoggingEnabled(Level.TRACE, marker) ?: delegate.isTraceEnabled(marker)

    override fun isDebugEnabled(): Boolean = delegate.isDebugEnabled

    override fun isDebugEnabled(marker: Marker?): Boolean =
        isInternalLoggingEnabled(Level.DEBUG, marker) ?: delegate.isDebugEnabled(marker)

    override fun isInfoEnabled(): Boolean = delegate.isInfoEnabled

    override fun isInfoEnabled(marker: Marker?): Boolean =
        isInternalLoggingEnabled(Level.INFO, marker) ?: delegate.isInfoEnabled(marker)

    override fun isWarnEnabled(): Boolean = delegate.isWarnEnabled

    override fun isWarnEnabled(marker: Marker?): Boolean =
        isInternalLoggingEnabled(Level.WARN, marker) ?: delegate.isWarnEnabled(marker)

    override fun isErrorEnabled(): Boolean = delegate.isErrorEnabled

    override fun isErrorEnabled(marker: Marker?): Boolean =
        isInternalLoggingEnabled(Level.ERROR, marker) ?: delegate.isErrorEnabled(marker)

    override fun getFullyQualifiedCallerName(): String? = null

    override fun handleNormalizedLoggingCall(
        level: Level?,
        marker: Marker?,
        messagePattern: String?,
        arguments: Array<out Any>?,
        throwable: Throwable?
    ) = delegate.atLevel(level)
        .addMarker(marker)
        .setCause(throwable)
        .log(messagePattern, *(arguments ?: arrayOf()))

    private fun isInternalLoggingEnabled(level: Level, marker: Marker?): Boolean? {
        if (!delegate.isEnabledForLevel(level)) return false
        return marker?.contains(INTERNAL_USE_ONLY_MARKER)
    }

    companion object {
        val INTERNAL_USE_ONLY_MARKER = MarkerFactory.getMarker("DT_INTERNAL_USE_ONLY_MARKER")
    }
}
