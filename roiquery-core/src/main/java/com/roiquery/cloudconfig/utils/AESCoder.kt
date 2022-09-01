package com.roiquery.cloudconfig.utils

import android.util.Base64
import com.roiquery.analytics.ROIQueryCoroutineScope
import com.roiquery.cloudconfig.utils.AESCoder.initKey
import kotlinx.coroutines.*
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * AES对称加密算法
 */
internal object AESCoder : ROIQueryCoroutineScope() {
    private const val KEY_ALGORITHM = "AES"
    private const val CIPHER_ALGORITHM = "AES/GCM/NoPadding"
    private var ivBytes: ByteArray? = null

    //hardcoded or read me from a file
    private val pass = "coludconfigpassword".toCharArray()

    // for more confusion
    private val salt = ByteArray(20)

    // vs brute force
    private const val PASSWORD_ITERATIONS = 65536
    private const val KEY_LENGTH = 256

    /**
     * 解密数据
     *
     * @param data 待解密数据
     * @param key  密钥
     * @return byte[] 解密后的数据
     */
    @Throws(Exception::class)
    fun decrypt(data: ByteArray?, key: ByteArray): ByteArray {
        // 欢迎密钥
        val cipher = createCipher(false, key)
        // 执行操作
        return cipher.doFinal(data)
    }

    /**
     * 加密数据
     *
     * @param data 待加密数据
     * @param key  密钥
     * @return byte[] 加密后的数据
     */
    @Throws(Exception::class)
    fun encrypt(data: ByteArray?, key: ByteArray): ByteArray {
        val cipher = createCipher(true, key)
        // 执行操作
        return cipher.doFinal(data)
    }

    @Throws(Exception::class)
    private fun createCipher(encryptMode: Boolean, secret: ByteArray): Cipher {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val mode = if (encryptMode) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE
        val key = toKey(secret)
        if (ivBytes == null) {
            cipher.init(mode, key)
            val params = cipher.parameters
            ivBytes = params?.getParameterSpec(IvParameterSpec::class.java)?.iv
        } else {
            cipher.init(mode, key, IvParameterSpec(ivBytes))
        }
        return cipher
    }

    /**
     * 生成密钥，java6只支持56位密钥，bouncycastle支持64位密钥
     *
     * @return byte[] 二进制密钥
     */
    @Throws(Exception::class)
    suspend fun initKey() = suspendCoroutine<ByteArray> {
        scope.launch(Dispatchers.IO) {
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val spec = PBEKeySpec(pass, salt, PASSWORD_ITERATIONS, KEY_LENGTH)
            val secretKey = factory.generateSecret(spec)
            val secret = SecretKeySpec(secretKey.encoded, KEY_ALGORITHM)
            it.resume(secret.encoded)
        }
    }



    /**
     * 转换密钥
     *
     * @param key 二进制密钥
     * @return Key 密钥
     */
    private fun toKey(key: ByteArray?): Key {
        // 实例化DES密钥
        // 生成密钥
        return SecretKeySpec(key, KEY_ALGORITHM)
    }

    /**
     * @throws Exception
     */
    @Throws(Exception::class)
    fun main() {
        // 初始化密钥
//        byte[] key = Base64.decode(UUID.randomUUID().toString(),Base64.NO_WRAP);
        GlobalScope.launch(Dispatchers.Default) {
            val key = initKey()
            println("密钥：" + Base64.encodeToString(key, Base64.NO_WRAP))
            for (i in 0..99) {
                val str = "我是一个快乐的人-$i"
                println("原文：$str")
                // 加密数据
                var data = encrypt(str.toByteArray(), key)
                println("加密后：" + Base64.encodeToString(data, Base64.NO_WRAP))
                // 解密数据
                data = decrypt(data, key)
                println("解密后：" + String(data))
                println("========================")
            }
        }

    }
}