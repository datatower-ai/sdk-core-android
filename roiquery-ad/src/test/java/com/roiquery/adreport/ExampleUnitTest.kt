package com.roiquery.adreport

import com.roiquery.ad.utils.AdEventProperty
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val mSequenessMap: MutableMap<String, AdEventProperty> = mutableMapOf()
        mSequenessMap["seq"] = AdEventProperty()
        mSequenessMap["seq"+"1"] = AdEventProperty()
        mSequenessMap["seq"+"2"] = AdEventProperty()
        mSequenessMap["seq"+"3"] = AdEventProperty()
        mSequenessMap["seq"+"4"] = AdEventProperty()
        mSequenessMap["seq"+"5"] = AdEventProperty()
        if (mSequenessMap.size > 5){
            val lastSeq = mSequenessMap.keys.last()
            mSequenessMap.remove(mSequenessMap.keys.last())
        }
    }
}