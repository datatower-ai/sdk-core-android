package com.roiquery.analytics.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.roiquery.analytics.data.room.bean.Events

/**
 * author: xiaosailing
 * date: 2022-07-28
 * description:
 * version：1.0
 */
@Dao
interface EventInfoDao {

    @Query("SELECT data FROM events limit 0,:limit ")
    fun queryEventData(limit: Int): Array<String>

    @Insert
    fun insertEvent(events: Events): Long

    @Delete
    fun delete(event: Events)

    @Query("select count(*) as num from events")
    fun dataCount(): Int

    @Query("DELETE FROM events WHERE _id IN ( SELECT t._id FROM ( SELECT _id FROM events ORDER BY _id ASC LIMIT :num ) AS t)")
    fun deleteTheOldestData(num: Int)

    @Query("DELETE FROM events WHERE event_syn=:eventSyn")
    fun deleteEventByEventSyn(eventSyn:String)

    @Query("DELETE FROM events WHERE event_syn IN (:eventSyn )")
    fun deleteBatchEventByEventSyn(eventSyn: List<String>)

    @Query("delete  from events")
    fun clearTable()

}