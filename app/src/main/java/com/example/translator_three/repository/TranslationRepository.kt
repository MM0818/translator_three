package com.example.translator_three.repository

import android.content.Context
import android.util.Log
import com.example.translator_three.api.BaiduTranslateService
import com.example.translator_three.model.TranslationResponse
import com.example.translator_three.utils.TranslationCacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.CompletableFuture

class TranslationRepository (context: Context){
    //缓存管理器（懒加载，首次使用时初始化，后面再使用就不用再初始化了）
    private val cacheManager by lazy { TranslationCacheManager(context.applicationContext) }

    private val translateService:BaiduTranslateService

    private val MAX_RETRY_COUNT = 3
    private val APP_ID = "20240826002132755"  //百度创建的应用翻译API的appid
    private val SECRET_KEY="uUnQQAsgQKUFPPZL3HZH"  //百度翻译API的密钥

    init {
        Log.d("TranslationRepo","开始初始化翻译仓库")

        translateService=Retrofit.Builder()
            .baseUrl("https://fanyi-api.baidu.com/api/trans/vip/")  //百度翻译的api
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BaiduTranslateService::class.java)
        Log.d("TranslationRepo","翻译仓库初始化完成")
    }

    //智能翻译，自动判断语音
    suspend fun translate(text:String):String?{
        return if(isChineseText(text)){
            translate(text,"zh","en")  //默认中译英
        }else{
            translate(text,"auto","zh")
        }
    }

    //核心翻译（协程版）
    suspend fun translate(text: String,fromLang:String,toLang:String):String?= withContext(Dispatchers.IO){
        Log.d("TranslationRepo","开始翻译：$text 从 $fromLang 到 $toLang")
        //1、优先查询缓存
        val cachedResult=cacheManager.getTranslatedCache(fromLang,toLang,text)
        if (cachedResult!=null){
            Log.d("TranslationRepo","缓存命中，返回结果：$cachedResult")
            return@withContext cachedResult
        }
        Log.d("TranslationRepo","缓存未命中，做网络请求调用API")

        //2、调用百度翻译API
        var retryCount=0  //重试请求次数
        while (retryCount<MAX_RETRY_COUNT){
            try {
                Log.d("TranslationRepo","第${retryCount+1}次调用API")
                val cleanText=text.trim()
                val salt=System.currentTimeMillis().toString()
                val sign=generateSign(cleanText,salt)

                //同步调用API（因为当前函数块已在IO线程，无需再异步。相对主线程这里就是用了异步请求哈）
                val call:Call<TranslationResponse> = translateService.translate(
                    cleanText,fromLang,toLang,APP_ID,salt,sign
                )
                val response:Response<TranslationResponse> = call.execute()

                if(response.isSuccessful&&response.body()!=null){
                    val body=response.body()!!  //断言一定不为空
                    if(body.error_code==null && body.trans_result!=null && body.trans_result.isNotEmpty()){
                        val result=body.trans_result[0].dst
                        Log.d("TranslationRepo","API翻译成功：$result")

                        //3、写入缓存
                        cacheManager.insertTranslatedCache(fromLang,toLang,cleanText,result)
                        Log.d("TranslationRepo","结果已缓存")
                        return@withContext result
                    }else{
                        Log.e("TranslationRepo","API返回错误：${body.error_code} - ${body.error_msg}")
                    }
                }else{
                    Log.e("TranslationRepo","网络请求失败：${response.message()}")
                }
            }catch (e:IOException){
                Log.d("TranslationRepo","网络异常：${e.message}")
                retryCount++;
                if(retryCount>=MAX_RETRY_COUNT) break
                //重试间隔1s
                kotlinx.coroutines.delay(1000)
            }catch (e:Exception){
                Log.e("TranslationRepo","未知异常",e)
                break
            }
        }

        Log.e("TranslationRepo","所有重试失败，返回null")
        null
    }

    //判断是否为中文文本
    private fun isChineseText(text: String):Boolean{
        if(text.isBlank()) return false
        val chineseCharCount=text.count { it in '\u4E00'..'\u9FFF' && it.isLetter() }
        val totalCharCount=text.count { it.isLetter() }  //isLetter，判断字符是否为字母
        return totalCharCount>0 && (chineseCharCount*100 / totalCharCount)>30
    }

    //生成百度翻译签名
    private fun generateSign(text:String,salt:String):String{
        val input="$APP_ID$text$salt$SECRET_KEY"
        return try {
            val md=MessageDigest.getInstance("MD5")
            val bytes=md.digest(input.toByteArray())
            bytes.joinToString(""){"%02x".format(it)}
        }catch (e:Exception){
            Log.d("TranslationRepo", "MD5加密失败",e)
            ""
        }
    }

    //清理过期缓存（外部调用）
    suspend fun clearExpiredCache(){
        cacheManager.clearExpiredCache()
    }

    //Java兼容方法
    /*
    * 供Java调用的翻译方法，返回CompletableFuture
    * 适配无障碍服务的Java代码调用
    * */
    fun translateForJava(text: String):CompletableFuture<String?>{
        val future=CompletableFuture<String?>()

        //启动协程执行翻译
        GlobalScope.launch(Dispatchers.IO){
            try {
                val result=translate(text)  //调用原因挂起函数
                future.complete(result)  //翻译成功，返回结果
            }catch (e:Exception){
                future.completeExceptionally(e)  //翻译失败，返回异常
            }
        }
        return future
    }
}