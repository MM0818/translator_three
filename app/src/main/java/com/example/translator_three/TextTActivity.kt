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
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.ui.AppBarConfiguration;
import com.example.translator_three.accessibility.TranslationAccessibilityService
import com.example.translator_three.model.TranslateResult
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

@SuppressLint("RestrictedApi")
class TextTActivity : AppCompatActivity() {
    var fromLanguage = "auto" // 源语言
    var toLanguage = "auto" // 目标语言
    val appId = "20240826002132755"
    val key = "uUnQQAsgQKUFPPZL3HZH"
    var myClipboard: ClipboardManager? = null // 复制文本

    private lateinit var appBarConfiguration: AppBarConfiguration  //延迟定义
    private lateinit var tvTranslation: TextView
    private lateinit var edContent: EditText
    private lateinit var iVClear: ImageView
    private lateinit var result_lay: View
    private lateinit var tv_result: TextView
    private lateinit var iv_clear_tx: ImageView
    private lateinit var iv_voice: ImageView
    private lateinit var baiduTranslateService: BaiduTranslateService
    private val TAG = "MainActivity"  //调试时筛选日志
    private lateinit var iv_copy_tx: ImageView
    private lateinit var spLanguage: Spinner
    // 新增：划词翻译图标
    private lateinit var iv_word_translate: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_textt)

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

        // 初始化Retrofit配置
        val retrofitBaidu = Retrofit.Builder()  //使用Retrofit建造者模式方法，创建实例
            .baseUrl("https://fanyi-api.baidu.com/api/trans/vip/") //指定百度翻译API的基础地址
            .addConverterFactory(GsonConverterFactory.create()) //添加Gson转换器，用于自动将JSON响应转换为kotlin对象
            .build()  //完成配置并创建Retrofit实例

        //创建服务接口实例，用于发起网络请求
        baiduTranslateService = retrofitBaidu.create(BaiduTranslateService::class.java)  

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
            val myClipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager  //通过该方法获取系统剪切板服务
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

        val selectedText = spLanguage.selectedItem?.toString() ?: "中文 -> 英文"
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

        //val from = "auto" // 源语种：自动
        //var to = "en" // 目标语种，这里限制了结果为英文！！！！
        val appid = "20240826002132755"  //百度翻译API的应用ID
        val salt = (Math.random() * 100 + 1).toInt()  //随机数，用于生成签名
        val key = "uUnQQAsgQKUFPPZL3HZH"  //百度翻译API的密钥
        val secretKey = "$appid$word$salt$key"  // 用于生成签名的密钥，包含应用ID、待翻译文本、随机数和密钥
        val sign = MD5Utils.md5(secretKey)  // 对密钥进行MD5加密，生成签名

        Log.d(TAG, "secretKey: $secretKey")
        Log.d(TAG, "sign: $sign")

        //2、API请求阶段
        //使用baiduTS（通过Retrofit创建的服务接口）的翻译方法来创建请求
        val call = baiduTranslateService.translate(word, fromLang, toLang, appid, salt.toString(), sign)
        //通过enqueue方法异步执行请求（避免主线程阻塞），将结果回调到主线程更新UI
        call.enqueue(object : Callback<TranslateResult> {
            //3、处理响应阶段：实现一个Callback接口
            override fun onResponse(
                call: Call<TranslateResult>,
                response: Response<TranslateResult>
            ) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(this@TextTActivity, "请求失败", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                val body = response.body()
                if (body == null || body.trans_result.isNullOrEmpty()) {
                    Log.e(TAG, "翻译失败，返回为空：$body")
                    runOnUiThread {
                        Toast.makeText(this@TextTActivity, "翻译失败，请稍后再试", Toast.LENGTH_SHORT).show()
                    }
                    return
                }

                val resultText = body.trans_result[0].dst

                runOnUiThread {
                    tvTranslation.visibility = View.GONE
                    tv_result.text = resultText
                    result_lay.visibility = View.VISIBLE
                }
            }


            override fun onFailure(call: Call<TranslateResult>, t: Throwable) {
                Log.d(TAG, "请求失败: ${t.message}")
            }
        })
    }

    //可能有冗余的部分代码======================
    //这是一个使用Retrofit网络库定义的接口，专门用于调用百度翻译API。它的作用是将API抽象为简洁的方法调用
    interface BaiduTranslateService {
            @FormUrlEncoded  //标记请求体使用表单编码格式，Retrofit会自动将方法参数转换为表单字段
            @POST("translate")  //标记这是一个POST请求，translate是API的端点路径
            fun translate(
                @Field("q") q: String,  //F（“字段名”）参数名：类型  //要翻译的文本
                @Field("from") from: String,  //源语言
                @Field("to") to: String,  //目标语言
                @Field("appid") appid: String,  //百度翻译API应用ID
                @Field("salt") salt: String,  //随机数
                @Field("sign") sign: String  //MD5签名
            ): Call<TranslateResult>  //返回类型Call：是Retrofit中的核心接口，表示一个可执行的HTTP请求，提供异步执行enqueue方法和同步执行execute方法。
        //TranslateResult ：泛型参数，Retrofit 会自动将 API 返回的 JSON 转换为 TranslateResult 实例
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
