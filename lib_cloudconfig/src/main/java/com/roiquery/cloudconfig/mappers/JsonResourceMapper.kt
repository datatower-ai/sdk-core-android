package com.roiquery.cloudconfig.mappers

import com.roiquery.cloudconfig.core.ResourceMapper
import java.io.InputStream

//object JsonResourceMapper: ResourceMapper {
////    private val gsonInstance = Gson()
//    override fun <T> toRepository(config: T): InputStream = gsonInstance.toJson(config).byteInputStream()
//
//    override fun <T> fromRepository(config: InputStream, c: Class<T>): T {
//        return gsonInstance.fromJson(config.readBytes().decodeToString(), c)
//    }
//}
