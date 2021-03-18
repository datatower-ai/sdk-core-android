package com.roiquery.cloudconfig.core

import java.io.InputStream

/**
 * 定义本地资源存储
 */
interface ResourceLocalRepository {
    fun setResourceName(resourceName: String)

    fun isFetchedFresh(maxAgeInMillis: Long): Boolean

    fun getActive(): InputStream?

    fun storeDefault(defaultValue: InputStream)

    fun storeFetched(fetchedResource: InputStream)

    fun activate()

    fun clear()
}
