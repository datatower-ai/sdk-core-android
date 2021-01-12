package com.nodetower.analytics.utils

/**
 * EventName, Properties Key/Value 格式错误
 */
class InvalidDataException : Exception {
    constructor(error: String?) : super(error)
    constructor(throwable: Throwable?) : super(throwable)
}
