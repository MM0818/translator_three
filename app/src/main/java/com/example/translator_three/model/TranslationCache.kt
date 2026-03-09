package com.example.translator_three.model;

import androidx.annotation.NonNull; // 新增导入
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "translation_cache")
public class TranslationCache {
    @PrimaryKey
    @NonNull // 关键：添加非空注解，确保cacheKey不会为null
    private String cacheKey; // 格式: "文本-源语言-目标语言"
    private String result;   // 翻译结果
    private long timestamp;  // 缓存时间戳

    // 构造方法中也要确保cacheKey非空（参数添加@NonNull）
    public TranslationCache(@NonNull String cacheKey, String result, long timestamp) {
        this.cacheKey = cacheKey;
        this.result = result;
        this.timestamp = timestamp;
    }

    // Getter和Setter（Setter中也建议添加@NonNull）
    @NonNull
    public String getCacheKey() { return cacheKey; }

    public void setCacheKey(@NonNull String cacheKey) { this.cacheKey = cacheKey; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
