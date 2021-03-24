package com.roiquery.ad.api

import com.roiquery.ad.utils.UUIDUtils

open class ROIQueryAdReport {
    companion object {


        /**
         * 上报 广告入口
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        fun reportEntrance(
            id: String,
            type: Int,
            platform: Int,
            location: String,
            seq: String,
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportEntrance(id, type, platform, location, seq, entrance)

        /**
         * 上报 广告展示请求
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        fun reportToShow(
            id: String,
            type: Int,
            platform: Int,
            location: String,
            seq: String,
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportToShow(id, type, platform, location, seq, entrance)

        /**
         * 上报 广告展示
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        fun reportShow(
            id: String,
            type: Int,
            platform: Int,
            location: String,
            seq: String,
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportShow(id, type, platform, location, seq, entrance)

        /**
         * 上报 广告关闭
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        fun reportClose(
            id: String,
            type: Int,
            platform: Int,
            location: String,
            seq: String,
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportClose(id, type, platform, location, seq, entrance)

        /**
         * 上报 广告点击
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        fun reportClick(
            id: String,
            type: Int,
            platform: Int,
            location: String,
            seq: String,
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportClick(id, type, platform, location, seq, entrance)

        /**
         * 上报 激励广告已获得奖励
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        fun reportRewarded(
            id: String,
            type: Int,
            platform: Int,
            location: String,
            seq: String,
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportRewarded(id, type, platform, location, seq, entrance)

        /**
         * 上报 访问广告链接，离开当前app(页面)
         *
         * @param id 广告最小单元id
         * @param type 广告类型
         * @param platform 广告平台
         * @param location 广告位
         * @param seq 系列行为标识
         * @param entrance 广告入口
         */
        @JvmOverloads
        @JvmStatic
        fun reportLeftApp(
            id: String,
            type: Int,
            platform: Int,
            location: String,
            seq: String,
            entrance: String? = ""
        ) = AdReportImp.getInstance()
            .reportLeftApp(id, type, platform, location, seq, entrance)

        /**
         * 上报 访问广告链接，回到当前app(页面)
         */
        @JvmStatic
        fun reportReturnApp() = AdReportImp.getInstance()
            .reportReturnApp()



        /**
         * 生成UUID
         */
        @JvmStatic
        fun generateUUID() = UUIDUtils.generateUUID()

    }
}