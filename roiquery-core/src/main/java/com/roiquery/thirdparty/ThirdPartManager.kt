package com.roiquery.thirdparty

class ThirdPartManager private constructor(){
    companion object{
        val  instance by lazy {
            ThirdPartManager()
        }
    }
}