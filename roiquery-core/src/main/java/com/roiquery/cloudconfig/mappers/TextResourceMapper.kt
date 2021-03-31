package com.roiquery.cloudconfig.mappers

import com.roiquery.cloudconfig.core.ResourceMapper
import com.roiquery.cloudconfig.utils.AESCoder
import java.io.ByteArrayInputStream
import java.io.InputStream

object TextResourceMapper : ResourceMapper {
    override fun <T> toRepository(config: T, key: ByteArray): InputStream = ByteArrayInputStream(AESCoder.encrypt((config as String).toByteArray(),key))
//        (config as String).byteInputStream()

    @Suppress("Unchecked_Cast")
    override fun <T> fromRepository(config: InputStream, c: Class<T>, key: ByteArray): T =
        AESCoder.decrypt(config.readBytes(),key).decodeToString() as T
}
