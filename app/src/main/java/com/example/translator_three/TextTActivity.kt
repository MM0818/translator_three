package com.example.translator_three;

import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri
import android.os.Build
import android.os.Bundle;
import android.provider.Settings;  // 新增导入
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.ui.AppBarConfiguration;
import com.example.translator_three.accessibility.TranslationAccessibilityService
import com.example.translator_three.databinding.ActivityTexttBinding
import kotlinx.coroutines.launch

@SuppressLint("RestrictedApi")
class TextTActivity : AppCompatActivity() {
//    var fromLanguage = "auto" // 源语言
//    var toLanguage = "auto" // 目标语言
//    var myClipboard: ClipboardManager? = null // 复制文本
//
//    private lateinit var appBarConfiguration: AppBarConfiguration  //延迟定义
//    private lateinit var tvTranslation: TextView
//    private lateinit var edContent: EditText
//    private lateinit var iVClear: ImageView
//    private lateinit var result_lay: View
//    private lateinit var tv_result: TextView
//    private lateinit var iv_clear_tx: ImageView
//    private lateinit var iv_voice: ImageView
//    private lateinit var iv_copy_tx: ImageView
//    private lateinit var spLanguage: Spinner
//    // 新增：划词翻译图标
//    private lateinit var iv_word_translate: ImageView
    private val TAG = "TextTActivity"  //调试时筛选日志

    //注入ViewModel
    private lateinit var viewModel: TranslateViewModel
    //DataBinding
    private lateinit var binding:ActivityTexttBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //setContentView(R.layout.activity_textt)
        //改成DATa Binding的方式，就不用写findViewById了，初始化DataBinding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_textt)
        binding.lifecycleOwner = this  //让binding感知生命周期

//        // 初始化控件
//        tv_result = findViewById(R.id.tv_result)
//        tvTranslation = findViewById(R.id.tvTranslation)
//        edContent = findViewById(R.id.ed_content)
//        iVClear = findViewById(R.id.iv_clear_tx)
//        result_lay = findViewById(R.id.result_lay)
//        iv_clear_tx = findViewById(R.id.iv_clear_tx)
//        iv_voice = findViewById(R.id.iv_voice)
//        iv_copy_tx = findViewById(R.id.iv_copy_tx)
//        spLanguage = findViewById(R.id.sp_language)
//        // 新增：初始化划词翻译图标
//        iv_word_translate = findViewById(R.id.iv_word_translate)

        viewModel = ViewModelProvider(this)[TranslateViewModel::class.java]

        // 绑定 VM 到布局（真正 MVVM 双向绑定），可选，目前没用上
        binding.viewModel = viewModel

        initWordTranslateClick()  // 划词翻译
        initClickEvents()         // 所有按钮
        initEditTextListener()    // 输入框监听
        requestOverlayPermission()// 悬浮窗权限
        observeViewModel()        // 翻译结果
    }

    // ==============================
    // 监听翻译结果（ViewModel 驱动）
    // ==============================
    private fun observeViewModel() {
        // 1. 监听翻译结果
        lifecycleScope.launch {
            viewModel.translateFlow.collect { result ->
                result?.let {
                    binding.tvResult.text = it
                    binding.resultLay.visibility = View.VISIBLE
                    binding.tvTranslation.visibility = View.GONE
                } ?: run {
                    Toast.makeText(this@TextTActivity, "翻译失败，请稍后再试", Toast.LENGTH_SHORT).show()
                    binding.tvResult.text = ""
                    binding.resultLay.visibility = View.GONE
                    binding.tvTranslation.visibility = View.VISIBLE
                }
            }
        }

        // 2. 监听错误信息
        lifecycleScope.launch {
            viewModel.errorMsg.collect { msg ->
                msg?.let {
                    Toast.makeText(this@TextTActivity, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ==============================
    // 输入框监听（完全保留）
    // ==============================
    private fun initEditTextListener() {
        binding.edContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.ivClearTx.visibility = View.VISIBLE
                val content = binding.edContent.text.toString().trim()

                if (content.isEmpty()) {
                    binding.resultLay.visibility = View.GONE
                    binding.tvTranslation.visibility = View.VISIBLE
                    binding.ivClearTx.visibility = View.GONE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // ==============================
    // 所有按钮点击（1:1 还原）
    // ==============================
    private fun initClickEvents() {
        // 翻译按钮
        binding.tvTranslation.setOnClickListener {
            startTranslation()
        }
        // 语音跳转
        binding.ivVoice.setOnClickListener {
            startActivity(Intent(this, VoiceTActivity::class.java))
        }
        // 清空
        binding.ivClearTx.setOnClickListener {
            binding.edContent.text.clear()
        }
        // 复制
        binding.ivCopyTx.setOnClickListener {
            val result = binding.tvResult.text.toString()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("text", result))
            Toast.makeText(this, "已复制", Toast.LENGTH_SHORT).show()
        }
    }

    // ==============================
    // 划词翻译点击（你原来的逻辑）
    // ==============================
    private fun initWordTranslateClick() {
        binding.ivWordTranslate.setOnClickListener {
            Log.d("AccessibilityCheck", "===== 点击实时翻译按钮 =====")
            val isEnabled = isAccessibilityServiceEnabled()

            if (isEnabled) {
                Toast.makeText(this, "划词翻译已就绪！长按选中文本即可翻译", Toast.LENGTH_LONG).show()
            } else {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                Toast.makeText(this, "请开启「Translator Three」的无障碍服务", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ==============================
    // 翻译逻辑（你原来的逻辑，只接入 VM）
    // ==============================
    private fun startTranslation() {
        val word = binding.edContent.text.toString().trim()
        if (word.isEmpty()) {
            Toast.makeText(this, "请输入要翻译的内容", Toast.LENGTH_SHORT).show()
            return
        }

        val selected = binding.spLanguage.selectedItem.toString()
        val (from, to) = when (selected) {
            "中文 -> 英文" -> "zh" to "en"
            "英文 -> 中文" -> "en" to "zh"
            "中文 -> 日语" -> "zh" to "jp"
            "日语 -> 中文" -> "jp" to "zh"
            "中文 -> 韩文" -> "zh" to "kor"
            "韩文 -> 中文" -> "kor" to "zh"
            else -> "zh" to "en"
        }

        // 只改这一句：接入 ViewModel
        viewModel.translateText(word, from, to)
    }

    // ==============================
    // 无障碍检测（你原封不动）
    // ==============================
    private fun isAccessibilityServiceEnabled(): Boolean {
        val serviceName = TranslationAccessibilityService::class.java.name
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val list = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

        for (info in list) {
            if (info.id.contains(serviceName)) return true
        }
        return false
    }

    // ==============================
    // 悬浮窗权限（你原封不动）
    // ==============================
    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }
    }


}
