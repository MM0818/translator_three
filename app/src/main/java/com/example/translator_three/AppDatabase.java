package com.example.translator_three;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {User.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;

    // 获取Dao
    public abstract UserDao userDao();

    // 单例模式
    public static synchronized AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "translator_db"
                    ).allowMainThreadQueries() // 简化示例，允许主线程操作（实际项目不推荐）
                    .build();
        }
        return INSTANCE;
    }
}
