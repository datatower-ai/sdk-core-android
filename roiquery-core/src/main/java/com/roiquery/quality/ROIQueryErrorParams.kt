package com.roiquery.quality

import androidx.annotation.IntDef
import androidx.annotation.StringDef

/**
 * author: xiaosailing
 * date: 2021-12-01
 * description:
 * version：1.0
 */
internal object ROIQueryErrorParams {

    //init
    const val CODE_INIT_CONFIG_ERROR = 1001
    const val CODE_INIT_EXCEPTION = 1002
    const val CODE_GET_ORIGINAL_ID_EXCEPTION = 1003
    const val CODE_INIT_DTID_EXCEPTION = 1004
    const val CODE_SHA1_DTID_EXCEPTION = 1005

    //track
    const val CODE_TRACK_ERROR = 2001
    const val CODE_TRACK_EVENT_NAME_EMPTY = 2011
    const val CODE_TRACK_EVENT_ILLEGAL = 2012
    const val CODE_INIT_DB_ERROR = 2007
    const val CODE_INSERT_DB_NORMAL_ERROR = 2003
    const val CODE_INSERT_DB_OUT_OF_ROW_ERROR = 2004
    const val CODE_INSERT_DB_EXCEPTION = 2005
    const val CODE_INSERT_JSON_EXCEPTION = 2006
    const val CODE_INSERT_DATA_EXCEPTION = 2008
    const val CODE_QUERY_DB_ERROR = 2009
    const val CODE_QUERY_DB_EXCEPTION = 2010
    const val CODE_DELETE_UPLOADED_EXCEPTION = 2012
    const val CODE_DELETE_DB_EXCEPTION = 2013
    const val CODE_DB_DATA_COUNT = 2014
    const val CODE_GAID_LIMIT = 2015
    const val CODE_GAID_UNKOWN = 2016

    //upload
    const val CODE_HANDLE_UPLOAD_MESSAGE_ERROR = 3001
    const val CODE_CHECK_ENABLE_UPLOAD_EXCEPTION = 3002
    const val CODE_REPORT_ERROR_ON_FAIL = 3003
    const val CODE_REPORT_ERROR_ON_RESPONSE = 3004
    const val CODE_UPLOAD_ERROR_MULTi_TIMES = 3005
    const val CODE_UPLOAD_ERROR_READ_DATA = 3006
    const val CODE_UPLOAD_ERROR_OVER_MAX = 3007

    //queue
    const val CODE_QUEUE_MAIN_DEAD = 4001
    const val CODE_QUEUE_UPLOAD_DEAD = 4002
    const val CODE_QUEUE_DB_DEAD = 4003

    @IntDef(

        CODE_INIT_CONFIG_ERROR,
        CODE_INIT_EXCEPTION,
        CODE_GET_ORIGINAL_ID_EXCEPTION,
        CODE_INIT_DTID_EXCEPTION,
        CODE_SHA1_DTID_EXCEPTION,

        CODE_TRACK_ERROR,
        CODE_INIT_DB_ERROR,
        CODE_INSERT_DB_NORMAL_ERROR,
        CODE_INSERT_DB_OUT_OF_ROW_ERROR,
        CODE_INSERT_DB_EXCEPTION,
        CODE_INSERT_JSON_EXCEPTION,
        CODE_INSERT_DATA_EXCEPTION,
        CODE_QUERY_DB_ERROR,
        CODE_QUERY_DB_EXCEPTION,
        CODE_DELETE_UPLOADED_EXCEPTION,
        CODE_DELETE_DB_EXCEPTION,
        CODE_DB_DATA_COUNT,
        CODE_GAID_LIMIT,
        CODE_GAID_UNKOWN,

        CODE_HANDLE_UPLOAD_MESSAGE_ERROR,
        CODE_CHECK_ENABLE_UPLOAD_EXCEPTION,
        CODE_REPORT_ERROR_ON_FAIL,
        CODE_REPORT_ERROR_ON_RESPONSE,
        CODE_UPLOAD_ERROR_MULTi_TIMES,
        CODE_UPLOAD_ERROR_READ_DATA,
        CODE_UPLOAD_ERROR_OVER_MAX,

        CODE_QUEUE_MAIN_DEAD,
        CODE_QUEUE_UPLOAD_DEAD,
        CODE_QUEUE_DB_DEAD
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class ROIQueryErrorCode


    const val TYPE_ERROR = 1
    const val TYPE_WARNING = 2
    const val TYPE_MESSAGE = 3

    @IntDef(
        TYPE_ERROR,
        TYPE_MESSAGE,
        TYPE_WARNING
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class ROIQueryErrorLevel


    const val INIT_CONFIG_ERROR = "can not get config "
    const val INIT_EXCEPTION = "throw exception when sdk init "
    const val TRACK_MANAGER_ERROR = "track task manager throws exception "
    const val TRACK_GENERATE_EVENT_ERROR = " throw exception when generate event info "
    const val INIT_DB_ERROR = "can not get db instance "
    const val INSERT_DB_NORMAL_ERROR = "insert data failed "
    const val INSERT_DB_OUT_OF_ROW_ERROR = "data counts over 500 "
    const val INSERT_DB_EXCEPTION = "throw exception when insert data "
    const val INSERT_JSON_EXCEPTION = "throw exception when add json data "
    const val INSERT_OLD_DATA_EXCEPTION = "throw exception when try to insert old data "
    const val DELETE_DB_EXCEPTION = "throw exception when delete data "
    const val HANDLE_UPLOAD_MESSAGE_ERROR = "throw exception when send meassage to upload data "
    const val CHECK_ENABLE_UPLOAD_EXCEPTION = "throw exception when check upload "


    @StringDef(
        INIT_CONFIG_ERROR,
        INIT_EXCEPTION,
        TRACK_MANAGER_ERROR,
        TRACK_GENERATE_EVENT_ERROR,
        INIT_DB_ERROR,
        INSERT_DB_NORMAL_ERROR,
        INSERT_DB_OUT_OF_ROW_ERROR,
        INSERT_DB_EXCEPTION,
        INSERT_JSON_EXCEPTION,
        INSERT_OLD_DATA_EXCEPTION,
        DELETE_DB_EXCEPTION,
        HANDLE_UPLOAD_MESSAGE_ERROR,
        CHECK_ENABLE_UPLOAD_EXCEPTION
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class ROIQueryErrorMsg

}