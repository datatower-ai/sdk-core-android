package com.roiquery.cloudconfig

import android.content.Context
import com.roiquery.cloudconfig.core.ResourceLocalRepository
import com.roiquery.cloudconfig.core.ResourceRemoteRepository
import com.roiquery.cloudconfig.locals.StorageResourceLocalRepository


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

internal fun storage(context: Context, dir: String): ResourceLocalRepository =
    StorageResourceLocalRepository(context,dir)

internal inline fun <reified T: Any> remoteConfig(nameOfConfig: String) = RemoteConfig.of<T>(nameOfConfig)
internal inline fun <reified T: Any> remoteConfig() = RemoteConfig.of<T>(T::class.java)