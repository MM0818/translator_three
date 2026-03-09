package com.example.translator_three;

import java.lang.System;

@android.annotation.SuppressLint(value = {"RestrictedApi"})
@kotlin.Metadata(mv = {1, 8, 0}, k = 1, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0011\n\u0000\n\u0002\u0010\u0015\n\u0002\b\t\b\u0007\u0018\u00002\u00020\u0001:\u0004&\'()B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0017\u001a\u00020\u0018H\u0002J\u0012\u0010\u0019\u001a\u00020\u00182\b\u0010\u001a\u001a\u0004\u0018\u00010\u001bH\u0014J-\u0010\u001c\u001a\u00020\u00182\u0006\u0010\u001d\u001a\u00020\u00042\u000e\u0010\u001e\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00060\u001f2\u0006\u0010 \u001a\u00020!H\u0016\u00a2\u0006\u0002\u0010\"J\b\u0010#\u001a\u00020\u0018H\u0002J\u0010\u0010$\u001a\u00020\u00182\u0006\u0010%\u001a\u00020\u0006H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\r\u001a\u00020\u000e8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0011\u0010\u0012\u001a\u0004\b\u000f\u0010\u0010R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0016X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006*"}, d2 = {"Lcom/example/translator_three/VoiceTActivity;", "Landroidx/core/app/ComponentActivity;", "()V", "REQUEST_RECORD_AUDIO_PERMISSION", "", "TAG", "", "baiduTranslateService", "Lcom/example/translator_three/VoiceTActivity$BaiduTranslateService;", "initListener", "Lcom/iflytek/cloud/InitListener;", "isRecognizing", "", "recognizerListener", "Lcom/iflytek/cloud/RecognizerListener;", "getRecognizerListener", "()Lcom/iflytek/cloud/RecognizerListener;", "recognizerListener$delegate", "Lkotlin/Lazy;", "speechRecognizer", "Lcom/iflytek/cloud/SpeechRecognizer;", "tvTranslateResult", "Landroid/widget/TextView;", "initVoiceRecognize", "", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onRequestPermissionsResult", "requestCode", "permissions", "", "grantResults", "", "(I[Ljava/lang/String;[I)V", "requestAudioPermission", "translate", "text", "BaiduTranslateService", "MD5Utils", "RespondBean", "TransResultBean", "app_debug"})
public final class VoiceTActivity extends androidx.core.app.ComponentActivity {
    private com.example.translator_three.VoiceTActivity.BaiduTranslateService baiduTranslateService;
    private android.widget.TextView tvTranslateResult;
    private com.iflytek.cloud.SpeechRecognizer speechRecognizer;
    private boolean isRecognizing = false;
    private final java.lang.String TAG = "MainActivity";
    private final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private final com.iflytek.cloud.InitListener initListener = null;
    private final kotlin.Lazy recognizerListener$delegate = null;
    
    public VoiceTActivity() {
        super();
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    private final void requestAudioPermission() {
    }
    
    private final void initVoiceRecognize() {
    }
    
    @java.lang.Override
    public void onRequestPermissionsResult(int requestCode, @org.jetbrains.annotations.NotNull
    java.lang.String[] permissions, @org.jetbrains.annotations.NotNull
    int[] grantResults) {
    }
    
    private final void translate(java.lang.String text) {
    }
    
    private final com.iflytek.cloud.RecognizerListener getRecognizerListener() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 8, 0}, k = 1, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\bf\u0018\u00002\u00020\u0001JJ\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u00062\b\b\u0001\u0010\u0007\u001a\u00020\u00062\b\b\u0001\u0010\b\u001a\u00020\u00062\b\b\u0001\u0010\t\u001a\u00020\u00062\b\b\u0001\u0010\n\u001a\u00020\u00062\b\b\u0001\u0010\u000b\u001a\u00020\u0006H\'\u00a8\u0006\f"}, d2 = {"Lcom/example/translator_three/VoiceTActivity$BaiduTranslateService;", "", "translate", "Lretrofit2/Call;", "Lcom/example/translator_three/VoiceTActivity$RespondBean;", "q", "", "from", "to", "appid", "salt", "sign", "app_debug"})
    public static abstract interface BaiduTranslateService {
        
        @org.jetbrains.annotations.NotNull
        @retrofit2.http.POST(value = "translate")
        @retrofit2.http.FormUrlEncoded
        public abstract retrofit2.Call<com.example.translator_three.VoiceTActivity.RespondBean> translate(@org.jetbrains.annotations.NotNull
        @retrofit2.http.Field(value = "q")
        java.lang.String q, @org.jetbrains.annotations.NotNull
        @retrofit2.http.Field(value = "from")
        java.lang.String from, @org.jetbrains.annotations.NotNull
        @retrofit2.http.Field(value = "to")
        java.lang.String to, @org.jetbrains.annotations.NotNull
        @retrofit2.http.Field(value = "appid")
        java.lang.String appid, @org.jetbrains.annotations.NotNull
        @retrofit2.http.Field(value = "salt")
        java.lang.String salt, @org.jetbrains.annotations.NotNull
        @retrofit2.http.Field(value = "sign")
        java.lang.String sign);
    }
    
    @kotlin.Metadata(mv = {1, 8, 0}, k = 1, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B#\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\u0002\u0010\bJ\t\u0010\u000e\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000f\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006H\u00c6\u0003J-\u0010\u0011\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\u000e\b\u0002\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006H\u00c6\u0001J\u0013\u0010\u0012\u001a\u00020\u00132\b\u0010\u0014\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0015\u001a\u00020\u0016H\u00d6\u0001J\t\u0010\u0017\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\nR\u0017\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r\u00a8\u0006\u0018"}, d2 = {"Lcom/example/translator_three/VoiceTActivity$RespondBean;", "", "from", "", "to", "trans_result", "", "Lcom/example/translator_three/VoiceTActivity$TransResultBean;", "(Ljava/lang/String;Ljava/lang/String;Ljava/util/List;)V", "getFrom", "()Ljava/lang/String;", "getTo", "getTrans_result", "()Ljava/util/List;", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    public static final class RespondBean {
        @org.jetbrains.annotations.NotNull
        private final java.lang.String from = null;
        @org.jetbrains.annotations.NotNull
        private final java.lang.String to = null;
        @org.jetbrains.annotations.NotNull
        private final java.util.List<com.example.translator_three.VoiceTActivity.TransResultBean> trans_result = null;
        
        @org.jetbrains.annotations.NotNull
        public final com.example.translator_three.VoiceTActivity.RespondBean copy(@org.jetbrains.annotations.NotNull
        java.lang.String from, @org.jetbrains.annotations.NotNull
        java.lang.String to, @org.jetbrains.annotations.NotNull
        java.util.List<com.example.translator_three.VoiceTActivity.TransResultBean> trans_result) {
            return null;
        }
        
        @java.lang.Override
        public boolean equals(@org.jetbrains.annotations.Nullable
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override
        public int hashCode() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull
        @java.lang.Override
        public java.lang.String toString() {
            return null;
        }
        
        public RespondBean(@org.jetbrains.annotations.NotNull
        java.lang.String from, @org.jetbrains.annotations.NotNull
        java.lang.String to, @org.jetbrains.annotations.NotNull
        java.util.List<com.example.translator_three.VoiceTActivity.TransResultBean> trans_result) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getFrom() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getTo() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.util.List<com.example.translator_three.VoiceTActivity.TransResultBean> component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.util.List<com.example.translator_three.VoiceTActivity.TransResultBean> getTrans_result() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 8, 0}, k = 1, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0087\b\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0005J\t\u0010\t\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\n\u001a\u00020\u0003H\u00c6\u0003J\u001d\u0010\u000b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\f\u001a\u00020\r2\b\u0010\u000e\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001J\t\u0010\u0011\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0007\u00a8\u0006\u0012"}, d2 = {"Lcom/example/translator_three/VoiceTActivity$TransResultBean;", "", "src", "", "dst", "(Ljava/lang/String;Ljava/lang/String;)V", "getDst", "()Ljava/lang/String;", "getSrc", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    public static final class TransResultBean {
        @org.jetbrains.annotations.NotNull
        private final java.lang.String src = null;
        @org.jetbrains.annotations.NotNull
        private final java.lang.String dst = null;
        
        @org.jetbrains.annotations.NotNull
        public final com.example.translator_three.VoiceTActivity.TransResultBean copy(@org.jetbrains.annotations.NotNull
        java.lang.String src, @org.jetbrains.annotations.NotNull
        java.lang.String dst) {
            return null;
        }
        
        @java.lang.Override
        public boolean equals(@org.jetbrains.annotations.Nullable
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override
        public int hashCode() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull
        @java.lang.Override
        public java.lang.String toString() {
            return null;
        }
        
        public TransResultBean(@org.jetbrains.annotations.NotNull
        java.lang.String src, @org.jetbrains.annotations.NotNull
        java.lang.String dst) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getSrc() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getDst() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 8, 0}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u00c7\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0004\u00a8\u0006\u0006"}, d2 = {"Lcom/example/translator_three/VoiceTActivity$MD5Utils;", "", "()V", "md5", "", "input", "app_debug"})
    public static final class MD5Utils {
        @org.jetbrains.annotations.NotNull
        public static final com.example.translator_three.VoiceTActivity.MD5Utils INSTANCE = null;
        
        private MD5Utils() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String md5(@org.jetbrains.annotations.NotNull
        java.lang.String input) {
            return null;
        }
    }
}