package com.roiquery.analytics.utils

object NetworkType {
    // NULL
    var TYPE_NONE = 0

    // 2G
    var TYPE_2G = 1

    // 3G
    var TYPE_3G = 1 shl 1

    // 4G
    var TYPE_4G = 1 shl 2

    // WIFI
    var TYPE_WIFI = 1 shl 3

    // 5G
    var TYPE_5G = 1 shl 4

    // ALL
    var TYPE_ALL = 0xFF
}