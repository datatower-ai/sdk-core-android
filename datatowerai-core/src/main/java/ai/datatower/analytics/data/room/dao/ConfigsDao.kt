package ai.datatower.analytics.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ai.datatower.analytics.data.room.bean.Configs

/**
 * author: xiaosailing
 * date: 2022-07-28
 * description:
 * version：1.0
 */
@Dao
interface ConfigsDao {

    @Insert
    fun insert(vararg config: Configs)

    @Query("select value from configs where name =:name")
    fun queryValueByName(name: String): String?

    @Query("update configs set value = :value where name = :name")
    fun update(name: String,value:String)

    @Query("select count(*) from configs where name = :name")
    fun existsValue(name: String):Int
}