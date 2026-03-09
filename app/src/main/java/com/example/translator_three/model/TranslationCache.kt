package com.example.translator_three.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "translation_cache",
    indices = [Index(value = ["sourceLang","targetLang","sourceText"], unique = true)]
)
data class TranslationCache(
    //自增主键
    @PrimaryKey(autoGenerate = true)
    var id:Long =0,
    val sourceLang:String,
    val targetLang:String,
    val sourceText:String,
    val translatedText:String,
    val timestamp:Long  //缓存时间戳，用于删除老数据
)
