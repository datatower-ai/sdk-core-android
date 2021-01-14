package com.nodetower.analytics.utils

/**
 * EventName, Properties Key/Value 格式错误
 */
class InvalidDataException : Exception {
    constructor(error: String?) : super(error)
    constructor(throwable: Throwable?) : super(throwable)
}


/**
 * Debug 模式下的异常，用于指出 Debug 模式下的各种问题，程序不应该捕获该异常，以免屏蔽错误信息
 */
class DebugModeException : RuntimeException {
    constructor(error: String?) : super(error)
    constructor(throwable: Throwable?) : super(throwable)
}


/**
 * 网络连接错误
 */
class ConnectErrorException : java.lang.Exception {
    var retryAfter = 0
        private set

    constructor(message: String?) : super(message) {
        retryAfter = 30 * 1000
    }

    constructor(message: String?, strRetryAfter: String) : super(message) {
        try {
            retryAfter = strRetryAfter.toInt()
        } catch (e: NumberFormatException) {
            retryAfter = 0
        }
    }

    constructor(throwable: Throwable?) : super(throwable) {}
}

/**
 * Sensors Analytics 返回数据收集异常
 */
class ResponseErrorException : java.lang.Exception {
    var httpCode: Int
        private set

    constructor(error: String?, httpCode: Int) : super(error) {
        this.httpCode = httpCode
    }

    constructor(throwable: Throwable?, httpCode: Int) : super(throwable) {
        this.httpCode = httpCode
    }
}