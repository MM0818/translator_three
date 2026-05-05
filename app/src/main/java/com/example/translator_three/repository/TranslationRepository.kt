package com.example.translator_three.repository

import android.content.Context
import android.util.Log
import com.example.translator_three.api.BaiduTranslateService
import com.example.translator_three.model.TranslationResponse
import com.example.translator_three.utils.TranslationCacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
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

    //定义协程作用域对象，管理协程生命周期
    //自定义CoroutineScope（带 IO 调度器 + Job 生命周期控制），并用自定义Scope启动协程
    private val coroutineScope = CoroutineScope(Dispatchers.IO + Job())

    init {
        Log.d("TranslationRepo","开始初始化翻译仓库")

        translateService=Retrofit.Builder()   //构造器创建实例
            .baseUrl("https://fanyi-api.baidu.com/api/trans/vip/")  //百度翻译的api
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BaiduTranslateService::class.java)  //创建动态代理对象，也就是我们定义的API接口
            //动态代理内部会帮我们：1. 解析注解 @GET、@Path 2. 构建 OkHttp 请求   3. 用 OkHttp 发请求  4. 解析响应，转成 User 对象
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
    suspend fun translate(text: String,fromLang:String,toLang:String):String? = withContext(Dispatchers.IO){
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
                //Call 代表一次准备好的 HTTP 请求，可以 execute() 执行，或 cancel() 取消。
                val call:Call<TranslationResponse> = translateService.translate(   //创建请求对象（还没发送请求），组装请求
                    cleanText,fromLang,toLang,APP_ID,salt,sign
                )
                val response:Response<TranslationResponse> = call.execute()   //真正执行同步网络请求，等待结果返回（决定什么时候发，现在、待会、取消）

                if(response.isSuccessful&&response.body()!=null){  //Http成功且body有数据
                    val body=response.body()!!  //，取body，断言一定不为空（因为编译器不知道你判断过）
                    //如果不用断言： ❌ 编译报错！Type is TranslationResponse?
                    // 原因：body() 是个方法，每次调用可能返回不同值
                    // 编译器认为"你现在判断了，但下一行调用可能又变 null"

                    if(body.error_code==null && body.trans_result!=null && body.trans_result.isNotEmpty()){  //业务成功，API没报错
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

//        //启动协程执行翻译
//        GlobalScope.launch(Dispatchers.IO){
//            try {
//                val result=translate(text)  //调用原因挂起函数
//                future.complete(result)  //翻译成功，返回结果
//            }catch (e:Exception){
//                future.completeExceptionally(e)  //翻译失败，返回异常
//            }
//        }

        //GlobalScope：生命周期与应用进程一致，易导致内存泄漏或协程无法取消
        //替换方案：CoroutineScope，受控于调用者的生命周期，，更安全，可在生命周期结束时取消
        //自定义CoroutineScope（带IO调度器+Job），并用自定义Scope启动协程
        coroutineScope.launch {
            try {
                val result = translate(text)
                future.complete(result)
            }catch (e:Exception){
                future.completeExceptionally(e)
            }
        }
        return future
    }

    //协程结束后取消Scope，避免内存泄漏
    //单独写一个函数供外部销毁时调用（如无障碍服务onDestroy时）
    fun cancelAllCoroutines(){
        coroutineScope.cancel()
    }
}