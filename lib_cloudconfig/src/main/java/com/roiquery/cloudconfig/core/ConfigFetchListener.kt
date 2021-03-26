package com.roiquery.cloudconfig.core

interface ConfigFetchListener {

    fun onSuccess()
    fun onError(errorMessage: String)
}
