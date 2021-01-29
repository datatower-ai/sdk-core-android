package com.nodetower.analytics.api

import org.json.JSONObject
import java.util.concurrent.TimeUnit

interface IAnalyticsApi {

    /**
     * 设置当前 serverUrl
     *
     * @param serverUrl 当前 serverUrl
     */
    fun setServerUrl(serverUrl: String?)


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
    var maxCacheSize: Long


    /**
     * 是否请求网络，默认是 true
     *
     * @return 是否请求网络
     */
    val isNetworkRequestEnable: Boolean

    /**
     * 设置是否允许请求网络，默认是 true
     *
     * @param isRequest boolean
     */
    fun enableNetworkRequest(isRequest: Boolean)

    /**
     * 设置 flush 时网络发送策略，默认 3G、4G、WI-FI 环境下都会尝试 flush
     *
     * @param networkType int 网络类型
     */
    fun setFlushNetworkPolicy(networkType: Int)

    /**
     * 两次数据发送的最小时间间隔，单位毫秒
     * 默认值为 15 * 1000 毫秒
     * 在每次调用 track、signUp 以及 profileSet 等接口的时候，都会检查如下条件，以判断是否向服务器上传数据:
     * 1. 是否是 WIFI/3G/4G 网络条件
     * 2. 是否满足发送条件之一:
     * 1) 与上次发送的时间间隔是否大于 flushInterval
     * 2) 本地缓存日志数目是否大于 flushBulkSize
     * 如果满足这两个条件，则向服务器发送一次数据；如果不满足，则把数据加入到队列中，等待下次检查时把整个队列的内
     * 容一并发送。需要注意的是，为了避免占用过多存储，队列最多只缓存 20MB 数据。
     *
     * @return 返回时间间隔，单位毫秒
     */
    /**
     * 设置两次数据发送的最小时间间隔
     *
     * @param flushInterval 时间间隔，单位毫秒
     */

    var flushInterval: Int
    /**
     * 返回本地缓存日志的最大条目数
     * 默认值为 100 条
     * 在每次调用 track、signUp 以及 profileSet 等接口的时候，都会检查如下条件，以判断是否向服务器上传数据:
     * 1. 是否是 WIFI/3G/4G 网络条件
     * 2. 是否满足发送条件之一:
     * 1) 与上次发送的时间间隔是否大于 flushInterval
     * 2) 本地缓存日志数目是否大于 flushBulkSize
     * 如果满足这两个条件，则向服务器发送一次数据；如果不满足，则把数据加入到队列中，等待下次检查时把整个队列的内
     * 容一并发送。需要注意的是，为了避免占用过多存储，队列最多只缓存 32MB 数据。
     *
     * @return 返回本地缓存日志的最大条目数
     */
    /**
     * 设置本地缓存日志的最大条目数，最小 50 条
     *
     * @param flushBulkSize 缓存数目
     */
    var flushBulkSize: Int
    /**
     * 设置 App 切换到后台与下次事件的事件间隔
     * 默认值为 30*1000 毫秒
     * 若 App 在后台超过设定事件，则认为当前 Session 结束，发送 $AppEnd 事件
     *
     * @return 返回设置的 SessionIntervalTime ，默认是 30s
     */
    /**
     * 设置 App 切换到后台与下次事件的事件间隔
     * 默认值为 30*1000 毫秒
     * 若 App 在后台超过设定事件，则认为当前 Session 结束，发送 $AppEnd 事件
     *
     * @param sessionIntervalTime int
     */
    var sessionIntervalTime: Int


    /**
     * 获取当前用户的 loginId
     * 若调用前未调用 [.login] 设置用户的 loginId，会返回 null
     *
     * @return 当前用户的 loginId
     */
    fun getAccountId(): String?


    /**
     * 获取当前用户的 appid
     * 后台分配，本地保存
     *
     * @return 当前用户的 loginId
     */
    fun getAppId(): String?


    /**
     * 记录 $AppInstall 事件，用于在 App 首次启动时追踪渠道来源，并设置追踪渠道事件的属性。
     * 这是 Sensors Analytics 进阶功能，请参考文档 https://sensorsdata.cn/manual/track_installation.html
     *
     * @param properties 渠道追踪事件的属性
     */
    fun trackAppInstall(properties: JSONObject?)

    /**
     * 记录 $AppInstall 事件，用于在 App 首次启动时追踪渠道来源，并设置追踪渠道事件的属性。
     * 这是 Sensors Analytics 进阶功能，请参考文档 https://sensorsdata.cn/manual/track_installation.html
     */
    fun trackAppInstall()

    /**
     * 调用 track 接口，追踪一个带有属性的事件
     *
     * @param eventName 事件的名称
     * @param properties 事件的属性
     */
    fun track(eventName: String?, properties: JSONObject?)

    /**
     * 与 [.track] 类似，无事件属性
     *
     * @param eventName 事件的名称
     */
    fun track(eventName: String?)


    /**
     * App 退出或进到后台时清空 referrer，默认情况下不清空
     */
    fun clearReferrerWhenAppEnd()


    val mainProcessName: String?


    /**
     * 将所有本地缓存的日志发送到 Sensors Analytics.
     */
    fun flush()

    /**
     * 以阻塞形式将所有本地缓存的日志发送到 Sensors Analytics
     */
    fun flushSync()

    /**
     * 获取事件公共属性
     *
     * @return 当前所有 Super 属性
     */
    val superProperties: JSONObject?

    /**
     * 设置 Cookie，flush 的时候会设置 HTTP 的 cookie
     * 内部会 URLEncoder.encode(cookie, "UTF-8")
     *
     * @param cookie String cookie
     * @param encode boolean 是否 encode
     */
    fun setCookie(cookie: String?, encode: Boolean)

    /**
     * 获取已设置的 Cookie
     * URLDecoder.decode(Cookie, "UTF-8")
     *
     * @param decode String
     * @return String cookie
     */
    fun getCookie(decode: Boolean): String?

    /**
     * 删除本地缓存的全部事件
     */
    fun deleteAll()


    /**
     * 停止事件采集，注意不要随便调用，调用后会造成数据丢失。
     */
    fun stopTrackThread()

    /**
     * 开启事件采集
     */
    fun startTrackThread()

    /**
     * 开启数据采集
     */
    fun enableDataCollect()


    /**
     * 获取当前屏幕方向
     *
     * @return portrait:竖屏 landscape:横屏
     */
    fun getScreenOrientation(): String?

}


