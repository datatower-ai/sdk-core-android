package com.roiquery.analytics.api

import org.json.JSONObject

interface IAnalytics {


    /**
     * 获取本地缓存上限制
     *
     * @return 字节
     */
    /**
     * 设置本地缓存上限值，单位 byte，默认为 32MB：32 * 1024 * 1024，最小 16MB：16 * 1024 * 1024，若小于 16MB，则按 16MB 处理。
     *
     * @param maxCacheSize 单位 byte
     */
    var maxCacheSize: Long?


    /**
     * 设置两次数据发送的最小时间间隔
     *
     * @param flushInterval 时间间隔，单位毫秒
     */

    var flushInterval: Int?

    /**
     * 设置本地缓存日志的最大条目数，最小 50 条
     *
     * @param flushBulkSize 缓存数目
     */
    var flushBulkSize: Int?

    /**
     * 设置当前用户的 acid
     */
     var accountId: String?

    /**
     * DataTower id
     */
    var dtid: String?

     /**
     * 设置当前用户Firebase的app_instance_id
     */
    var fiid: String?

     /**
     * 设置当前用户Firebase Cloud Message Token
     */
    var fcmToken: String?


    /**
     * 设置当前AppsFlyers id
     */
    var afid: String?


    /**
     * 设置当kochava id
     */
    var koid: String?


//    /**
//     * 是否允许采集开启数据采集
//     */
//    var enableTrack: Boolean?

    /**
     * 是否请求网络，默认是 true
     *
     * @return 是否请求网络
     */
    var enableUpload: Boolean?

    /**
     * 设置 flush 时网络发送策略，默认 3G、4G、WI-FI 环境下都会尝试 flush
     *
     * @param networkType int 网络类型
     */
    var flushNetworkPolicy: Int?

    /**
     * 调用 track 接口，追踪一个带有属性的事件
     *
     * @param eventName 事件的名称
     * @param isPreset 是否是预置事件
     * @param eventType 事件的类型
     */
    fun trackNormal(eventName: String?, isPreset: Boolean, properties: JSONObject? = JSONObject())

     /**
     * 调用 track 用户属性接口，追踪一个带有属性的事件
     *
     * @param eventName 事件的名称
     * @param eventType 事件的类型
     */
    fun trackUser(eventName: String, properties: JSONObject? = JSONObject())


//    /**
//     * 采集 app 退出
//     *
//     * @param eventName 事件的名称
//     */
//    fun trackAppClose(properties: JSONObject? = JSONObject())
//
//
//    /**
//     * 采集页面打开事件
//     *
//     * @param properties 事件的属性
//     */
//    fun trackPageOpen(properties: JSONObject? = JSONObject())
//
//
//    /**
//     * 采集页面关闭事件
//     *
//     * @param properties 事件的属性
//     */
//    fun trackPageClose(properties: JSONObject? = JSONObject())

//
//    /**
//     * 设置用户属性
//     *
//     * @param properties 事件的属性
//     */
//    fun setUserProperties(properties: JSONObject? = JSONObject())


    /**
     * 将所有本地缓存的日志发送到  Analytics.
     */
    fun flush()

    /**
     * 删除本地缓存的全部事件
     */
    fun deleteAll()


}


