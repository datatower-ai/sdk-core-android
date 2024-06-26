package ai.datatower.analytics.data.room

import ai.datatower.analytics.Constant
import ai.datatower.analytics.data.DataParams
import android.annotation.SuppressLint
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.json.JSONObject

/**
 * author: xiaosailing
 * date: 2022-08-03
 * description:
 * version：1.0
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    @SuppressLint("Range")
    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            database.execSQL("CREATE TABLE IF NOT EXISTS `_new_configs` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `value` TEXT)")
            database.execSQL("INSERT INTO `_new_configs` (`name`,`_id`,`value`) SELECT `name`,`_id`,`value` FROM `configs`")
            database.execSQL("DROP TABLE `configs`")
            database.execSQL("ALTER TABLE `_new_configs` RENAME TO `configs`")
            database.execSQL("CREATE TABLE IF NOT EXISTS `_new_events` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `event_syn` TEXT, `data` TEXT NOT NULL, `created_at` INTEGER NOT NULL)")
            database.execSQL("INSERT INTO `_new_events` (`data`,`created_at`,`_id`) SELECT `data`,`created_at`,`_id` FROM `events`")
            database.execSQL("DROP TABLE `events`")
            database.execSQL("ALTER TABLE `_new_events` RENAME TO `events`")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_events_event_syn` ON `events` (`event_syn`)")


            database.let {
                val cursor = it.query("select _id,${DataParams.KEY_DATA} from events")
                while (cursor.moveToNext()) {
                    val data = cursor.getString(cursor.getColumnIndex(DataParams.KEY_DATA))
                    val id = cursor.getString(cursor.getColumnIndex("_id"))
                    val eventSyn =
                        JSONObject(data).optJSONObject(Constant.EVENT_BODY)?.let { eventBody ->
                            eventBody.optString(Constant.EVENT_INFO_SYN).ifEmpty {
                                eventBody.optString(Constant.PRE_EVENT_INFO_SYN)
                            }
                        }
                    database.execSQL("update events set event_syn= $eventSyn where _id =$id")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
val MIGRATION_2_3 = object : Migration(2, 3) {
    @SuppressLint("Range")
    override fun migrate(database: SupportSQLiteDatabase) {
        try {
            database.execSQL("CREATE TABLE IF NOT EXISTS `_new_configs` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `value` TEXT)")
            database.execSQL("INSERT INTO `_new_configs` (`name`,`_id`,`value`) SELECT `name`,`_id`,`value` FROM `configs`")
            database.execSQL("DROP TABLE `configs`")
            database.execSQL("ALTER TABLE `_new_configs` RENAME TO `configs`")
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `_new_events` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                        " `event_syn` TEXT, `data` TEXT NOT NULL, `created_at` INTEGER NOT NULL,`event_name` TEXT )"
            )
            database.execSQL("INSERT INTO `_new_events` (`data`,`created_at`,`_id`,`event_syn`) SELECT `data`,`created_at`,`_id` ,`event_syn`FROM `events`")
            database.execSQL("DROP TABLE `events`")
            database.execSQL("ALTER TABLE `_new_events` RENAME TO `events`")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_events_event_syn` ON `events` (`event_syn`)")
//        database.execSQL("ALTER TABLE `events` ADD COLUMN  `event_name` INTEGER")


            database.let {
                val cursor = it.query("select _id,${DataParams.KEY_DATA} from events")
                while (cursor.moveToNext()) {
                    val data = cursor.getString(cursor.getColumnIndex(DataParams.KEY_DATA))
                    val id = cursor.getString(cursor.getColumnIndex("_id"))
                    val eventName =
                        JSONObject(data).optJSONObject(Constant.EVENT_BODY)?.optString(Constant.EVENT_INFO_NAME)

                    database.execSQL("update `events` set event_name=$eventName where _id =$id")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}