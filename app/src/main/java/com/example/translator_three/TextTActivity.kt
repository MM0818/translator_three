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
import androidx.navigation.ui.AppBarConfiguration;
import com.example.translator_three.accessibility.TranslationAccessibilityService
import com.example.translator_three.repository.TranslationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("RestrictedApi")
class TextTActivity : AppCompatActivity() {
    var fromLanguage = "auto" // 源语言
    var toLanguage = "auto" // 目标语言
    var myClipboard: ClipboardManager? = null // 复制文本

    private lateinit var appBarConfiguration: AppBarConfiguration  //延迟定义
    private lateinit var tvTranslation: TextView
    private lateinit var edContent: EditText
    private lateinit var iVClear: ImageView
    private lateinit var result_lay: View
    private lateinit var tv_result: TextView
    private lateinit var iv_clear_tx: ImageView
    private lateinit var iv_voice: ImageView
    private val TAG = "TextTActivity"  //调试时筛选日志
    private lateinit var iv_copy_tx: ImageView
    private lateinit var spLanguage: Spinner
    // 新增：划词翻译图标
    private lateinit var iv_word_translate: ImageView

    //初始化翻译仓库
    private lateinit var translationRepository:TranslationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_textt)

        //初始化翻译仓库
        translationRepository=TranslationRepository(this)

        // 初始化控件
        tvTranslation = findViewById(R.id.tvTranslation)
        edContent = findViewById(R.id.ed_content)
        iVClear = findViewById(R.id.iv_clear_tx)
        result_lay = findViewById(R.id.result_lay)
        tv_result = findViewById(R.id.tv_result)
        iv_clear_tx = findViewById(R.id.iv_clear_tx)
        iv_voice = findViewById(R.id.iv_voice)
        iv_copy_tx = findViewById(R.id.iv_copy_tx)
        spLanguage = findViewById(R.id.sp_language)
        // 新增：初始化划词翻译图标
        iv_word_translate = findViewById(R.id.iv_word_translate)

        // 划词翻译图标点击事件 - 跳转到无障碍设置
        iv_word_translate.setOnClickListener {
            Log.d("AccessibilityCheck", "===== 点击实时翻译按钮 =====")

            val isEnabled = isAccessibilityServiceEnabled()

            Log.d(
                "AccessibilityCheck",
                "按钮点击后，服务检测结果：" + (if (isEnabled) "已开启" else "未开启")
            )

            if (isEnabled) {
                Toast.makeText(
                    this@TextTActivity,
                    "划词翻译已就绪！长按选中文本即可翻译",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // 未开启，跳设置页
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
                Toast.makeText(
                    this@TextTActivity,
                    "请开启「Translator Three」的无障碍服务",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        onClick()
        editTextListener()

        // 检查并请求悬浮窗权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {  //检查应用安卓版本是否大于等于M（安卓6.0），以及是否获取悬浮窗权限（返回值为true）
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"))
            startActivity(intent)  //启动系统的悬浮窗权限设置页面，让用户手动授权
        }
    }

    /**
     * 输入框监听
     */
    private fun editTextListener() {
        edContent.addTextChangedListener(object : TextWatcher {  //安卓提供的用于监听文本变化的接口，有三个回调方法都得实现一下
            override fun afterTextChanged(s: Editable?) {
                iVClear.visibility = View.VISIBLE

                val content = edContent.text.toString().trim()
                if (content.isEmpty()) {
                    result_lay.visibility = View.GONE
                    tvTranslation.visibility = View.VISIBLE
                    iVClear.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
    }

    /**
     * 点击事件处理
     */
    private fun onClick() {
        tvTranslation.setOnClickListener { // 文本翻译按钮
            transition()
        }

        iv_voice.setOnClickListener {
            val intent = Intent(this@TextTActivity, VoiceTActivity::class.java)
            startActivity(intent)
        }

        iv_clear_tx.setOnClickListener {// 清空输入框
            edContent.text.clear()
        }

        iv_copy_tx.setOnClickListener {// 复制文本
            val result = tv_result.text.toString()
            //1、获取剪贴板管理器，参数表示剪贴板服务的常量，返回值是ClipboardManager类的实例
            val myClipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager  //通过该方法获取系统剪切板服务
            //2、参数：剪贴板数据的标签+要复制的文本内容，即创建ClipData对象
            //3、通过管理器的方法将内容设置到剪贴板
            myClipboard.setPrimaryClip(ClipData.newPlainText("text", result))  //将创建的ClipData对象设置为剪切板的主要内容
            showMsg("已复制")
        }
    }

    /**
     * Toast提示
     * @param msg 提示内容
     */
    private fun showMsg(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun transition() {
        //1、参数准备阶段
        val word = edContent.text.toString().trim() // 需查询的单词，trim() 方法是 Kotlin/Java 中String类的一个常用方法，用于去除字符串前后的空白字符
        if (word.isEmpty()) {
            Toast.makeText(this, "请输入要翻译的内容", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedText = spLanguage.selectedItem?.toString() ?: "中文 -> 英文"  //用户没手动选的话默认中翻译英
        val (fromLang, toLang) = when (selectedText) {
            "中文 -> 英文" -> Pair("zh", "en")
            "英文 -> 中文" -> Pair("en", "zh")
            "中文 -> 日语" -> Pair("zh", "jp")
            "日语 -> 中文" -> Pair("jp", "zh")
            "中文 -> 韩文" -> Pair("zh", "kor")
            "韩文 -> 中文" -> Pair("kor", "zh")
            else -> Pair("zh", "en")
        }

        Log.d(TAG, "翻译方向: $fromLang -> $toLang")

        //通过协程调用仓库的翻译方法
        CoroutineScope(Dispatchers.Main).launch {
            //切换到IO线程执行翻译（仓库内部已用Dispatchers.IO，此处仅做线程切换）
            val transRes= withContext(Dispatchers.IO){
                translationRepository.translate(word,fromLang,toLang)
            }

            //处理翻译结果
            if(transRes!=null){
                tv_result.text=transRes
                result_lay.visibility=View.VISIBLE
                tvTranslation.visibility=View.GONE
            }else{
                Toast.makeText(this@TextTActivity,"翻译失败，请稍后再试",Toast.LENGTH_SHORT).show()
                tv_result.text=""
                result_lay.visibility=View.GONE
                tvTranslation.visibility=View.VISIBLE
            }
        }

    }


    // 新增：检测无障碍服务是否开启
    private fun isAccessibilityServiceEnabled(): Boolean {
        Log.d("AccessibilityCheck", "===== 点击实时翻译按钮 =====")

        //构建目标服务Id，是无障碍服务的唯一标识符
        // 直接使用系统标准格式：包名 + "/." + 相对类名
        val serviceClassName = TranslationAccessibilityService::class.java.simpleName
        val servicePackageName = "accessibility" // 或者从完整类名中提取

        // 系统标准格式：包名/.[子包名.]服务类名
        val targetServiceId = "$packageName/.accessibility.$serviceClassName"

        Log.d("AccessibilityCheck", "目标服务ID（修正格式）：$targetServiceId")
 
        // 获取无障碍管理服务：通过getSS方法获取AManager实例，用于管理和查询系统中的无障碍服务
        //这个实例Context.AS包含了实际的功能实现，比如获取已启用的无障碍服务列表等
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager?
        if (am == null) {
            Log.e("AccessibilityCheck", "获取AccessibilityManager失败！")
            return false
        }

        // 获取系统中已开启的无障碍服务列表：通过getEnabledAccessibilityServiceList方法获取当前系统中所有已开启的无障碍服务
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

        Log.d("AccessibilityCheck", "系统中已开启的服务数量：${enabledServices.size}")

        // 遍历所有服务进行匹配，检查是否有我们目标的无障碍服务TranslationAccessibilityService
        for (service in enabledServices) {
            val systemServiceId = service.id
            Log.d("AccessibilityCheck", "系统服务ID：$systemServiceId")

            // 使用包含匹配（更可靠）
            if (systemServiceId.contains("TranslationAccessibilityService")) {
                Log.d("AccessibilityCheck", "✅ 找到匹配的无障碍服务！")
                return true
            }

            // 精确匹配
            if (systemServiceId == targetServiceId) {
                Log.d("AccessibilityCheck", "✅ 精确匹配成功！")
                return true
            }
        }

        Log.d("AccessibilityCheck", "❌ 未找到匹配的无障碍服务")
        return false
    }


}
