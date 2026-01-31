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

public class TranslationRepository {
    // 把单线程池改成缓存线程池（临时解决阻塞问题）
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final BaiduTranslateService translateService;
    private final TranslationCacheDao cacheDao;
    //private final ExecutorService executorService;
    private static final int MAX_RETRY_COUNT = 3; // 最大重试次数
    private static final int CACHE_EXPIRE_DAYS = 7; // 缓存有效期7天
    private static final String APP_ID = "20240826002132755"; // 替换为你的APP_ID
    private static final String SECRET_KEY = "uUnQQAsgQKUFPPZL3HZH"; // 替换为你的密钥

    public TranslationRepository(Context context) {
        Log.d("TranslationRepo", "🔧 开始初始化 TranslationRepository");

        // 初始化Retrofit（预加载，避免首次调用耗时）
        try {
            // 初始化Retrofit（预加载，避免首次调用耗时）
            Log.d("TranslationRepo", "🔧 初始化 Retrofit...");
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://fanyi-api.baidu.com/api/trans/vip/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            this.translateService = retrofit.create(BaiduTranslateService.class);
            Log.d("TranslationRepo", "✅ Retrofit 初始化完成");

            // 注释数据库相关代码（临时关闭缓存）
            Log.d("TranslationRepo", "🔧 设置缓存DAO为null...");
            this.cacheDao = null; // 避免空指针
            Log.d("TranslationRepo", "✅ 缓存DAO设置完成");

            Log.d("TranslationRepo", "✅ 线程池初始化完成");

            Log.d("TranslationRepo", "🎉 TranslationRepository 初始化完成！");

        } catch (Exception e) {
            Log.e("TranslationRepo", "❌ TranslationRepository 初始化失败！", e);
            throw new RuntimeException("TranslationRepository 初始化失败", e);
        }
    }

//    // 翻译主方法（带缓存和重试），单参数的方法
//    public Future<String> translate(String text) {
//        return translate(text, "auto", "zh");
//    }

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
            return false;
        }

        int chineseCharCount = 0;
        int totalCharCount = 0;

        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
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

    // 替换成这个完整实现，覆盖你当前的 translate 方法
    public Future<String> translate(String text, String fromLang, String toLang) {
        return executorService.submit(() -> {
            Log.d("TranslationRepo", "🎯 开始翻译: " + text + " 从 " + fromLang + " 到 " + toLang);

            // 1. 生成缓存Key
            String cacheKey = generateCacheKey(text, fromLang, toLang);
            Log.d("TranslationRepo", "缓存Key: " + cacheKey);

            // 2. 检查缓存（添加空值检查）
            if (cacheDao != null) {
                TranslationCache cache = cacheDao.getCache(cacheKey);
                Log.d("TranslationRepo", "缓存查询结果: " + (cache != null ? "命中" : "未命中"));
                if (cache != null && !isCacheExpired(cache.getTimestamp())) {
                    Log.d("TranslationRepo", "✅ 使用缓存结果: " + cache.getResult());
                    return cache.getResult();
                }
            } else {
                Log.d("TranslationRepo", "⚠️ 缓存功能已禁用，直接调用API");
            }

            Log.d("TranslationRepo", "❌ 缓存未命中或已过期，调用API");

            // 3. 缓存未命中，调用API（带重试）
            int retryCount = 0;
            while (retryCount < MAX_RETRY_COUNT) {
                try {
                    Log.d("TranslationRepo", "🔄 第 " + (retryCount + 1) + " 次重试调用API");

                    String salt = String.valueOf(System.currentTimeMillis());
                    String sign = generateSign(text, salt);
                    Log.d("TranslationRepo", "生成签名: " + sign);

                    Call<TranslationResponse> call = translateService.translate(
                            text, fromLang, toLang, APP_ID, salt, sign
                    );
                    Log.d("TranslationRepo", "📡 发起网络请求...");
                    Response<TranslationResponse> response = call.execute();
                    Log.d("TranslationRepo", "📡 网络请求完成，状态码: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        TranslationResponse body = response.body();
                        Log.d("TranslationRepo", "📦 响应体: " + body.toString());

                        if (body.getError_code() == null && body.getTrans_result() != null
                                && !body.getTrans_result().isEmpty()) {
                            String result = body.getTrans_result().get(0).getDst();
                            Log.d("TranslationRepo", "✅ API翻译成功: " + result);

                            // 存入缓存（添加空值检查）
                            if (cacheDao != null) {
                                cacheDao.insertCache(new TranslationCache(
                                        cacheKey, result, System.currentTimeMillis()
                                ));
                                Log.d("TranslationRepo", "✅ 结果已缓存");
                            }
                            return result;
                        } else {
                            Log.e("TranslationRepo", "❌ API返回错误: " + body.getError_code() + " - " + body.getError_msg());
                        }
                    } else {
                        Log.e("TranslationRepo", "❌ 网络请求失败: " + response.message());
                    }

                } catch (IOException e) {
                    Log.e("TranslationRepo", "❌ 网络异常: " + e.getMessage());
                    retryCount++;
                    if (retryCount >= MAX_RETRY_COUNT) break;
                    // 重试间隔1秒
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } catch (Exception e) {
                    Log.e("TranslationRepo", "❌ 未知异常: " + e.getMessage(), e);
                    break;
                }
            }

            Log.e("TranslationRepo", "❌ 所有重试失败，返回null");
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
