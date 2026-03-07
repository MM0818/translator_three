package com.example.translator_three.api;

import com.example.translator_three.model.TranslationResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

//Retrofit API接口定义
public interface BaiduTranslateService {  //1、接口：Retrofit会自动生成实现类
    @FormUrlEncoded  //2、注解：请求体是表单格式（key=value&key=value）
    @POST("translate")  //3、注解：POST 请求，向服务器发送数据以创建新资源，路径是 translate（是给服务器看的，决定请求发到哪）
    Call<TranslationResponse> translate(  //4、返回值：Call代表一次请求，泛型是响应数据类（在Model文件夹），该方法名字不需要和路径一致噢
            @Field("q") String text,  //@Field：表单字段名=“q”，值=text参数
            @Field("from") String fromLang,
            @Field("to") String toLang,
            @Field("appid") String appId,
            @Field("salt") String salt,
            @Field("sign") String sign
    );
}

/*
    实际发出的HTTP请求：
    POST https://fanyi-api.baidu.com/api/trans/vip/translate HTTP/1.1
    Content-Type: application/x-www-form-urlencoded

    q=hello&from=en&to=zh&appid=xxx&salt=xxx&sign=xxx （@Field 就是帮你拼左边这段 body）

*/