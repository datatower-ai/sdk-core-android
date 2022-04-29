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
         * @param filePath The path of file.
         * @param is       The input stream.
         * @param append   True to append, false otherwise.
         * @return `true`: success<br></br>`false`: fail
         */
        fun writeFileFromIS(
            filePath: String?,
            `is`: InputStream?,
            append: Boolean
        ): Boolean {
            return writeFileFromIS(FileUtils.getFileByPath(filePath), `is`, append, null)
        }
        ///////////////////////////////////////////////////////////////////////////
        // writeFileFromIS with progress
        ///////////////////////////////////////////////////////////////////////////
        /**
         * Write file from input stream.
         *
         * @param filePath The path of file.
         * @param is       The input stream.
         * @param listener The progress update listener.
         * @return `true`: success<br></br>`false`: fail
         */
        fun writeFileFromIS(
            filePath: String?,
            `is`: InputStream?,
            listener: OnProgressUpdateListener?
        ): Boolean {
            return writeFileFromIS(FileUtils.getFileByPath(filePath), `is`, false, listener)
        }

        /**
         * Write file from input stream.
         *
         * @param filePath The path of file.
         * @param is       The input stream.
         * @param append   True to append, false otherwise.
         * @param listener The progress update listener.
         * @return `true`: success<br></br>`false`: fail
         */
        fun writeFileFromIS(
            filePath: String?,
            `is`: InputStream?,
            append: Boolean,
            listener: OnProgressUpdateListener?
        ): Boolean {
            return writeFileFromIS(FileUtils.getFileByPath(filePath), `is`, append, listener)
        }

        /**
         * Write file from input stream.
         *
         * @param file     The file.
         * @param is       The input stream.
         * @param listener The progress update listener.
         * @return `true`: success<br></br>`false`: fail
         */
        fun writeFileFromIS(
            file: File,
            `is`: InputStream?,
            listener: OnProgressUpdateListener?
        ): Boolean {
            return writeFileFromIS(file, `is`, false, listener)
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
         * @param filePath The path of file.
         * @param bytes    The bytes.
         * @param append   True to append, false otherwise.
         * @return `true`: success<br></br>`false`: fail
         */
        fun writeFileFromBytesByStream(
            filePath: String?,
            bytes: ByteArray?,
            append: Boolean
        ): Boolean {
            return writeFileFromBytesByStream(
                FileUtils.getFileByPath(filePath),
                bytes,
                append,
                null
            )
        }
        ///////////////////////////////////////////////////////////////////////////
        // writeFileFromBytesByStream with progress
        ///////////////////////////////////////////////////////////////////////////
        /**
         * Write file from bytes by stream.
         *
         * @param filePath The path of file.
         * @param bytes    The bytes.
         * @param listener The progress update listener.
         * @return `true`: success<br></br>`false`: fail
         */
        fun writeFileFromBytesByStream(
            filePath: String?,
            bytes: ByteArray?,
            listener: OnProgressUpdateListener?
        ): Boolean {
            return writeFileFromBytesByStream(
                FileUtils.getFileByPath(filePath),
                bytes,
                false,
                listener
            )
        }

        /**
         * Write file from bytes by stream.
         *
         * @param filePath The path of file.
         * @param bytes    The bytes.
         * @param append   True to append, false otherwise.
         * @param listener The progress update listener.
         * @return `true`: success<br></br>`false`: fail
         */
        fun writeFileFromBytesByStream(
            filePath: String?,
            bytes: ByteArray?,
            append: Boolean,
            listener: OnProgressUpdateListener?
        ): Boolean {
            return writeFileFromBytesByStream(
                FileUtils.getFileByPath(filePath),
                bytes,
                append,
                listener
            )
        }

        /**
         * Write file from bytes by stream.
         *
         * @param file     The file.
         * @param bytes    The bytes.
         * @param listener The progress update listener.
         * @return `true`: success<br></br>`false`: fail
         */
        fun writeFileFromBytesByStream(
            file: File,
            bytes: ByteArray?,
            listener: OnProgressUpdateListener?
        ): Boolean {
            return writeFileFromBytesByStream(file, bytes, false, listener)
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

        /**
         * Write file from bytes by channel.
         *
         * @param filePath The path of file.
         * @param bytes    The bytes.
         * @param isForce  是否写入文件
         * @return `true`: success<br></br>`false`: fail
         */
        fun writeFileFromBytesByChannel(
            filePath: String?,
            bytes: ByteArray?,
            isForce: Boolean
        ): Boolean {
            return writeFileFromBytesByChannel(
                FileUtils.getFileByPath(filePath),
                bytes,
                false,
                isForce
            )
        }

        /**
         * Write file from bytes by channel.
         *
         * @param filePath The path of file.
         * @param bytes    The bytes.
         * @param append   True to append, false otherwise.
         * @param isForce  True to force write file, false otherwise.
         * @return `true`: success<br></br>`false`: fail
         */
        fun writeFileFromBytesByChannel(
            filePath: String?,
            bytes: ByteArray?,
            append: Boolean,
            isForce: Boolean
        ): Boolean {
            return writeFileFromBytesByChannel(
                FileUtils.getFileByPath(filePath),
                bytes,
                append,
                isForce
            )
        }

        /**
         * Write file from bytes by channel.
         *
         * @param file    The file.
         * @param bytes   The bytes.
         * @param isForce True to force write file, false otherwise.
         * @return `true`: success<br></br>`false`: fail
         */
        fun writeFileFromBytesByChannel(
            file: File,
            bytes: ByteArray?,
            isForce: Boolean
        ): Boolean {
            return writeFileFromBytesByChannel(file, bytes, false, isForce)
        }

        /**
         * Write file from bytes by channel.
         *
         * @param file    The file.
         * @param bytes   The bytes.
         * @param append  True to append, false otherwise.
         * @param isForce True to force write file, false otherwise.
         * @return `true`: success<br></br>`false`: fail
         */
        fun writeFileFromBytesByChannel(
            file: File,
            bytes: ByteArray?,
            append: Boolean,
            isForce: Boolean
        ): Boolean {
            if (bytes == null) {
                Log.e("FileIOUtils", "bytes is null.")
                return false
            }
            if (!FileUtils.createOrExistsFile(file)) {
                Log.e("FileIOUtils", "create file <$file> failed.")
                return false
            }
            var fc: FileChannel? = null
            return try {
                fc = FileOutputStream(file, append).channel
                if (fc == null) {
                    Log.e("FileIOUtils", "fc is null.")
                    return false
                }
                fc.position(fc.size())
                fc.write(ByteBuffer.wrap(bytes))
                if (isForce) fc.force(true)
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            } finally {
                try {
                    fc?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * Write file from bytes by map.
         *
         * @param filePath The path of file.
         * @param bytes    The bytes.
         * @param isForce  True to force write file, false otherwise.
         * @return `true`: success<br></br>`false`: fail
         */
        fun writeFileFromBytesByMap(
            filePath: String?,
            bytes: ByteArray?,
            isForce: Boolean
        ): Boolean {
            return writeFileFromBytesByMap(filePath, bytes, false, isForce)
        }

        /**
         * Write file from bytes by map.
         *
         * @param filePath The path of file.
         * @param bytes    The bytes.
         * @param append   True to append, false otherwise.
         * @param isForce  True to force write file, false otherwise.
         * @return `true`: success<br></br>`false`: fail
         */
        fun writeFileFromBytesByMap(
            filePath: String?,
            bytes: ByteArray?,
            append: Boolean,
            isForce: Boolean
        ): Boolean {
            return writeFileFromBytesByMap(
                FileUtils.getFileByPath(filePath),
                bytes,
                append,
                isForce
            )
        }

        /**
         * Write file from bytes by map.
         *
         * @param file    The file.
         * @param bytes   The bytes.
         * @param isForce True to force write file, false otherwise.
         * @return `true`: success<br></br>`false`: fail
         */
        fun writeFileFromBytesByMap(
            file: File,
            bytes: ByteArray?,
            isForce: Boolean
        ): Boolean {
            return writeFileFromBytesByMap(file, bytes, false, isForce)
        }

        /**
         * Write file from bytes by map.
         *
         * @param file    The file.
         * @param bytes   The bytes.
         * @param append  True to append, false otherwise.
         * @param isForce True to force write file, false otherwise.
         * @return `true`: success<br></br>`false`: fail
         */
        fun writeFileFromBytesByMap(
            file: File,
            bytes: ByteArray?,
            append: Boolean,
            isForce: Boolean
        ): Boolean {
            if (bytes == null || !FileUtils.createOrExistsFile(file)) {
                Log.e("FileIOUtils", "create file <$file> failed.")
                return false
            }
            var fc: FileChannel? = null
            return try {
                fc = FileOutputStream(file, append).channel
                if (fc == null) {
                    Log.e("FileIOUtils", "fc is null.")
                    return false
                }
                val mbb = fc.map(FileChannel.MapMode.READ_WRITE, fc.size(), bytes.size.toLong())
                mbb.put(bytes)
                if (isForce) mbb.force()
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            } finally {
                try {
                    fc?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * Write file from string.
         *
         * @param filePath The path of file.
         * @param content  The string of content.
         * @return `true`: success<br></br>`false`: fail
         */
        fun writeFileFromString(filePath: String?, content: String?): Boolean {
            return writeFileFromString(FileUtils.getFileByPath(filePath), content, false)
        }

        /**
         * Write file from string.
         *
         * @param filePath The path of file.
         * @param content  The string of content.
         * @param append   True to append, false otherwise.
         * @return `true`: success<br></br>`false`: fail
         */
        fun writeFileFromString(
            filePath: String?,
            content: String?,
            append: Boolean
        ): Boolean {
            return writeFileFromString(FileUtils.getFileByPath(filePath), content, append)
        }
        /**
         * Write file from string.
         *
         * @param file    The file.
         * @param content The string of content.
         * @param append  True to append, false otherwise.
         * @return `true`: success<br></br>`false`: fail
         */
        /**
         * Write file from string.
         *
         * @param file    The file.
         * @param content The string of content.
         * @return `true`: success<br></br>`false`: fail
         */
        @JvmOverloads
        fun writeFileFromString(
            file: File?,
            content: String?,
            append: Boolean = false
        ): Boolean {
            if (file == null || content == null) return false
            if (!FileUtils.createOrExistsFile(file)) {
                Log.e("FileIOUtils", "create file <$file> failed.")
                return false
            }
            var bw: BufferedWriter? = null
            return try {
                bw = BufferedWriter(FileWriter(file, append))
                bw.write(content)
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            } finally {
                try {
                    bw?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        ///////////////////////////////////////////////////////////////////////////
        // the divide line of write and read
        ///////////////////////////////////////////////////////////////////////////
        /**
         * Return the lines in file.
         *
         * @param filePath The path of file.
         * @return the lines in file
         */
        fun readFile2List(filePath: String?): List<String>? {
            return readFile2List(FileUtils.getFileByPath(filePath), null)
        }

        /**
         * Return the lines in file.
         *
         * @param filePath    The path of file.
         * @param charsetName The name of charset.
         * @return the lines in file
         */
        fun readFile2List(filePath: String?, charsetName: String?): List<String>? {
            return readFile2List(FileUtils.getFileByPath(filePath), charsetName)
        }

        /**
         * Return the lines in file.
         *
         * @param file        The file.
         * @param charsetName The name of charset.
         * @return the lines in file
         */
        fun readFile2List(file: File?, charsetName: String?): List<String>? {
            return readFile2List(file, 0, 0x7FFFFFFF, charsetName)
        }

        /**
         * Return the lines in file.
         *
         * @param filePath The path of file.
         * @param st       The line's index of start.
         * @param end      The line's index of end.
         * @return the lines in file
         */
        fun readFile2List(filePath: String?, st: Int, end: Int): List<String>? {
            return readFile2List(FileUtils.getFileByPath(filePath), st, end, null)
        }

        /**
         * Return the lines in file.
         *
         * @param filePath    The path of file.
         * @param st          The line's index of start.
         * @param end         The line's index of end.
         * @param charsetName The name of charset.
         * @return the lines in file
         */
        fun readFile2List(
            filePath: String?,
            st: Int,
            end: Int,
            charsetName: String?
        ): List<String>? {
            return readFile2List(FileUtils.getFileByPath(filePath), st, end, charsetName)
        }
        /**
         * Return the lines in file.
         *
         * @param file        The file.
         * @param st          The line's index of start.
         * @param end         The line's index of end.
         * @param charsetName The name of charset.
         * @return the lines in file
         */
        /**
         * Return the lines in file.
         *
         * @param file The file.
         * @return the lines in file
         */
        /**
         * Return the lines in file.
         *
         * @param file The file.
         * @param st   The line's index of start.
         * @param end  The line's index of end.
         * @return the lines in file
         */
        @JvmOverloads
        fun readFile2List(
            file: File?,
            st: Int = 0,
            end: Int = 0x7FFFFFFF,
            charsetName: String? = null
        ): List<String>? {
            if (!FileUtils.isFileExists(file)) return null
            if (st > end) return null
            var reader: BufferedReader? = null
            return try {
                var line: String
                var curLine = 1
                val list: MutableList<String> = ArrayList()
                reader = if (StringUtils.isSpace(charsetName)) {
                    BufferedReader(InputStreamReader(FileInputStream(file)))
                } else {
                    BufferedReader(
                        InputStreamReader(FileInputStream(file), charsetName)
                    )
                }
                while (reader.readLine().also { line = it } != null) {
                    if (curLine > end) break
                    if (st <= curLine && curLine <= end) list.add(line)
                    ++curLine
                }
                list
            } catch (e: IOException) {
                e.printStackTrace()
                null
            } finally {
                try {
                    reader?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * Return the string in file.
         *
         * @param filePath The path of file.
         * @return the string in file
         */
        fun readFile2String(filePath: String?): String? {
            return readFile2String(FileUtils.getFileByPath(filePath), null)
        }

        /**
         * Return the string in file.
         *
         * @param filePath    The path of file.
         * @param charsetName The name of charset.
         * @return the string in file
         */
        fun readFile2String(filePath: String?, charsetName: String?): String? {
            return readFile2String(FileUtils.getFileByPath(filePath), charsetName)
        }
        /**
         * Return the string in file.
         *
         * @param file        The file.
         * @param charsetName The name of charset.
         * @return the string in file
         */
        /**
         * Return the string in file.
         *
         * @param file The file.
         * @return the string in file
         */
        @JvmOverloads
        fun readFile2String(file: File?, charsetName: String? = null): String? {
            val bytes = readFile2BytesByStream(file) ?: return null
            return if (StringUtils.isSpace(charsetName)) {
                String(bytes)
            } else {
                try {
                    String(
                        bytes,
                        if (charsetName?.isNotEmpty() == true) Charset.forName(charsetName) else Charset.defaultCharset()
                    )
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                    ""
                }
            }
        }
        ///////////////////////////////////////////////////////////////////////////
        // readFile2BytesByStream without progress
        ///////////////////////////////////////////////////////////////////////////
        /**
         * Return the bytes in file by stream.
         *
         * @param filePath The path of file.
         * @return the bytes in file
         */
        fun readFile2BytesByStream(filePath: String?): ByteArray? {
            return readFile2BytesByStream(FileUtils.getFileByPath(filePath), null)
        }
        ///////////////////////////////////////////////////////////////////////////
        // readFile2BytesByStream with progress
        ///////////////////////////////////////////////////////////////////////////
        /**
         * Return the bytes in file by stream.
         *
         * @param filePath The path of file.
         * @param listener The progress update listener.
         * @return the bytes in file
         */
        fun readFile2BytesByStream(
            filePath: String?,
            listener: OnProgressUpdateListener?
        ): ByteArray? {
            return readFile2BytesByStream(FileUtils.getFileByPath(filePath), listener)
        }
        /**
         * Return the bytes in file by stream.
         *
         * @param file     The file.
         * @param listener The progress update listener.
         * @return the bytes in file
         */
        /**
         * Return the bytes in file by stream.
         *
         * @param file The file.
         * @return the bytes in file
         */
        @JvmOverloads
        fun readFile2BytesByStream(
            file: File?,
            listener: OnProgressUpdateListener? = null
        ): ByteArray? {
            return if (!FileUtils.isFileExists(file)) null else try {
                var os: ByteArrayOutputStream? = null
                val `is`: InputStream = BufferedInputStream(FileInputStream(file), sBufferSize)
                try {
                    os = ByteArrayOutputStream()
                    val b = ByteArray(sBufferSize)
                    var len: Int
                    if (listener == null) {
                        while (`is`.read(b, 0, sBufferSize).also { len = it } != -1) {
                            os.write(b, 0, len)
                        }
                    } else {
                        val totalSize = `is`.available().toDouble()
                        var curSize = 0
                        listener.onProgressUpdate(0.0)
                        while (`is`.read(b, 0, sBufferSize).also { len = it } != -1) {
                            os.write(b, 0, len)
                            curSize += len
                            listener.onProgressUpdate(curSize / totalSize)
                        }
                    }
                    os.toByteArray()
                } catch (e: IOException) {
                    e.printStackTrace()
                    null
                } finally {
                    try {
                        `is`.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    try {
                        os?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                null
            }
        }

        /**
         * Return the bytes in file by channel.
         *
         * @param filePath The path of file.
         * @return the bytes in file
         */
        fun readFile2BytesByChannel(filePath: String?): ByteArray? {
            return readFile2BytesByChannel(FileUtils.getFileByPath(filePath))
        }

        /**
         * Return the bytes in file by channel.
         *
         * @param file The file.
         * @return the bytes in file
         */
        fun readFile2BytesByChannel(file: File?): ByteArray? {
            if (!FileUtils.isFileExists(file)) return null
            var fc: FileChannel? = null
            return try {
                fc = RandomAccessFile(file, "r").channel
                if (fc == null) {
                    Log.e("FileIOUtils", "fc is null.")
                    return ByteArray(0)
                }
                val byteBuffer = ByteBuffer.allocate(
                    fc.size().toInt()
                )
                while (true) {
                    if (fc.read(byteBuffer) <= 0) break
                }
                byteBuffer.array()
            } catch (e: IOException) {
                e.printStackTrace()
                null
            } finally {
                try {
                    fc?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * Return the bytes in file by map.
         *
         * @param filePath The path of file.
         * @return the bytes in file
         */
        fun readFile2BytesByMap(filePath: String?): ByteArray? {
            return readFile2BytesByMap(FileUtils.getFileByPath(filePath))
        }

        /**
         * Return the bytes in file by map.
         *
         * @param file The file.
         * @return the bytes in file
         */
        fun readFile2BytesByMap(file: File?): ByteArray? {
            if (!FileUtils.isFileExists(file)) return null
            var fc: FileChannel? = null
            return try {
                fc = RandomAccessFile(file, "r").channel
                if (fc == null) {
                    Log.e("FileIOUtils", "fc is null.")
                    return ByteArray(0)
                }
                val size = fc.size().toInt()
                val mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, size.toLong()).load()
                val result = ByteArray(size)
                mbb[result, 0, size]
                result
            } catch (e: IOException) {
                e.printStackTrace()
                null
            } finally {
                try {
                    fc?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * Set the buffer's size.
         *
         * Default size equals 8192 bytes.
         *
         * @param bufferSize The buffer's size.
         */
        fun setBufferSize(bufferSize: Int) {
            sBufferSize = bufferSize
        }

    }

    init {
        throw UnsupportedOperationException("u can't instantiate me...")
    }
}