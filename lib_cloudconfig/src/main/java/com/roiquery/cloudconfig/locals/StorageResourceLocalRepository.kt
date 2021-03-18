package com.roiquery.cloudconfig.locals

import android.content.Context
import com.roiquery.analytics.utils.FileIOUtils
import com.roiquery.analytics.utils.FileUtils
import com.roiquery.analytics.utils.StringUtils
import com.roiquery.cloudconfig.core.ResourceLocalRepository
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class StorageResourceLocalRepository(
    rootDir: String
) : ResourceLocalRepository {
    private val root: File = File(rootDir).also {
        if (!it.exists()) {
            it.mkdir()
        }
    }

    private lateinit var resourceName: String

    override fun setResourceName(resourceName: String) {
        this.resourceName = resourceName
    }

    override fun isFetchedFresh(maxAgeInMillis: Long): Boolean {
        val lastFetched = getResourceFile(FETCHED)?.lastModified() ?: 0
        return (System.currentTimeMillis() - lastFetched) <= maxAgeInMillis
    }

    override fun getActive(): InputStream? = getInputStream(ACTIVE)

    override fun storeDefault(defaultValue: InputStream) {
        writeResourceFile(DEFAULT, defaultValue)
        if (!FileUtils.isFileExists(getResourcePath(ACTIVE))){
            writeResourceFile(ACTIVE, getInputStream(DEFAULT))
        }
    }

    override fun storeFetched(fetchedResource: InputStream) {
        writeResourceFile(FETCHED, fetchedResource)
    }

    override fun activate() {
        writeResourceFile(ACTIVE, getInputStream(FETCHED))
    }

    override fun clear() {
        FileUtils.delete(getResourcePath(ACTIVE))
        FileUtils.delete(getResourcePath(FETCHED))
        FileUtils.delete(getResourcePath(DEFAULT))
    }

    private fun getResourcePath(variant: String): String {
        return root.resolve(getFileName(variant)).path
    }

    private fun getResourceFile(variant: String): File? {
        return root.resolve(getFileName(variant)).let {
            if (it.exists()) {
                it
            } else {
                null
            }
        }
    }

    private fun getInputStream(variant: String): InputStream? {
        val path = getResourcePath(variant)
        return if (FileUtils.isFileExists(path)) {
            FileInputStream(path)
        } else {
            null
        }
    }

    private fun writeResourceFile(variant: String, stream: InputStream?) {
        stream?.use {
            FileIOUtils.writeFileFromBytesByStream(getResourcePath(variant),it.readBytes())
        }
    }

    private fun getFileName(variant: String) = "${resourceName}_${variant}"

    private companion object Variants {
        private const val ACTIVE = "active"
        private const val FETCHED = "fetched"
        private const val DEFAULT = "default"
    }
}
