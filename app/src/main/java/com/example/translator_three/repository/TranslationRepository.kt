package com.example.translator_three.repository;

import android.content.Context;
import android.util.Log;

import com.example.translator_three.AppDatabase;
import com.example.translator_three.api.BaiduTranslateService;
import com.example.translator_three.dao.TranslationCacheDao;
import com.example.translator_three.model.TranslationCache;
import com.example.translator_three.model.TranslationResponse;
import com.example.translator_three.model.User;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//TranslationRepository 承担了数据获取和处理的核心职责，使得上层组件可以专注于业务逻辑和用户交互，而不必关心底层的 API 调用细节。
public class TranslationRepository {
    // 把单线程池改成缓存线程池（临时解决阻塞问题）
    //ES：Java并发编程框架中的核心接口，Executors是工具类，提供创建线程池的静态方法，下面就是创建了一个可缓存的线程池
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final BaiduTranslateService translateService;
    private final TranslationCacheDao cacheDao;
    //private final ExecutorService executorService;
    private static final int MAX_RETRY_COUNT = 3; // 最大重试次数
    private static final int CACHE_EXPIRE_DAYS = 7; // 缓存有效期7天
    private static final String APP_ID = "20240826002132755"; // APP_ID
    private static final String SECRET_KEY = "uUnQQAsgQKUFPPZL3HZH"; // 密钥

    //初始化翻译仓库，配置网络请求、缓存机制和线程池等核心组件
    public TranslationRepository(Context context) {  //应用上下文，用于访问系统资源和服务
        Log.d("TranslationRepo", " 开始初始化 TranslationRepository");

        // 初始化Retrofit（预加载，避免首次调用耗时）
        try {
            // 初始化Retrofit（预加载，避免首次调用耗时）
            Log.d("TranslationRepo", " 初始化 Retrofit...");
            // 创建网络请求框架Retrofit实例，用于调用RESTful API
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://fanyi-api.baidu.com/api/trans/vip/")  //百度翻译API的基础地址
                    .addConverterFactory(GsonConverterFactory.create())  //添加Gson转换器，用于将JSON响应转换为Java对象
                    .build(); 
            //创建翻译服务接口实例
            this.translateService = retrofit.create(BaiduTranslateService.class);
            Log.d("TranslationRepo", " Retrofit 初始化完成");

            // 注释数据库相关代码（临时关闭缓存）
            Log.d("TranslationRepo", " 设置缓存DAO为null...");
            this.cacheDao = null; // 避免空指针，这里指缓存功能暂未实现
            Log.d("TranslationRepo", " 缓存DAO设置完成");

            Log.d("TranslationRepo", " 线程池初始化完成");

            Log.d("TranslationRepo", " TranslationRepository 初始化完成！");

        } catch (Exception e) {
            Log.e("TranslationRepo", " TranslationRepository 初始化失败！", e);
            throw new RuntimeException("TranslationRepository 初始化失败", e);
        }
    }

    //Future接口：表示异步计算的结果，用于在未来的某个时间获取计算结果
    public Future<String> translate(String text) {
        // 智能判断：如果是中文就翻译成英文，否则翻译成中文
        if (isChineseText(text)) {
            return translate(text, "zh", "en");  // 中文 → 英文
        } else {
            return translate(text, "auto", "zh");  // 其他语言 → 中文
        }
    }

