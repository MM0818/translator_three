package com.example.translator_three.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.translator_three.model.TranslationCache;

@Dao
public interface TranslationCacheDao {
    @Insert
    void insertCache(TranslationCache cache);

    @Query("SELECT * FROM translation_cache WHERE cacheKey = :cacheKey LIMIT 1")
    TranslationCache getCache(String cacheKey);

    @Query("DELETE FROM translation_cache WHERE timestamp < :expireTime")
    void deleteExpiredCache(long expireTime);
}
