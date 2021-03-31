package com.roiquery.cloudconfig.core

import java.io.InputStream

/**
 * Map the remote configuration class to repository class
 */
interface ResourceMapper {
    fun <T> toRepository(config: T, key: ByteArray): InputStream

    fun <T> fromRepository(config: InputStream, c: Class<T>, key: ByteArray): T
}
