package com.roiquery.cloudconfig.core

/**
 * 定义远程资源拉取
 */
interface ResourceRemoteRepository {
    fun fetch(
        success: (String) -> Unit,
        fail: (Exception) -> Unit
    )
}
