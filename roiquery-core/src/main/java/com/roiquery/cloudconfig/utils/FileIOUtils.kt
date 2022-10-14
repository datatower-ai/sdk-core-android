package com.roiquery.cloudconfig.utils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.*
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset

/**
 * <pre>
 * author: Blankj
 * blog  : http://blankj.com
 * time  : 2017/06/22
 * desc  : utils about file io
</pre> *
 */
open class FileIOUtils private constructor() {
    interface OnProgressUpdateListener {
        fun onProgressUpdate(progress: Double)
    }

    companion object  {
        private var sBufferSize = 524288
        ///////////////////////////////////////////////////////////////////////////
        // writeFileFromIS without progress
        ///////////////////////////////////////////////////////////////////////////
        /**
         * Write file from input stream.
         *
         * @param filePath The path of file.
         * @param `is`       The input stream.
         * @return `true`: success<br></br>`false`: fail
         */
        @JvmStatic
        @JvmOverloads
        fun writeFileFromIS(filePath: String?, inputStream: InputStream?): Boolean {
            return writeFileFromIS(FileUtils.getFileByPath(filePath), inputStream, false, null)
        }



        /**
         * Write file from input stream.
         *
         * @param file     The file.
         * @param is       The input stream.
         * @param append   True to append, false otherwise.
         * @param listener The progress update listener.
         * @return `true`: success<br></br>`false`: fail
         */
        /**
         * Write file from input stream.
         *
         * @param file The file.
         * @param is   The input stream.
         * @return `true`: success<br></br>`false`: fail
         */
        /**
         * Write file from input stream.
         *
         * @param file   The file.
         * @param `is`     The input stream.
         * @param append True to append, false otherwise.
         * @return `true`: success<br></br>`false`: fail
         */
        @JvmOverloads
         fun writeFileFromIS(
            file: File,
            inputStream: InputStream?,
            append: Boolean = false,
            listener: OnProgressUpdateListener? = null
        ): Boolean {
            if (inputStream == null || !FileUtils.createOrExistsFile(file)) {
                Log.e("FileIOUtils", "create file <$file> failed.")
                return false
            }
            var os: BufferedOutputStream? = null
            return try {
                runBlocking {
                    withContext(Dispatchers.IO){
                        os = BufferedOutputStream(FileOutputStream(file, append), sBufferSize)
                        if (listener == null) {
                            val data = ByteArray(sBufferSize)
                            var len: Int
                            while (inputStream.read(data).also { len = it } != -1) {
                                os?.write(data, 0, len)
                            }
                        } else {
                            val totalSize = inputStream.available().toDouble()
                            var curSize = 0
                            listener.onProgressUpdate(0.0)
                            val data = ByteArray(sBufferSize)
                            var len: Int
                            while (inputStream.read(data).also { len = it } != -1) {
                                os?.write(data, 0, len)
                                curSize += len
                                listener.onProgressUpdate(curSize / totalSize)
                            }
                        }
                        true
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
                false
            } finally {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                try {
                    os?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        ///////////////////////////////////////////////////////////////////////////
        // writeFileFromBytesByStream without progress
        ///////////////////////////////////////////////////////////////////////////
        /**
         * Write file from bytes by stream.
         *
         * @param filePath The path of file.
         * @param bytes    The bytes.
         * @return `true`: success<br></br>`false`: fail
         */
        fun writeFileFromBytesByStream(filePath: String?, bytes: ByteArray?): Boolean {
            return writeFileFromBytesByStream(FileUtils.getFileByPath(filePath), bytes, false, null)
        }

        /**
         * Write file from bytes by stream.
         *
         * @param file     The file.
         * @param bytes    The bytes.
         * @param append   True to append, false otherwise.
         * @param listener The progress update listener.
         * @return `true`: success<br></br>`false`: fail
         */
        /**
         * Write file from bytes by stream.
         *
         * @param file  The file.
         * @param bytes The bytes.
         * @return `true`: success<br></br>`false`: fail
         */
        /**
         * Write file from bytes by stream.
         *
         * @param file   The file.
         * @param bytes  The bytes.
         * @param append True to append, false otherwise.
         * @return `true`: success<br></br>`false`: fail
         */
        @JvmOverloads
        fun writeFileFromBytesByStream(
            file: File,
            bytes: ByteArray?,
            append: Boolean = false,
            listener: OnProgressUpdateListener? = null
        ): Boolean {
            return if (bytes == null) false else writeFileFromIS(
                file,
                ByteArrayInputStream(bytes),
                append,
                listener
            )
        }
    }

    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }
}