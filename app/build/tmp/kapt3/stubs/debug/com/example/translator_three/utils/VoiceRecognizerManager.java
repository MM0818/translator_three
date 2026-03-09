package com.example.translator_three.utils;

import java.lang.System;

/**
 * 讯飞语音识别管理器（解耦Activity，专注语音识别逻辑）
 */
@kotlin.Metadata(mv = {1, 8, 0}, k = 1, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0007\b\u0007\u0018\u00002\u00020\u0001:\u0001\u0018B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u000eJ\u0006\u0010\u0014\u001a\u00020\u0012J\u0006\u0010\u0015\u001a\u00020\u0012J\u0006\u0010\u0016\u001a\u00020\u0012J\f\u0010\u0017\u001a\u00020\u0012*\u00020\u0010H\u0002R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"Lcom/example/translator_three/utils/VoiceRecognizerManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "TAG", "", "initListener", "Lcom/iflytek/cloud/InitListener;", "isRecognizing", "", "recognizerListener", "Lcom/iflytek/cloud/RecognizerListener;", "resultListener", "Lcom/example/translator_three/utils/VoiceRecognizerManager$OnVoiceResultListener;", "speechRecognizer", "Lcom/iflytek/cloud/SpeechRecognizer;", "init", "", "listener", "release", "startRecognize", "stopRecognize", "setRecognizeLanguage", "OnVoiceResultListener", "app_debug"})
public final class VoiceRecognizerManager {
    private final android.content.Context context = null;
    private final java.lang.String TAG = "VoiceRecognizerManager";
    private com.iflytek.cloud.SpeechRecognizer speechRecognizer;
    private boolean isRecognizing = false;
    private com.example.translator_three.utils.VoiceRecognizerManager.OnVoiceResultListener resultListener;
    private final com.iflytek.cloud.InitListener initListener = null;
    private final com.iflytek.cloud.RecognizerListener recognizerListener = null;
    
    public VoiceRecognizerManager(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super();
    }
    
    public final void init(@org.jetbrains.annotations.NotNull
    com.example.translator_three.utils.VoiceRecognizerManager.OnVoiceResultListener listener) {
    }
    
    public final void startRecognize() {
    }
    
    public final void stopRecognize() {
    }
    
    public final void release() {
    }
    
    private final void setRecognizeLanguage(com.iflytek.cloud.SpeechRecognizer $this$setRecognizeLanguage) {
    }
    
    @kotlin.Metadata(mv = {1, 8, 0}, k = 1, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\b\n\u0000\bf\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&J\u0010\u0010\u0006\u001a\u00020\u00032\u0006\u0010\u0007\u001a\u00020\u0005H&J\b\u0010\b\u001a\u00020\u0003H&J\b\u0010\t\u001a\u00020\u0003H&J\u0010\u0010\n\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\fH&\u00a8\u0006\r"}, d2 = {"Lcom/example/translator_three/utils/VoiceRecognizerManager$OnVoiceResultListener;", "", "onRecognizeError", "", "errorMsg", "", "onRecognizeSuccess", "text", "onSpeechEnd", "onSpeechStart", "onVolumeChanged", "volume", "", "app_debug"})
    public static abstract interface OnVoiceResultListener {
        
        public abstract void onRecognizeSuccess(@org.jetbrains.annotations.NotNull
        java.lang.String text);
        
        public abstract void onRecognizeError(@org.jetbrains.annotations.NotNull
        java.lang.String errorMsg);
        
        public abstract void onVolumeChanged(int volume);
        
        public abstract void onSpeechStart();
        
        public abstract void onSpeechEnd();
    }
}