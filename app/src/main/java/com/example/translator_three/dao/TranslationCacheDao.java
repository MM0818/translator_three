package com.example.translator_three.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.translator_three.model.TranslationCache;

//TranslationCacheDao 是一个 数据访问对象（Data Access Object，DAO） ，用于定义对翻译缓存数据库的操作。
@Dao  //告诉 Room 这是一个 DAO 接口，Room 会自动生成实现类。
public interface TranslationCacheDao {
    @Insert  //告诉 Room 这是一个插入操作，Room 自动生成 INSERT INTO translation_cache (...) VALUES (...) SQL 语句
    void insertCache(TranslationCache cache);

    //根据缓存键获取缓存记录，@Query 注解 ：自定义查询
    //- 该方法用于根据缓存键查询缓存记录。
    //- :cacheKey 参数是缓存键，用于指定要查询的记录。
    //- LIMIT 1 子句用于限制返回结果最多只有一条记录。
    @Query("SELECT * FROM translation_cache WHERE cacheKey = :cacheKey LIMIT 1")
    TranslationCache getCache(String cacheKey);

    //删除过期的缓存记录，@Query 注解 ：自定义删除查询
    //- 该方法用于删除缓存时间超过指定时间的记录，以释放数据库空间。
    //- :expireTime 参数是一个时间戳，用于指定过期时间。
    @Query("DELETE FROM translation_cache WHERE timestamp < :expireTime")
    void deleteExpiredCache(long expireTime);
}

/*
    Room 是 Android 官方推荐的数据库框架，它简化了 SQLite 数据库的使用。
    Room 的三个核心组件 ：
    1. Entity（实体） ：表示数据库表（如 TranslationCache ）
    2. DAO（数据访问对象） ：定义数据库操作（如 TranslationCacheDao ）
    3. Database（数据库） ：创建和管理数据库（如 AppDatabase ）
*/