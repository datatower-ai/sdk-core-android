package com.roiquery.analytics.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.roiquery.analytics.data.room.bean.Configs

/**
 * author: xiaosailing
 * date: 2022-07-28
 * description:
 * version：1.0
 */
@Dao
interface ConfigsDao {

    @Insert
    suspend fun insert(vararg config: Configs)

    @Query("select value from configs where name =:name")
    suspend fun queryValueByName(name: String): String?


    @Query("update configs set value = :value where name = :name")
    suspend fun update(name: String,value:String)

    @Query("select count(*) from configs where name = :name")
    suspend fun existsValue(name: String):Int
}