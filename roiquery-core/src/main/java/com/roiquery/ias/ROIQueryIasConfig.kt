package com.roiquery.ias

import com.roiquery.analytics.utils.LogUtils

/**
 * author: xiaosailing
 * date: 2022-02-24
 * description: 订阅相关配置类
 * version：1.0
 */
internal class ROIQueryIasConfig private constructor(val iasSeq: String, val iasPlacement: String) {
    var iasSku: String = ""
        private set
    var iasOrderId: String = ""
        private set
    var iasPrice: String = ""
        private set
    var iasCurrency: String = ""
        private set
    var iasCode: String = ""
        private set
    var iasMsg: String = ""
        private set
    var iasOriginalOrderId: String = ""
        private set
    var iasEntrance: String = ""
        private set

    private constructor(builder: Builder) : this(builder.iasSeq, builder.iasPlacement) {
        iasSku = builder.iasSku
        iasOrderId = builder.iasOrderId
        iasPrice = builder.iasPrice
        iasCurrency = builder.iasCurrency
        iasCode = builder.iasCode
        iasMsg = builder.iasMsg
        iasOriginalOrderId = builder.iasOriginalOrderId
        iasEntrance = builder.iasEntrance
    }


    fun detectBaseProperties() = iasSeq.isNotEmpty() && iasPlacement.isNotEmpty().apply {
        if (!this) {
            LogUtils.e("please set correct ias properties==> iasSeq, iasPlacement")
        }
    }
    fun detectSubscribeSuccessProperties() =
        detectSubscribeProperties() && iasOriginalOrderId.isNotEmpty().apply {
            if (!this) {
                LogUtils.e("please set correct ias properties==>原始订单id：iasOriginalOrderId")
            }
        }

    fun detectShowFailProperties() = detectBaseProperties() && iasCode.isNotEmpty().apply {
        if (!this) {
            LogUtils.e("please set correct ias properties==> 错误码：iasCode")
        }
    }

    fun detectSubscribeProperties() =
        detectBaseProperties() && iasSku.isNotEmpty() && iasOrderId.isNotEmpty() && iasPrice.isNotEmpty() && iasCurrency.isNotEmpty()
            .apply {
                if (!this) {
                    LogUtils.e("please set correct ias properties")
                }
            }
    fun detectSubscribeFailProperties() =
        detectSubscribeSuccessProperties() && iasCode.isNotEmpty().apply {
            if (!this) {
                LogUtils.e("please set correct ias properties==>错误码：iasCode ")
            }
        }

    class Builder(val iasSeq: String, val iasPlacement: String) {
        var iasEntrance: String = ""
            private set
        var iasSku: String = ""
            private set
        var iasOrderId: String = ""
            private set
        var iasPrice: String = ""
            private set
        var iasCurrency: String = ""
            private set
        var iasCode: String = ""
            private set
        var iasMsg: String = ""
            private set
        var iasOriginalOrderId: String = ""
            private set


        fun iasEntrance(entrance: String?) = apply {
            iasEntrance = entrance?:""
        }

        fun iasSku(sku: String?) = apply {
            iasSku = sku?:""
        }

        fun iasOrderId(orderId: String?) = apply {
            iasOrderId = orderId?:""
        }

        fun iasPrice(price: String?) = apply {
            iasPrice = price?:""
        }

        fun iasCurrency(currency: String?) = apply {
            iasCurrency = currency?:""
        }

        fun iasCode(code: String?) = apply {
            iasCode = code?:""
        }

        fun iasMsg(msg: String?) = apply {
            iasMsg = msg?:""

        }

        fun iasOriginalOrderId(originalOrderId: String?) =apply {
            iasOriginalOrderId = originalOrderId?:""
        }

        fun build() = ROIQueryIasConfig(this)

    }
}