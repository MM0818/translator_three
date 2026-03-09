//package com.example.translator_three;
//
//import android.content.Context;
//import androidx.room.Database;
//import androidx.room.Room;
//import androidx.room.RoomDatabase;
//
//import com.example.translator_three.dao.TranslationCacheDao;
//import com.example.translator_three.dao.UserDao;
//import com.example.translator_three.model.TranslationCache;
//import com.example.translator_three.model.User;
//
//@Database(
//        entities = {User.class, TranslationCache.class},
//        version = 2,  // 已更新为2
//        exportSchema = false
//)
//public abstract class AppDatabase extends RoomDatabase {
//    //  volatile 关键字确保多线程可见性（之前缺失，补充上）
//    private static  volatile AppDatabase INSTANCE;
//
//    public abstract UserDao userDao();
//    public abstract TranslationCacheDao translationCacheDao();
//
//    public static synchronized AppDatabase getDatabase(Context context) {
//        if (INSTANCE == null) {
//            INSTANCE = Room.databaseBuilder(
//                            context.getApplicationContext(),
//                            AppDatabase.class,
//                            "translator_db"
//                    )
//                    .allowMainThreadQueries()
//                    // 关键：添加允许破坏性迁移
//                    .fallbackToDestructiveMigration()
//                    .build();
//        }
//        return INSTANCE;
//    }
//
//    // 提供销毁实例方法（方便测试时重置）
//    public static void destroyInstance() {
//        INSTANCE = null;
//    }
//}



package com.example.translator_three;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.translator_three.dao.UserDao; // 只保留 UserDao
import com.example.translator_three.model.User; // 只保留 User 实体

// 核心修改1：entities 仅保留 User.class，移除 TranslationCache.class
// 核心修改2：版本号可保留 2（也可重置为 1，建议重置避免旧版本冲突）
@Database(
        entities = {User.class},
        version = 1,  // 重置为 1，彻底清空旧的冲突数据
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    // 核心修改3：只保留 UserDao，移除 TranslationCacheDao
    public abstract UserDao userDao();

    public static synchronized AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "user_db" // 已改为 user_db，专属用户数据
                    )
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
