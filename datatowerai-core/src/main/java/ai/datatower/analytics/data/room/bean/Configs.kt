package ai.datatower.analytics.data.room.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * author: xiaosailing
 * date: 2022-07-28
 * description:
 * version：1.0
 */

@Entity(tableName = "configs")
data class Configs(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "_id") val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "value") val value: String?
)
