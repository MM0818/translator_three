package com.example.translator_three.dao;

import java.lang.System;

@androidx.room.Dao
@kotlin.Metadata(mv = {1, 8, 0}, k = 1, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0007\bg\u0018\u00002\u00020\u0001J\u0011\u0010\u0002\u001a\u00020\u0003H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0004J\u0019\u0010\u0005\u001a\u00020\u00032\u0006\u0010\u0006\u001a\u00020\u0007H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\bJ+\u0010\t\u001a\u0004\u0018\u00010\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\f2\u0006\u0010\u000e\u001a\u00020\fH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u000fJ\u0019\u0010\u0010\u001a\u00020\u00032\u0006\u0010\u0011\u001a\u00020\nH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0012\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u0013"}, d2 = {"Lcom/example/translator_three/dao/TranslationCacheDao;", "", "clearAllCache", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteExpiredCache", "expireTime", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getCache", "Lcom/example/translator_three/model/TranslationCache;", "sourceLang", "", "targetLang", "sourceText", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertCache", "cache", "(Lcom/example/translator_three/model/TranslationCache;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface TranslationCacheDao {
    
    @org.jetbrains.annotations.Nullable
    @androidx.room.Insert(onConflict = 1)
    public abstract java.lang.Object insertCache(@org.jetbrains.annotations.NotNull
    com.example.translator_three.model.TranslationCache cache, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
    
    /**
     * 根据 源语言+目标语言+源文本 查询缓存（核心查询）
     * 用 suspend 标记为挂起函数，避免主线程操作
     */
    @org.jetbrains.annotations.Nullable
    @androidx.room.Query(value = "\n        SELECT * FROM translation_cache \n        WHERE sourceLang = :sourceLang \n        AND targetLang = :targetLang \n        AND sourceText = :sourceText \n        LIMIT 1\n    ")
    public abstract java.lang.Object getCache(@org.jetbrains.annotations.NotNull
    java.lang.String sourceLang, @org.jetbrains.annotations.NotNull
    java.lang.String targetLang, @org.jetbrains.annotations.NotNull
    java.lang.String sourceText, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super com.example.translator_three.model.TranslationCache> continuation);
    
    /**
     * 删除过期缓存
     */
    @org.jetbrains.annotations.Nullable
    @androidx.room.Query(value = "DELETE FROM translation_cache WHERE timestamp < :expireTime")
    public abstract java.lang.Object deleteExpiredCache(long expireTime, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
    
    /**
     * 清空所有缓存（可选）
     */
    @org.jetbrains.annotations.Nullable
    @androidx.room.Query(value = "DELETE FROM translation_cache")
    public abstract java.lang.Object clearAllCache(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
}