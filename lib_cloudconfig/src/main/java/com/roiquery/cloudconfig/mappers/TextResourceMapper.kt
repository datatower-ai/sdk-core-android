package com.roiquery.cloudconfig.mappers

import com.roiquery.cloudconfig.core.ResourceMapper
import java.io.InputStream

object TextResourceMapper: ResourceMapper {
    override fun <T> toRepository(config: T): InputStream = (config as String).byteInputStream()

    @Suppress("Unchecked_Cast")
    override fun <T> fromRepository(config: InputStream, c: Class<T>): T = config.readBytes().decodeToString() as T
}
