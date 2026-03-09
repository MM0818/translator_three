package com.example.translator_three.utils

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.util.Log
import com.iflytek.cloud.*
import java.util.Locale

/**
 * 讯飞语音识别管理器（解耦Activity，专注语音识别逻辑）
 */
class VoiceRecognizerManager(private val context: Context) {
    private val TAG = "VoiceRecognizerManager"
    private var speechRecognizer: SpeechRecognizer? = null
    private var isRecognizing = false

    // 识别结果回调
    interface OnVoiceResultListener {
        fun onRecognizeSuccess(text: String) // 识别成功
        fun onRecognizeError(errorMsg: String) // 识别失败
        fun onVolumeChanged(volume: Int) // 音量变化
        fun onSpeechStart() // 开始说话
        fun onSpeechEnd() // 结束说话
    }

    private var resultListener: OnVoiceResultListener? = null

    // 初始化讯飞语音（需先确保有录音权限）
    fun init(listener: OnVoiceResultListener) {
        this.resultListener = listener
        // 初始化讯飞SDK
        SpeechUtility.createUtility(context, SpeechConstant.APPID + "=1d295360")
        // 创建识别实例
        speechRecognizer = SpeechRecognizer.createRecognizer(context, initListener).also { recognizer ->
            recognizer?.apply {
                // 通用参数配置
                setParameter(SpeechConstant.RESULT_TYPE, "plain")
                setParameter(SpeechConstant.VAD_BOS, "4000") // 前端点静音超时
                setParameter(SpeechConstant.VAD_EOS, "1000") // 后端点静音超时
                // 自动适配系统语言（识别中文/英文）
                setRecognizeLanguage()
            } ?: run {
                listener.onRecognizeError("语音识别实例创建失败")
            }
        }
    }

    // 开始识别
    fun startRecognize() {
        if (isRecognizing || speechRecognizer == null) return
        isRecognizing = true
        speechRecognizer?.startListening(recognizerListener)
        resultListener?.onSpeechStart()
    }

    // 停止识别
    fun stopRecognize() {
        if (!isRecognizing || speechRecognizer == null) return
        isRecognizing = false
        speechRecognizer?.stopListening()
        resultListener?.onSpeechEnd()
    }

    // 释放资源
    fun release() {
        stopRecognize()
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        resultListener = null
    }

    // 自动设置识别语言（适配系统语言）
    private fun SpeechRecognizer.setRecognizeLanguage() {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList.getDefault()[0]
        } else {
            Locale.getDefault()
        }
        val language = "${locale.language}-${locale.country}"
        Log.d(TAG, "系统语言：$language")
        // 中文系统识别中文，其他识别英文
        val recognizeLang = if (language.equals("zh-CN", ignoreCase = true)) "zh_cn" else "en_us"
        setParameter(SpeechConstant.LANGUAGE, recognizeLang)
    }

    // 讯飞初始化监听器
    private val initListener = InitListener { code ->
        if (code != ErrorCode.SUCCESS) {
            resultListener?.onRecognizeError("语音初始化失败，错误码：$code")
            Log.e(TAG, "初始化失败：$code")
        } else {
            Log.d(TAG, "语音初始化成功")
        }
    }

    // 讯飞识别监听器
    private val recognizerListener = object : RecognizerListener {
        override fun onVolumeChanged(volume: Int, bytes: ByteArray?) {
            resultListener?.onVolumeChanged(volume)
        }

        override fun onBeginOfSpeech() {
            Log.d(TAG, "开始说话")
        }

        override fun onEndOfSpeech() {
            Log.d(TAG, "结束说话")
            isRecognizing = false
            resultListener?.onSpeechEnd()
        }

        override fun onResult(result: RecognizerResult?, isLast: Boolean) {
            result ?: run {
                resultListener?.onRecognizeError("识别结果为空")
                return
            }
            val text = result.resultString.trim()
            if (text.isNotEmpty()) {
                resultListener?.onRecognizeSuccess(text)
                Log.d(TAG, "识别结果：$text")
            }
        }

        override fun onError(error: SpeechError) {
            isRecognizing = false
            val errorMsg = "识别错误：${error.errorDescription}"
            resultListener?.onRecognizeError(errorMsg)
            Log.e(TAG, errorMsg)
        }

        override fun onEvent(eventType: Int, arg1: Int, arg2: Int, bundle: Bundle?) {}
    }
}