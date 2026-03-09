package com.example.translator_three;

import java.lang.System;

@android.annotation.SuppressLint(value = {"RestrictedApi"})
@kotlin.Metadata(mv = {1, 8, 0}, k = 1, d1 = {"\u0000b\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u0007\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010&\u001a\u00020\'H\u0002J\b\u0010(\u001a\u00020)H\u0002J\b\u0010*\u001a\u00020\'H\u0002J\u0012\u0010+\u001a\u00020\'2\b\u0010,\u001a\u0004\u0018\u00010-H\u0014J\u0010\u0010.\u001a\u00020\'2\u0006\u0010/\u001a\u00020\u0004H\u0002J\b\u00100\u001a\u00020\'H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u001a\u0010\t\u001a\u00020\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\n\u0010\u000b\"\u0004\b\f\u0010\rR\u000e\u0010\u000e\u001a\u00020\u000fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u000fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u000fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u000fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u000fX\u0082.\u00a2\u0006\u0002\n\u0000R\u001c\u0010\u0014\u001a\u0004\u0018\u00010\u0015X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0016\u0010\u0017\"\u0004\b\u0018\u0010\u0019R\u000e\u0010\u001a\u001a\u00020\u001bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\u001dX\u0082.\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u001e\u001a\u00020\u0004X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010\u000b\"\u0004\b \u0010\rR\u000e\u0010!\u001a\u00020\"X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010#\u001a\u00020$X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010%\u001a\u00020$X\u0082.\u00a2\u0006\u0002\n\u0000\u00a8\u00061"}, d2 = {"Lcom/example/translator_three/TextTActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "TAG", "", "appBarConfiguration", "Landroidx/navigation/ui/AppBarConfiguration;", "edContent", "Landroid/widget/EditText;", "fromLanguage", "getFromLanguage", "()Ljava/lang/String;", "setFromLanguage", "(Ljava/lang/String;)V", "iVClear", "Landroid/widget/ImageView;", "iv_clear_tx", "iv_copy_tx", "iv_voice", "iv_word_translate", "myClipboard", "Landroid/content/ClipboardManager;", "getMyClipboard", "()Landroid/content/ClipboardManager;", "setMyClipboard", "(Landroid/content/ClipboardManager;)V", "result_lay", "Landroid/view/View;", "spLanguage", "Landroid/widget/Spinner;", "toLanguage", "getToLanguage", "setToLanguage", "translationRepository", "Lcom/example/translator_three/repository/TranslationRepository;", "tvTranslation", "Landroid/widget/TextView;", "tv_result", "editTextListener", "", "isAccessibilityServiceEnabled", "", "onClick", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "showMsg", "msg", "transition", "app_debug"})
public final class TextTActivity extends androidx.appcompat.app.AppCompatActivity {
    @org.jetbrains.annotations.NotNull
    private java.lang.String fromLanguage = "auto";
    @org.jetbrains.annotations.NotNull
    private java.lang.String toLanguage = "auto";
    @org.jetbrains.annotations.Nullable
    private android.content.ClipboardManager myClipboard;
    private androidx.navigation.ui.AppBarConfiguration appBarConfiguration;
    private android.widget.TextView tvTranslation;
    private android.widget.EditText edContent;
    private android.widget.ImageView iVClear;
    private android.view.View result_lay;
    private android.widget.TextView tv_result;
    private android.widget.ImageView iv_clear_tx;
    private android.widget.ImageView iv_voice;
    private final java.lang.String TAG = "TextTActivity";
    private android.widget.ImageView iv_copy_tx;
    private android.widget.Spinner spLanguage;
    private android.widget.ImageView iv_word_translate;
    private com.example.translator_three.repository.TranslationRepository translationRepository;
    
    public TextTActivity() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getFromLanguage() {
        return null;
    }
    
    public final void setFromLanguage(@org.jetbrains.annotations.NotNull
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getToLanguage() {
        return null;
    }
    
    public final void setToLanguage(@org.jetbrains.annotations.NotNull
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final android.content.ClipboardManager getMyClipboard() {
        return null;
    }
    
    public final void setMyClipboard(@org.jetbrains.annotations.Nullable
    android.content.ClipboardManager p0) {
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    /**
     * 输入框监听
     */
    private final void editTextListener() {
    }
    
    /**
     * 点击事件处理
     */
    private final void onClick() {
    }
    
    /**
     * Toast提示
     * @param msg 提示内容
     */
    private final void showMsg(java.lang.String msg) {
    }
    
    private final void transition() {
    }
    
    private final boolean isAccessibilityServiceEnabled() {
        return false;
    }
}