    // 判断文本是否主要为中文
    private boolean isChineseText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;  //空文本直接返回false
        }

        int chineseCharCount = 0;
        int totalCharCount = 0;

        for (char c : text.toCharArray()) {  //将字符串转换为字符数组
            if (Character.isLetter(c)) {  //判断字符是否为字母
                totalCharCount++;
                // 中文字符的Unicode范围
                if (c >= 0x4E00 && c <= 0x9FFF) {
                    chineseCharCount++;
                }
            }
        }

        // 如果中文字符占比超过30%，认为是中文文本
        return totalCharCount > 0 && (chineseCharCount * 100 / totalCharCount) > 30;
    }

    //Future：异步执行结果
    public Future<String> translate(String text, String fromLang, String toLang) {
        //提交任务到线程池执行，并返回一个Future对象用于追踪任务执行状态（在后台线程中执行）
        return executorService.submit(() -> {  
            Log.d("TranslationRepo", " 开始翻译: " + text + " 从 " + fromLang + " 到 " + toLang);

            // 1. 生成缓存Key：由文本、源语言和目标语言组成，确保唯一性
            String cacheKey = generateCacheKey(text, fromLang, toLang);
            Log.d("TranslationRepo", "缓存Key: " + cacheKey);

            // 2. 检查缓存（添加空值检查）
            if (cacheDao != null) {
                //检查缓存DAO是否启用
                TranslationCache cache = cacheDao.getCache(cacheKey);
                
                Log.d("TranslationRepo", "缓存查询结果: " + (cache != null ? "命中" : "未命中"));
                
                if (cache != null && !isCacheExpired(cache.getTimestamp())) {  //缓存存在且没过期
                    Log.d("TranslationRepo", " 使用缓存结果: " + cache.getResult());

                    return cache.getResult();  //如果缓存命中，直接返回缓存结果，避免重复API调用
                }
            } else {
                Log.d("TranslationRepo", " 缓存功能已禁用，直接调用API");
            }

            Log.d("TranslationRepo", " 缓存未命中或已过期，调用API");

            // 3. 缓存未命中，调用API（带重试）
            int retryCount = 0;
            while (retryCount < MAX_RETRY_COUNT) {  //重试次数小于最大重试次数
                try {
                    Log.d("TranslationRepo", " 第 " + (retryCount + 1) + " 次重试调用API");

                    String salt = String.valueOf(System.currentTimeMillis());  //当前时间戳作为盐值
                    String sign = generateSign(text, salt);  //根据文本和盐值生成API签名，确保请求安全
                    Log.d("TranslationRepo", "生成签名: " + sign);

                    // 发起翻译请求
                    Call<TranslationResponse> call = translateService.translate(
                            text, fromLang, toLang, APP_ID, salt, sign
                    );
                    Log.d("TranslationRepo", " 发起网络请求...");

                    // Retrofit的同步执行翻译请求方法，返回Respose对象（在当前线程中执行，当前线程是后台线程）
                    //阻塞后台线程，等待网络响应
                    Response<TranslationResponse> response = call.execute(); 
                    Log.d("TranslationRepo", " 网络请求完成，状态码: " + response.code());

                    //响应处理
                    if (response.isSuccessful() && response.body() != null) { //如果http响应成功且有响应体
                        TranslationResponse body = response.body(); //来源 ：Retrofit 框架的 Response<T> 类，自动将json响应体解析为对应类型的对象（TranslationR）
                        Log.d("TranslationRepo", " 响应体: " + body.toString());

                        if (body.getError_code() == null && body.getTrans_result() != null
                                && !body.getTrans_result().isEmpty()) {  //检查响应体是否包含有效翻译结果
                            //future.get()-获取future异步任务的结果，返回翻译结果字符串，在调用线程中执行
                            String result = body.getTrans_result().get(0).getDst();  //获取翻译列表中第一个元素的dst字段即目标语言文本
                            Log.d("TranslationRepo", " API翻译成功: " + result);

                            // 存入缓存：将翻译结果保存到本地数据库中，以便下次翻译相同内容时直接使用缓存结果，无需再次调用 API。
                            if (cacheDao != null) { //如果缓存功能没被禁用
                                //创建缓存对象，最后一个参数是当前时间戳（ms），然后insertC插入缓存
                                cacheDao.insertCache(new TranslationCache(
                                        cacheKey, result, System.currentTimeMillis()
                                ));
                                Log.d("TranslationRepo", " 结果已缓存");
                            }
                            return result;
                        } else {
                            Log.e("TranslationRepo", " API返回错误: " + body.getError_code() + " - " + body.getError_msg());
                        }
                    } else {
                        Log.e("TranslationRepo", " 网络请求失败: " + response.message());
                    }

                } catch (IOException e) {
                    Log.e("TranslationRepo", " 网络异常: " + e.getMessage());
                    retryCount++;
                    if (retryCount >= MAX_RETRY_COUNT) break;
                    
                    // 重试间隔1秒
                    try {
                        Thread.sleep(1000);  //让当前线程休眠 1000 毫秒，在重试机制中，用于避免频繁请求
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt(); //当捕获到 InterruptedException 时，最佳实践是恢复中断状态
                        break;
                        //- Java 的 InterruptedException 会清除中断状态
                        //- 为了保持中断状态的传播，需要手动恢复
                        //- 这是一个重要的线程安全实践
                    }
                } catch (Exception e) {
                    Log.e("TranslationRepo", " 未知异常: " + e.getMessage(), e);
                    break;
                }
            }

            Log.e("TranslationRepo", " 所有重试失败，返回null");
            return null;
        });
    }


    // 生成缓存Key
    private String generateCacheKey(String text, String fromLang, String toLang) {
        return text + "-" + fromLang + "-" + toLang;
    }

    // 检查缓存是否过期
    private boolean isCacheExpired(long timestamp) {
        long expireTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(CACHE_EXPIRE_DAYS);
        return timestamp < expireTime;
    }

    // 清理过期缓存
    private void cleanExpiredCache() {
        executorService.submit(() -> {
            long expireTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(CACHE_EXPIRE_DAYS);
            cacheDao.deleteExpiredCache(expireTime);
        });
    }

    // 生成百度翻译签名
    // 替换成独立的MD5加密实现，删除对User类的依赖
    private String generateSign(String text, String salt) {
        String input = APP_ID + text + salt + SECRET_KEY;
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            Log.e("TranslationRepo", "MD5加密失败: " + e.getMessage(), e);
            return "";
        }
    }
}
