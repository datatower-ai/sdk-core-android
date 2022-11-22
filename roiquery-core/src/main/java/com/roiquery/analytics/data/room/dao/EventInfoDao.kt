package com.roiquery.analytics.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.roiquery.analytics.Constant
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
    suspend fun queryEventData(limit: Int): Array<String>

    @Insert
    fun insertEvent(events: Events): Long

    @Delete
    suspend fun delete(event: Events)

    @Query("select count(*) as num from events")
    suspend fun dataCount(): Int

    @Query("DELETE FROM events WHERE _id IN ( SELECT t._id FROM ( SELECT _id FROM events ORDER BY _id ASC LIMIT :num ) AS t)")
    suspend fun deleteTheOldestData(num: Int)

    @Query("DELETE FROM events WHERE event_syn=:eventSyn")
    suspend fun deleteEventByEventSyn(eventSyn:String)

    @Query("delete  from events")
    suspend fun clearTable()

}