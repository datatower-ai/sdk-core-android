package com.roiquery.analytics.api

interface ServerTimeListener {
    fun onFinished(time: Long, msg: String)
}