package com.roiquery.cloudconfig

import com.roiquery.cloudconfig.core.RemoteConfigContext
import com.roiquery.cloudconfig.core.ResourceLocalRepository
import com.roiquery.cloudconfig.core.ResourceRemoteRepository
import com.roiquery.cloudconfig.locals.StorageResourceLocalRepository
import com.roiquery.cloudconfig.remote.HttpGETResourceRemoteRepository
import com.roiquery.cloudconfig.remote.HttpPOSTResourceRemoteRepository


fun initRemoteConfig(init: RemoteConfig.() -> Unit) {
    RemoteConfig.initialize(init)
}

inline fun <reified T: Any> RemoteConfig.remoteResource(
        localRepository: ResourceLocalRepository,
        remoteRepository: ResourceRemoteRepository) {
    initRemoteResource(T::class.java) {
        resourceLocalRepository = localRepository
        resourceRemoteRepository = remoteRepository
    }
}

inline fun <reified T: Any> RemoteConfig.remoteResource(
        localRepository: ResourceLocalRepository,
        remoteRepository: ResourceRemoteRepository,
        noinline init: RemoteResource<T>.() -> Unit) {
    initRemoteResource(T::class.java) {
        resourceLocalRepository = localRepository
        resourceRemoteRepository = remoteRepository
        init(this)
    }
}

fun RemoteConfigContext.network(url: String): ResourceRemoteRepository =
    HttpGETResourceRemoteRepository.create(url)

fun RemoteConfigContext.storage(dir: String): ResourceLocalRepository =
    StorageResourceLocalRepository(dir)

inline fun <reified T: Any> remoteConfig(nameOfConfig: String) = RemoteConfig.of<T>(nameOfConfig)
inline fun <reified T: Any> remoteConfig() = RemoteConfig.of<T>(T::class.java)