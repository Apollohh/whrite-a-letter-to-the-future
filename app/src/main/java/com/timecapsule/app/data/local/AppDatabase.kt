// TODO: 实现 Room 数据库 AppDatabase
package com.timecapsule.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.timecapsule.app.data.local.entity.CapsuleEntity
import com.timecapsule.app.model.CapsuleStatus
import com.timecapsule.app.model.RandomMode
import java.util.UUID

/**
 * Room数据库配置
 * 单例模式，通过getInstance获取
 */
@Database(
    entities = [CapsuleEntity::class],
    version = 1,
    exportSchema = false  // PRD要求，暂不导出schema
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun capsuleDao(): CapsuleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "timecapsule.db"
                )
                    // 预留Migration框架，未来版本使用
                    // .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

/**
 * Room类型转换器
 * 用于存储枚举和UUID等非基本类型
 */
class Converters {

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toUUID(value: String?): UUID? {
        return value?.let { UUID.fromString(it) }
    }

    @TypeConverter
    fun fromCapsuleStatus(status: CapsuleStatus): String {
        return status.name
    }

    @TypeConverter
    fun toCapsuleStatus(value: String): CapsuleStatus {
        return CapsuleStatus.valueOf(value)
    }

    @TypeConverter
    fun fromRandomMode(mode: RandomMode?): String? {
        return mode?.name
    }

    @TypeConverter
    fun toRandomMode(value: String?): RandomMode? {
        return value?.let { RandomMode.valueOf(it) }
    }
}