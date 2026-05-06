package com.example.translator_three

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.translator_three.databinding.ActivityVoicetBinding
import com.example.translator_three.utils.VoiceRecognizerManager
import kotlinx.coroutines.launch

@SuppressLint("RestrictedApi")  //使用安卓开发的特殊注解抑制Lnit工具警告，括号中是指定要忽略的是使用了受限API的警告
class VoiceTActivity : AppCompatActivity() {
    private val TAG = "VoiceTActivity"
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    //工具类/仓库实例
    private lateinit var voiceManager:VoiceRecognizerManager

//    //初始化UI控件变量
//    private lateinit var tvRecognizeStatus:TextView
//    private lateinit var tvRecognizeResult:TextView
//    private lateinit var tvTranslateResult: TextView
//    private lateinit var volumeBar:ProgressBar

    private lateinit var viewModel: TranslateViewModel
    private lateinit var binding: ActivityVoicetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_voicet)
//
//        //初始化UI
//        initView()
//
//        //初始化语音管理器
//        voiceManager=VoiceRecognizerManager(this)
//
//        viewModel = TranslateViewModel(application)
//
//        // 请求录音权限
//        requestAudioPermission()
//
//        //绑定按钮事件
//        bindButtonClick()

        // DataBinding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_voicet)
        binding.lifecycleOwner = this

        // ViewModel（现在不报错了）
        viewModel = ViewModelProvider(this)[TranslateViewModel::class.java]

        // 初始化
        voiceManager = VoiceRecognizerManager(this)

        // 权限检查 + 语音初始化
        requestAudioPermission()

        // 按钮 + 观察数据
        bindButtons()
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.translateFlow.collect { result ->
                binding.tvTranslateResult.text = if (result != null) "译义：$result" else "翻译失败"
            }
        }
    }

    private fun bindButtons() {
        binding.btnStartListening.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                voiceManager.startRecognize()
                binding.tvRecognizeStatus.text = "正在听，请说中文或英文"
            } else {
                requestAudioPermission()
            }
        }

        binding.btnStopListening.setOnClickListener {
            voiceManager.stopRecognize()
            binding.tvRecognizeStatus.text = "识别停止"
        }
    }

    // 检查权限代码（必须有！）
    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        } else {
            initVoiceRecognizer()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initVoiceRecognizer()
                binding.tvRecognizeStatus.text = "权限已授予，可开始识别"
            } else {
                binding.tvRecognizeStatus.text = "录音权限被拒绝，无法使用语音识别"
            }
        }
    }

    // ====================== 语音识别逻辑 ======================
    private fun initVoiceRecognizer() {
        voiceManager.init(object : VoiceRecognizerManager.OnVoiceResultListener {
            override fun onRecognizeSuccess(text: String) {
                runOnUiThread {
                    binding.tvRecognizeResult.text = text
                }
                viewModel.translateVoice(text)
            }

            override fun onRecognizeError(errorMsg: String) {
                runOnUiThread {
                    binding.tvRecognizeStatus.text = errorMsg
                }
            }

            override fun onVolumeChanged(volume: Int) {
                runOnUiThread {
                    binding.volumeBar.progress = volume
                }
            }

            override fun onSpeechStart() {
                runOnUiThread {
                    binding.tvRecognizeStatus.text = "正在说话..."
                }
            }

            override fun onSpeechEnd() {
                runOnUiThread {
                    binding.tvRecognizeStatus.text = "识别中..."
                }
            }
        })
    }

    //释放资源
    override fun onDestroy() {
        super.onDestroy()
        voiceManager.release()
    }

}


