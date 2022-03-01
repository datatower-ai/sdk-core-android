package com.roiquery.ias

/**
 * author: xiaosailing
 * date: 2022-02-24
 * description:
 * version：1.0
 */
open class ROIQueryIasReport {
    companion object {
        fun reportToShow(iasSeq: String, iasEntrance: String?, iasPlacement: String) {
            ROIQueryIasReportImp.trackIasShowEvent(
                ROIQueryIasConfig.Builder(iasSeq, iasPlacement).iasEntrance(iasEntrance).build()
            )
        }

        fun reportShowSuccess(iasSeq: String, iasEntrance: String?, iasPlacement: String) {
            ROIQueryIasReportImp.trackIasShowSuccessEvent(
                ROIQueryIasConfig.Builder(
                    iasSeq,
                    iasPlacement
                ).iasEntrance(entrance = iasEntrance).build()
            )
        }

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
    }
}