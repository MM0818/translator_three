package com.example.translator_three.api;

import com.example.translator_three.model.TranslationResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface BaiduTranslateService {
    @FormUrlEncoded
    @POST("translate")
    Call<TranslationResponse> translate(
            @Field("q") String text,
            @Field("from") String fromLang,
            @Field("to") String toLang,
            @Field("appid") String appId,
            @Field("salt") String salt,
            @Field("sign") String sign
    );
}