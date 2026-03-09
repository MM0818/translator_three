package com.example.translator_three

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat
import com.example.translator_three.repository.TranslationRepository
import com.example.translator_three.utils.VoiceRecognizerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("RestrictedApi")  //使用安卓开发的特殊注解抑制Lnit工具警告，括号中是指定要忽略的是使用了受限API的警告
class VoiceTActivity : ComponentActivity() {
    private val TAG = "VoiceTActivity"
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    //工具类/仓库实例
    private lateinit var voiceManager:VoiceRecognizerManager
    private lateinit var translationRepo:TranslationRepository

    //初始化UI控件变量
    private lateinit var tvRecognizeStatus:TextView
    private lateinit var tvRecognizeResult:TextView
    private lateinit var tvTranslateResult: TextView
    private lateinit var volumeBar:ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voicet)

        //初始化UI
        initView()

        //初始化翻译仓库
        translationRepo=TranslationRepository(this)

        //初始化语音管理器
        voiceManager=VoiceRecognizerManager(this)

        // 请求录音权限
        requestAudioPermission()

        //绑定按钮事件
        bindButtonClick()

    }

    private fun initView(){
        tvRecognizeResult=findViewById(R.id.tvRecognizeResult)
        tvRecognizeStatus=findViewById(R.id.tvRecognizeStatus)
        tvTranslateResult=findViewById(R.id.tvTranslateResult)
        volumeBar=findViewById(R.id.volumeBar)
    }

    private fun bindButtonClick(){
        //开始识别
        findViewById<Button>(R.id.btnStartListening).setOnClickListener(){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
                voiceManager.startRecognize()
                tvRecognizeStatus.text="正在听，请说中文或英文"
            }else{
                tvRecognizeStatus.text="请先授予录音权限"
                requestAudioPermission()
            }
        }

        //停止识别
        findViewById<Button>(R.id.btnStopListening).setOnClickListener {
            voiceManager.stopRecognize()
            tvRecognizeStatus.text="识别停止"
        }
    }

    //检查应用是否具有录音权限，如果没有则请求用户授权；如果已经具有录音权限，则会调用语音识别初始化函数。
    //像ContextCompat、ActivityCompat、PackageManager这些都是来自AndroidX库和Android框架的类，是标准API，用于处理权限、上下文操作等功能。
    //前两个是- AndroidX 库的一部分，提供了 向下兼容 的实现，例如，权限检查在不同 Android 版本的实现可能不同，这些类会根据设备的 Android 版本自动选择合适的实现方式
    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { //如果未授予该权限则进入请求流程
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)  //（当前页面实例，权限数组，请求码用于回调处理）
        } else {
            // 如果已经获得权限，可以调用语音识别
            initVoiceRecognizer()
        }
    }


    //初始化语音识别（带回调）
    private fun initVoiceRecognizer() {

        voiceManager.init(object :VoiceRecognizerManager.OnVoiceResultListener{
            override fun onRecognizeSuccess(text: String) {
                // 识别成功：更新UI + 调用翻译仓库翻译
                runOnUiThread {
                    tvRecognizeResult.text = text
                }
                translateText(text)
            }

            override fun onRecognizeError(errorMsg: String) {
                runOnUiThread {
                    tvRecognizeStatus.text = errorMsg
                    tvTranslateResult.text = "识别失败：$errorMsg"
                }
            }

            override fun onVolumeChanged(volume: Int) {
                // 更新音量进度条
                runOnUiThread {
                    volumeBar.progress = volume
                }
            }

            override fun onSpeechStart() {
                runOnUiThread {
                    tvRecognizeStatus.text = "正在说话..."
                }
            }

            override fun onSpeechEnd() {
                runOnUiThread {
                    tvRecognizeStatus.text = "识别中..."
                }
            }
        })
    }


    //翻译方法
    private fun translateText(text:String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                tvTranslateResult.text="翻译中..."
                //调用仓库的自动翻译方法（自动判断中->英 / 英->中）
                val result=translationRepo.translate(text)
                tvTranslateResult.text=if(result!=null) "译义：$result" else "翻译失败"
            }catch (e:Exception){
                Log.e(TAG,"翻译异常",e)
                tvTranslateResult.text="翻译异常：$(e.message)"
            }
        }
    }

    // 权限请求的回调方法，是安卓系统自动调用的回调方法
    //1开发者调用请求权限方法->2系统显示权限请求对话框->3用户点击允许或者拒绝->4安卓系统自动调用该回调方法去处理用户的选择
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initVoiceRecognizer()
                tvRecognizeStatus.text = "权限已授予，可开始识别"
            } else {
                tvRecognizeStatus.text = "录音权限被拒绝，无法使用语音识别"
            }
        }
    }

    // 释放资源
    override fun onDestroy() {
        super.onDestroy()
        voiceManager.release()
        // 清理过期缓存（可选）
        CoroutineScope(Dispatchers.IO).launch {
            translationRepo.clearExpiredCache()
        }
    }


}


