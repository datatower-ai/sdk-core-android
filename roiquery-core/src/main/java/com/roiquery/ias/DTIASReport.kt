package com.roiquery.ias

import com.roiquery.ad.utils.UUIDUtils

/**
 * author: xiaosailing
 * date: 2022-02-24
 * description:
 * version：1.0
 */
open class DTIASReport {
    companion object {


        @JvmStatic
        @JvmOverloads
        fun reportSubscribeSuccess(
            iasSeq: String,
            iasEntrance: String? = "",
            iasPlacement: String,
            iasSku: String,
            iasOrderId: String,
            iasOriginalOrderId: String,
            iasPrice: String,
            iasCurrency: String
        ) {
            ROIQueryIasReportImp.trackIasSubSuccessEvent(
                ROIQueryIasConfig.Builder(
                    iasSeq,
                    iasPlacement
                ).iasEntrance(iasEntrance).iasSku(iasSku).iasOrderId(iasOrderId).iasPrice(iasPrice)
                    .iasOriginalOrderId(iasOriginalOrderId).iasCurrency(iasCurrency).build()
            )

        }


        /**
         * 生成UUID
         */
        @JvmStatic
        fun generateUUID() = UUIDUtils.generateUUID()

    }
}