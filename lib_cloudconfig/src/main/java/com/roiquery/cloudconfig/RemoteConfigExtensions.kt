package com.roiquery.cloudconfig

import com.roiquery.cloudconfig.core.RemoteConfigContext
import com.roiquery.cloudconfig.core.ResourceLocalRepository
import com.roiquery.cloudconfig.core.ResourceRemoteRepository
import com.roiquery.cloudconfig.locals.StorageResourceLocalRepository
import com.roiquery.cloudconfig.remote.HttpGETResourceRemoteRepository
import com.roiquery.cloudconfig.remote.HttpPOSTResourceRemoteRepository


internal fun initRemoteConfig(init: RemoteConfig.() -> Unit) {
    RemoteConfig.initialize(init)
}

internal inline fun <reified T: Any> RemoteConfig.remoteResource(
        localRepository: ResourceLocalRepository,
        remoteRepository: ResourceRemoteRepository) {
    initRemoteResource(T::class.java) {
        resourceLocalRepository = localRepository
        resourceRemoteRepository = remoteRepository
    }
}

internal inline fun <reified T: Any> RemoteConfig.remoteResource(
        localRepository: ResourceLocalRepository,
        remoteRepository: ResourceRemoteRepository,
        noinline init: RemoteResource<T>.() -> Unit) {
    initRemoteResource(T::class.java) {
        resourceLocalRepository = localRepository
        resourceRemoteRepository = remoteRepository
        init(this)
    }
}

internal fun network(url: String): ResourceRemoteRepository =
    HttpGETResourceRemoteRepository.create(url)

internal fun storage(dir: String): ResourceLocalRepository =
    StorageResourceLocalRepository(dir)

internal inline fun <reified T: Any> remoteConfig(nameOfConfig: String) = RemoteConfig.of<T>(nameOfConfig)
internal inline fun <reified T: Any> remoteConfig() = RemoteConfig.of<T>(T::class.java)