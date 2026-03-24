package com.example.translator_three.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.translator_three.model.TranslationCache

//Room组件之一，数据访问对象 (DAO)，为您的应用提供在数据库中查询、更新、插入和删除数据的方法。
@Dao
interface TranslationCacheDao {
    //冲突时替换，避免重复
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache:TranslationCache)

    /**
     * 根据 源语言+目标语言+源文本 查询缓存（核心查询）
     * 用 suspend 标记为挂起函数，避免主线程操作
     */
    @Query("""
        SELECT * FROM translation_cache 
        WHERE sourceLang = :sourceLang 
        AND targetLang = :targetLang 
        AND sourceText = :sourceText 
        LIMIT 1
    """)
    suspend fun getCache(
        sourceLang: String,
        targetLang: String,
        sourceText: String
    ): TranslationCache?

    /**
     * 删除过期缓存
     */
    @Query("DELETE FROM translation_cache WHERE timestamp < :expireTime")
    suspend fun deleteExpiredCache(expireTime: Long)

    /**
     * 清空所有缓存（可选）
     */
    @Query("DELETE FROM translation_cache")
    suspend fun clearAllCache()
}