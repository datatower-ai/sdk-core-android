package ai.datatower.analytics.data.room

import ai.datatower.analytics.data.DataParams.Companion.DATABASE_NAME
import ai.datatower.analytics.data.room.bean.Configs
import ai.datatower.analytics.data.room.bean.Events
import ai.datatower.analytics.data.room.dao.ConfigsDao
import ai.datatower.analytics.data.room.dao.EventInfoDao
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

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
abstract class DTAnalyticsDB : RoomDatabase() {
    abstract fun getEventsDao(): EventInfoDao
    abstract fun getConfigDao(): ConfigsDao

    companion object {
        @Volatile
        private var instance: DTAnalyticsDB? = null
        fun getInstance(context: Context): DTAnalyticsDB? {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): DTAnalyticsDB? {
            return try {
                // migrate from "roiquery_analytics_db" -> "datatower_ai_core_db"
                val dbFile = context.applicationContext.getDatabasePath("roiquery_analytics_db")
                if (dbFile.exists()) {
                    val newDbFile = context.applicationContext.getDatabasePath(DATABASE_NAME)
                    dbFile.renameTo(newDbFile)
                }

                Room.databaseBuilder(
                    context,
                    DTAnalyticsDB::class.java,
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