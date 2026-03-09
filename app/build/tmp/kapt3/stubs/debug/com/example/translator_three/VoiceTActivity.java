package com.example.translator_three;

import java.lang.System;

@android.annotation.SuppressLint(value = {"RestrictedApi"})
@kotlin.Metadata(mv = {1, 8, 0}, k = 1, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0011\n\u0000\n\u0002\u0010\u0015\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0011\u001a\u00020\u0012H\u0002J\b\u0010\u0013\u001a\u00020\u0012H\u0002J\b\u0010\u0014\u001a\u00020\u0012H\u0002J\u0012\u0010\u0015\u001a\u00020\u00122\b\u0010\u0016\u001a\u0004\u0018\u00010\u0017H\u0014J\b\u0010\u0018\u001a\u00020\u0012H\u0014J-\u0010\u0019\u001a\u00020\u00122\u0006\u0010\u001a\u001a\u00020\u00042\u000e\u0010\u001b\u001a\n\u0012\u0006\b\u0001\u0012\u00020\u00060\u001c2\u0006\u0010\u001d\u001a\u00020\u001eH\u0016\u00a2\u0006\u0002\u0010\u001fJ\b\u0010 \u001a\u00020\u0012H\u0002J\u0010\u0010!\u001a\u00020\u00122\u0006\u0010\"\u001a\u00020\u0006H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u0006#"}, d2 = {"Lcom/example/translator_three/VoiceTActivity;", "Landroidx/core/app/ComponentActivity;", "()V", "REQUEST_RECORD_AUDIO_PERMISSION", "", "TAG", "", "translationRepo", "Lcom/example/translator_three/repository/TranslationRepository;", "tvRecognizeResult", "Landroid/widget/TextView;", "tvRecognizeStatus", "tvTranslateResult", "voiceManager", "Lcom/example/translator_three/utils/VoiceRecognizerManager;", "volumeBar", "Landroid/widget/ProgressBar;", "bindButtonClick", "", "initView", "initVoiceRecognizer", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onRequestPermissionsResult", "requestCode", "permissions", "", "grantResults", "", "(I[Ljava/lang/String;[I)V", "requestAudioPermission", "translateText", "text", "app_debug"})
public final class VoiceTActivity extends androidx.core.app.ComponentActivity {
    private final java.lang.String TAG = "VoiceTActivity";
    private final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private com.example.translator_three.utils.VoiceRecognizerManager voiceManager;
    private com.example.translator_three.repository.TranslationRepository translationRepo;
    private android.widget.TextView tvRecognizeStatus;
    private android.widget.TextView tvRecognizeResult;
    private android.widget.TextView tvTranslateResult;
    private android.widget.ProgressBar volumeBar;
    
    public VoiceTActivity() {
        super();
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    private final void initView() {
    }
    
    private final void bindButtonClick() {
    }
    
    private final void requestAudioPermission() {
    }
    
    private final void initVoiceRecognizer() {
    }
    
    private final void translateText(java.lang.String text) {
    }
    
    @java.lang.Override
    public void onRequestPermissionsResult(int requestCode, @org.jetbrains.annotations.NotNull
    java.lang.String[] permissions, @org.jetbrains.annotations.NotNull
    int[] grantResults) {
    }
    
    @java.lang.Override
    protected void onDestroy() {
    }
}