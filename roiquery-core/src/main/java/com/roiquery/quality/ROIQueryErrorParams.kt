package com.roiquery.quality

import androidx.annotation.StringDef
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * author: xiaosailing
 * date: 2021-12-01
 * description:
 * version：1.0
 */
internal object ROIQueryErrorParams {

    /**
     * 数据库插入失败
     */
    const val DATA_INSERT_ERROR = "data_insert_error"

    /**
     * 数据库超过最大限制，且正在进行上报
     */
    const val DATA_OVER_NUM = "data_over_num"

    /**
     * sdk 不可用
     */
    const val SDK_INIT_ERROR = "sdk_init_error"

    /**
     * 数据上传Key 值为null
     */
    const val TRACK_PROPERTIES_KEY_NULL = "track_properties_key_null"

    /**
     * oaid 获取失败
     *
     * 不会导致事件不上报，只可能导致 oaid为 空字符串
     */
    const val OAID_ERROR = "oaid_error"

    /**
     * 未知错误
     */
    const val UNKNOWN_TYPE = "unknown_type"

    /**
     * 上报接口访问失败
     */
    const val REPORT_ERROR_ON_FAIL = "report_error_on_fail"

    /**
     * 上报接口访问成功，但业务失败
     */
    const val REPORT_ERROR_ON_RESPONSE = "report_error_on_response"

    /**
     * 事件记录线程池错误
     */
    const val TRACK_TASK_MANAGER_ERROR = "track_task_manager_error"


    @StringDef(
        DATA_INSERT_ERROR,
        DATA_OVER_NUM,
        SDK_INIT_ERROR,
        TRACK_PROPERTIES_KEY_NULL,
        OAID_ERROR,
        UNKNOWN_TYPE
    )
    @Retention(
        RetentionPolicy.SOURCE
    )
    annotation class ROIQueryErrorType
}