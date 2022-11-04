package com.roiquery.analytics.data.room.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * author: xiaosailing
 * date: 2022-07-28
 * description:
 * version：1.0
 */

@Entity(
    indices = [Index(
        value = ["event_syn"],
        unique = true
    )], tableName = "events"
)
data class Events(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") val id: Long = 0,
    @ColumnInfo(name = "event_syn") val eventSyn: String?,
    @ColumnInfo(name = "data") val data: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "event_name")  val eventName:String?
)
