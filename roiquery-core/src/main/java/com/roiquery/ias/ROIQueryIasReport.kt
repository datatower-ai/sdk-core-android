package com.roiquery.ias

import com.roiquery.ad.utils.UUIDUtils

/**
 * author: xiaosailing
 * date: 2022-02-24
 * description:
 * version：1.0
 */
@Deprecated("this class will be remove,Replace With \'DTIASReport\'",ReplaceWith("DTIASReport"))
open class ROIQueryIasReport {
    companion object {
        @JvmStatic
        @JvmOverloads
        fun reportToShow(iasSeq: String, iasEntrance: String?, iasPlacement: String) {
            ROIQueryIasReportImp.trackIasShowEvent(
                ROIQueryIasConfig.Builder(iasSeq, iasPlacement).iasEntrance(iasEntrance).build()
            )
        }

        @JvmStatic
        @JvmOverloads
        fun reportShowSuccess(iasSeq: String, iasEntrance: String?, iasPlacement: String) {
            ROIQueryIasReportImp.trackIasShowSuccessEvent(
                ROIQueryIasConfig.Builder(
                    iasSeq,
                    iasPlacement
                ).iasEntrance(entrance = iasEntrance).build()
            )
        }

        @JvmStatic
        @JvmOverloads
        fun reportShowFail(
            iasSeq: String,
            iasEntrance: String? = "",
            iasPlacement: String,
            iasCode: String,
            iasMsg: String? = ""
        ) {
            ROIQueryIasReportImp.trackIasShowFailEvent(
                ROIQueryIasConfig.Builder(
                    iasSeq,
                    iasPlacement
                ).iasEntrance(iasEntrance).iasCode(iasCode).iasMsg(iasMsg).build()
            )

        }

        @JvmStatic
        @JvmOverloads
        fun reportSubscribe(
            iasSeq: String,
            iasEntrance: String? = "",
            iasPlacement: String,
            iasSku: String,
            iasOrderId: String,
            iasPrice: String,
            iasCurrency: String
        ) {
            ROIQueryIasReportImp.trackIasSubEvent(
                ROIQueryIasConfig.Builder(
                    iasSeq,
                    iasPlacement
                ).iasEntrance(iasEntrance).iasSku(iasSku).iasOrderId(iasOrderId).iasPrice(iasPrice)
                    .iasCurrency(iasCurrency).build()
            )
        }

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

        @JvmStatic
        @JvmOverloads
        fun reportSubscribeFail(
            iasSeq: String,
            iasEntrance: String? = "",
            iasPlacement: String,
            iasSku: String,
            iasOrderId: String,
            iasOriginalOrderId: String,
            iasPrice: String,
            iasCurrency: String,
            iasCode: String,
            iasMsg: String? = ""
        ) {
            ROIQueryIasReportImp.trackIasSubFailEvent(
                ROIQueryIasConfig.Builder(
                    iasSeq,
                    iasPlacement
                ).iasEntrance(iasEntrance).iasCode(iasCode).iasMsg(iasMsg).iasSku(iasSku)
                    .iasOrderId(iasOrderId).iasPrice(iasPrice)
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