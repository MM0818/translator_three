package com.example.translator_three.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.translator_three.dao.TranslationCacheDao
import com.example.translator_three.model.TranslationCache

//Room组件之一，数据库类，用于保存数据库并作为应用持久性数据底层连接的主要访问点。
// 数据库版本（升级时修改）
@Database(entities = [TranslationCache::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    // 提供 DAO 实例
    abstract fun translationCacheDao(): TranslationCacheDao

    // Kotlin 单例（双重校验锁），避免重复建立数据库连接
    //Java的static 变量/方法	= companion object { } 内的成员

//    companion object {
//        @Volatile
//        private var INSTANCE: AppDatabase? = null
//
//        fun getInstance(context: Context): AppDatabase {
//            return INSTANCE ?: synchronized(this) {
//                val instance = Room.databaseBuilder(
//                    context.applicationContext,
//                    AppDatabase::class.java,
//                    "translator_db"
//                )
//                    // 开发阶段允许清空数据库（上线前删除）
//                    // .fallbackToDestructiveMigration()
//                    // 核心2：强制破坏性迁移（清空所有旧数据，重建表）
//                    .fallbackToDestructiveMigration()
//                    // 额外：兼容所有版本的破坏性迁移（兜底）
//                    .fallbackToDestructiveMigrationOnDowngrade()
//
//                    .build()
//                INSTANCE = instance
//                instance
//            }
//        }
//    }

    //这个才是双重检查好不，有时间测试了的话要是运行没错就把上面那注释删了吧，我见代码后面有变化
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            // 第一次检查（无锁）
            return INSTANCE ?: synchronized(this) {
                // 第二次检查（锁内，防止其他线程已初始化）
                INSTANCE ?: run {
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "translator_db"
                    )
                        .fallbackToDestructiveMigration()  // 开发阶段允许清空数据库（上线前删除）（清空所有旧数据，重建表）
                        .fallbackToDestructiveMigrationOnDowngrade()  // 额外：兼容所有版本的破坏性迁移（兜底）
                        .build()
                        .also { INSTANCE = it }  // 赋值并返回
                }
            }
        }
    }
}