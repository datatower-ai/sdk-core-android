package ai.datatower.analytics.utils

import ai.datatower.quality.DTErrorParams
import ai.datatower.quality.DTQualityHelper
import java.security.MessageDigest

/**
 * @ClassName DataEncryption
 * @Description TODO
 * @Author violet202
 * @Date 2022/10/20 14:32
 * @Version 1.0
 */
class DataEncryption private constructor() {
    companion object{
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            DataEncryption()
        }
    }

    fun  str2Sha1Str(data: String): String {
        return try{
            val digest = MessageDigest.getInstance("SHA-1")
            val result = digest.digest(data.toByteArray())
            toHex(result)
        }catch (e:Exception){
            DTQualityHelper.instance.reportQualityMessage(
                DTErrorParams.CODE_SHA1_DTID_EXCEPTION,
                e.message,
                DTErrorParams.INIT_EXCEPTION
            )
            ""
        }
    }

    private fun toHex(byteArray: ByteArray): String {
        val result = with(StringBuilder()) {
            byteArray.forEach {
                val hex = it.toInt() and (0xFF)
                val hexStr = Integer.toHexString(hex)
                if (hexStr.length == 1) {
                    this.append("0").append(hexStr)
                } else {
                    this.append(hexStr)
                }
            }
            this.toString()
        }
        //转成16进制后是32字节
        return result
    }
}