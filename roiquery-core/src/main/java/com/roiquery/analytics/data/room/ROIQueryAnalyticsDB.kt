package com.roiquery.analytics.data.room

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.roiquery.analytics.data.DataParams.Companion.DATABASE_NAME
import com.roiquery.analytics.data.room.bean.Configs
import com.roiquery.analytics.data.room.bean.Events
import com.roiquery.analytics.data.room.dao.ConfigsDao
import com.roiquery.analytics.data.room.dao.EventInfoDao
import com.roiquery.analytics.utils.AppInfoUtils
import com.roiquery.analytics.utils.ProcessUtils

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
            if (ProcessUtils.isMainProcess(context as Application?)){
                return null
            }
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): ROIQueryAnalyticsDB {
            return Room.databaseBuilder(
                context,
                ROIQueryAnalyticsDB::class.java,
                DATABASE_NAME
            ).addMigrations(MIGRATION_1_2).build()
        }
    }

}