package com.roiquery.cloudconfig.exceptions

import java.io.IOException


class HttpException : IOException {
    val httpCode: Int

    constructor(httpCode: Int) : super("[http code: $httpCode]") {
        this.httpCode = httpCode
    }

    constructor(httpCode: Int, message: String) : super("[http code: $httpCode] $message") {
        this.httpCode = httpCode
    }
}
