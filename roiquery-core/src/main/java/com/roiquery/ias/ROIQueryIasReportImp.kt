package com.roiquery.ias

import com.roiquery.analytics.DTAnalytics

/**
 * author: xiaosailing
 * date: 2022-02-24
 * description:
 * version：1.0
 */
internal class ROIQueryIasReportImp {
    companion object {

        fun trackIasShowEvent(iasConfig: ROIQueryIasConfig) {
            if (iasConfig.detectBaseProperties()) {
                DTAnalytics.trackInternal(
                    ROIQueryIasConstant.IAS_TO_SHOW_EVENT,
                    iasProperties(iasConfig)
                )
            }
        }


        fun trackIasShowSuccessEvent(iasConfig: ROIQueryIasConfig) {
            if (iasConfig.detectBaseProperties()) {
                DTAnalytics.trackInternal(
                    ROIQueryIasConstant.IAS_SHOW_SUCCESS_EVENT,
                    iasProperties(config = iasConfig)
                )
            }
        }

        fun trackIasShowFailEvent(iasConfig: ROIQueryIasConfig) {
            if (iasConfig.detectShowFailProperties()) {
                DTAnalytics.trackInternal(
                    ROIQueryIasConstant.IAS_SHOW_FAIL_EVENT,
                    iasProperties
                        (
                        iasConfig
                    )
                )
            }
        }

        fun trackIasSubEvent(iasConfig: ROIQueryIasConfig) {
            if (iasConfig.detectSubscribeProperties()) {
                DTAnalytics.trackInternal(
                    ROIQueryIasConstant.IAS_TO_SUBSCRIBE_EVENT,
                    iasProperties
                        (
                        iasConfig
                    )
                )
            }
        }

        fun trackIasSubSuccessEvent(iasConfig: ROIQueryIasConfig) {
            if (iasConfig.detectSubscribeSuccessProperties()) {
                DTAnalytics.trackInternal(
                    ROIQueryIasConstant.IAS_TO_SUBSCRIBE_SUCCESS_EVENT,
                    iasProperties
                        (
                        iasConfig
                    )
                )
            }
        }

        fun trackIasSubFailEvent(iasConfig: ROIQueryIasConfig) {
            if (iasConfig.detectSubscribeFailProperties()) {
                DTAnalytics.trackInternal(
                    ROIQueryIasConstant.IAS_TO_SUBSCRIBE_FAIL_EVENT,
                    iasProperties
                        (
                        iasConfig
                    )
                )
            }
        }


        private fun iasProperties(config: ROIQueryIasConfig): Map<String, Any> {
            val map = HashMap<String, Any>()
            map.apply {
                put(ROIQueryIasConstant.IAS_SEQ, config.iasSeq)

                put(ROIQueryIasConstant.IAS_PLACEMENT, config.iasPlacement)
                if (config.iasEntrance.isNotEmpty()) {
                    put(ROIQueryIasConstant.IAS_ENTRANCE, config.iasEntrance)
                }
                if (config.iasCode.isNotEmpty()) {
                    put(ROIQueryIasConstant.IAS_CODE, config.iasCode)
                }
                if (config.iasMsg.isNotEmpty()) {
                    put(ROIQueryIasConstant.IAS_MSG, config.iasMsg)
                }
                put(ROIQueryIasConstant.IAS_SKU, config.iasSku)
                put(ROIQueryIasConstant.IAS_ORDER_ID, config.iasOrderId)
                put(ROIQueryIasConstant.IAS_PRICE, config.iasPrice)
                put(ROIQueryIasConstant.IAS_CURRENCY, config.iasCurrency)
                put(ROIQueryIasConstant.IAS_ORIGINAL_ORDER_ID, config.iasOriginalOrderId)
            }
            return map
        }
    }
}


