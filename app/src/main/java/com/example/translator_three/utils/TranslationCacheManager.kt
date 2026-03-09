package com.example.translator_three.utils

import android.content.Context
import com.example.translator_three.database.AppDatabase
import com.example.translator_three.model.TranslationCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

//用来写对数据库对象DAO操作的增删改查实现方法
class TranslationCacheManager(private val context:Context) {
    //懒加载数据库实例（仅仅首次使用时初始化），延迟初始化只读变量val，线程安全（同步锁）
    private val db by lazy { AppDatabase.getInstance(context) }
    private val cacheDao by lazy { db.translationCacheDao() }

    //缓存有效期（7天）
    private val cacheExpireDays=7L

    //查找缓存（挂起函数，异步执行）
    suspend fun getTranslatedCache(
        sourceLang:String,
        targetLang:String,
        sourceText:String
    ):String? = withContext(Dispatchers.IO){
        //文本预处理
        val cleanText=sourceText.trim()
        val cache = cacheDao.getCache(sourceLang,targetLang,cleanText)

        //检查是否过期
        if(cache!=null && !isCacheExpired(cache.timestamp)){
            cache.translatedText
        }else{
            null
        }
    }


    /**
     * 插入缓存（挂起函数）
     */
    suspend fun insertTranslatedCache(
        sourceLang: String,
        targetLang: String,
        sourceText: String,
        translatedText: String
    ) = withContext(Dispatchers.IO) {
        val cleanText = sourceText.trim()
        val cache = TranslationCache(
            sourceLang = sourceLang,
            targetLang = targetLang,
            sourceText = cleanText,
            translatedText = translatedText,
            timestamp = System.currentTimeMillis()
        )
        cacheDao.insertCache(cache)
    }

    /**
     * 清理过期缓存
     */
    suspend fun clearExpiredCache() = withContext(Dispatchers.IO) {
        val expireTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(cacheExpireDays)
        cacheDao.deleteExpiredCache(expireTime)
    }

    /**
     * 清空所有缓存
     */
    suspend fun clearAllCache() = withContext(Dispatchers.IO) {
        cacheDao.clearAllCache()
    }

    /**
     * 检查缓存是否过期
     */
    private fun isCacheExpired(timestamp: Long): Boolean {
        val expireTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(cacheExpireDays)
        return timestamp < expireTime
    }

}