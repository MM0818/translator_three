package com.example.translator_three.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.translator_three.dao.TranslationCacheDao
import com.example.translator_three.model.TranslationCache

// 数据库版本（升级时修改）
@Database(entities = [TranslationCache::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    // 提供 DAO 实例
    abstract fun translationCacheDao(): TranslationCacheDao

    // Kotlin 单例（双重校验锁），避免重复建立数据库连接
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "translator_db"
                )
                    // 开发阶段允许清空数据库（上线前删除）
                    // .fallbackToDestructiveMigration()
                    // 核心2：强制破坏性迁移（清空所有旧数据，重建表）
                    .fallbackToDestructiveMigration()
                    // 额外：兼容所有版本的破坏性迁移（兜底）
                    .fallbackToDestructiveMigrationOnDowngrade()

                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}