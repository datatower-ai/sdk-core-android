package ai.datatower.analytics.data.room

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ai.datatower.analytics.data.DataParams.Companion.DATABASE_NAME
import ai.datatower.analytics.data.room.bean.Configs
import ai.datatower.analytics.data.room.bean.Events
import ai.datatower.analytics.data.room.dao.ConfigsDao
import ai.datatower.analytics.data.room.dao.EventInfoDao

/**
 * author: xiaosailing
 * date: 2022-07-28
 * description:
 * version：1.0
 */

@Database(
    version = 2,
    entities = [Events::class, Configs::class], exportSchema = true
)
abstract class ROIQueryAnalyticsDB : RoomDatabase() {
    abstract fun getEventsDao(): EventInfoDao
    abstract fun getConfigDao(): ConfigsDao

    companion object {
        @Volatile
        private var instance: ROIQueryAnalyticsDB? = null
        fun getInstance(context: Context): ROIQueryAnalyticsDB? {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): ROIQueryAnalyticsDB? {
            return try {
                Room.databaseBuilder(
                    context,
                    ROIQueryAnalyticsDB::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .build()
            } catch (e: Exception){
                null
            }
        }
    }

}