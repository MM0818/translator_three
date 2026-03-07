package com.example.translator_three

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.ComponentActivity
import androidx.core.content.ContextCompat
import com.iflytek.cloud.ErrorCode
import com.iflytek.cloud.InitListener
import com.iflytek.cloud.RecognizerListener
import com.iflytek.cloud.RecognizerResult
import com.iflytek.cloud.SpeechConstant
import com.iflytek.cloud.SpeechError
import com.iflytek.cloud.SpeechRecognizer
import com.iflytek.cloud.SpeechUtility
import java.util.Locale
//引入百度翻译api所需的依赖
//import android.os.Bundle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Field
import java.security.MessageDigest

//录音视图

@SuppressLint("RestrictedApi")  //使用安卓开发的特殊注解抑制Lnit工具警告，括号中是指定要忽略的是使用了受限API的警告
class VoiceTActivity : ComponentActivity() {
    //百度翻译,初始化控件变量
    private lateinit var baiduTranslateService: BaiduTranslateService
    private lateinit var tvTranslateResult: TextView
    private lateinit var speechRecognizer: SpeechRecognizer
    private var isRecognizing = false
    private val TAG = "MainActivity"
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voicet)

        // 请求录音权限
        requestAudioPermission()

        // 不能在“=”与appid之间添加任何空字符或者转义符
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=1d295360")

        // 初始化语音识别
        initVoiceRecognize()

        ///初始化控件
        tvTranslateResult = findViewById(R.id.tvTranslateResult)

        // 设置按钮点击事件
        findViewById<Button>(R.id.btnStartListening).setOnClickListener {
            if (!isRecognizing&& ::speechRecognizer.isInitialized) {
                speechRecognizer.startListening(recognizerListener)
                isRecognizing = true
                findViewById<TextView>(R.id.tvRecognizeStatus).text = "正在听，请说中文或英文..."
            }else {
                Log.e(TAG,"speechRecognizer 未初始化")
            }
        }

        findViewById<Button>(R.id.btnStopListening).setOnClickListener {
            if (isRecognizing ) {
                speechRecognizer.stopListening()
                isRecognizing = false
                findViewById<TextView>(R.id.tvRecognizeStatus).text = "识别停止"
            }
        }

        //百度翻译
        // 初始化Retrofit配置，通过简单的接口来处理网络请求
        val retrofitBaidu = Retrofit.Builder()
            .baseUrl("https://fanyi-api.baidu.com/api/trans/vip/")  //百度翻译的api
            .addConverterFactory(GsonConverterFactory.create())  //添加Gson 转换器
            .build()

        baiduTranslateService = retrofitBaidu.create(BaiduTranslateService::class.java)
        //translate()
    }

    //检查应用是否具有录音权限，如果没有则请求用户授权；如果已经具有录音权限，则会调用语音识别初始化函数。
    //像ContextCompat、ActivityCompat、PackageManager这些都是来自AndroidX库和Android框架的类，是标准API，用于处理权限、上下文操作等功能。
    //前两个是- AndroidX 库的一部分，提供了 向下兼容 的实现，例如，权限检查在不同 Android 版本的实现可能不同，这些类会根据设备的 Android 版本自动选择合适的实现方式
    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { //如果未授予该权限则进入请求流程
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)  //（当前页面实例，权限数组，请求码用于回调处理）
        } else {
            // 如果已经获得权限，可以调用语音识别
            initVoiceRecognize()
        }
    }

    // 初始化对象时，通过此回调接口，获取初始化状态。
    //InitListener是讯飞语音识别SDK提供的接口，用于监听语音识别引擎的初始化状态。通过错误码来判断初始化是否成功（0是成功）
    private val initListener = InitListener { code ->
        Log.d(TAG, "SpeechRecognizer initListener() code = $code")
        if (code != ErrorCode.SUCCESS) {
            Log.d(TAG, "语音识别初始化失败，错误码：$code")
        } else {
            Log.d(TAG, "语音识别初始化成功，错误码：$code")
        }
    }

    //设置语音识别的各种参数，并开始监听用户的语音输入。
    private fun initVoiceRecognize() {
            // 获取系统默认语言
            val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { //判断当前安卓设备的版本是否大于等于Android7
                LocaleList.getDefault().get(0)  //获取系统默认语言区域列表中的第一个语言设置
            } else {
                Locale.getDefault()  //否则获取系统默认的语言设置
            }

            val language = "${locale.language}-${locale.country}"  //把语言代码和国家代码用字符连接起来
            Log.d(TAG, "系统默认language: $language")

            // 初始化无UI语音识别对象，用于存储语音识别引擎的实例
            //SpeechRecognizer是讯飞语音识别SDK提供的类，用于创建和管理语音识别功能
            speechRecognizer = SpeechRecognizer.createRecognizer(this, initListener).also { //（上下文对象，初始化监听器），当初始化完成或失败时会回调这个监听器。also是作用域函数，it指当前引擎实例
                if (it != null) {
                    // 设置返回结果格式，目前支持json, xml以及plain 三种格式，其中plain为纯听写文本内容
                    it.setParameter(SpeechConstant.RESULT_TYPE, "plain")
                    // 设置语音前端点:静音超时时间，单位ms，即用户多长时间不说话则当做超时处理
                    it.setParameter(SpeechConstant.VAD_BOS, "4000")
                    // 设置语音后端点:后端点静音检测时间，单位ms，即用户停止说话多长时间内即认为不再输入
                    it.setParameter(SpeechConstant.VAD_EOS, "1000")
                    
                    // 设置语音输入语言，根据系统语言自动切换识别语言：zh_cn为简体中文, en_us为美式英文
                    when {
                        language.equals("zh-CN", ignoreCase = true) -> {  //中文系统识别中文
                            it.setParameter(SpeechConstant.LANGUAGE, "zh_cn")
                        }
                        else -> { //其他语言默认识别英文
                            it.setParameter(SpeechConstant.LANGUAGE, "en_us")
                        }
                    }

                    Log.d(TAG, "语音识别对象完成初始化")
                } else {
                    Log.d(TAG, "语音识别对象 == null")
                    return  ///////////退出防止访问空对象，这里语音识别对象是空的，但是后台能识别并且context有中文结果
                }
            }

            // 设置识别监听器，开始监听
            isRecognizing = true
            speechRecognizer.startListening(recognizerListener)  //启动语音识别，括号里是识别监听器，用于处理识别过程中的各种事件，如音量变化、识别结果等
        }

    // 权限请求的回调方法，是安卓系统自动调用的回调方法
    //1开发者调用请求权限方法->2系统显示权限请求对话框->3用户点击允许或者拒绝->4安卓系统自动调用该回调方法去处理用户的选择
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {  //判断当前回调是哪个权限请求的结果
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {  //如果权限数组第一个也就是录音权限被授予
                initVoiceRecognize()  // 用户授权后初始化语音识别
            } else {
                Log.e(TAG, "录音权限被拒绝，请在设置中启用权限。")
                // 提示用户权限被拒绝，可以考虑显示对话框
            }
        }
        //调用父类方法确保权限处理的完整性
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    ///////////////百度翻译
    private fun translate(text:String) {
        val word = text // 需查询的单词 q
        val from = "auto" // 源语种 en 英语 zh 中文
        var to = "en" // 目标语种，根据实际情况设置
        val appid = "20240826002132755" // 百度创建的应用的翻译API的appid
        val salt = (Math.random() * 100 + 1).toInt() // 随机数这里范围是[0,100]整数无强制要求
        val key = "uUnQQAsgQKUFPPZL3HZH" // 百度翻译API的密钥
        val secretKey = "$appid$word$salt$key" // 拼接的密钥
        val sign = MD5Utils.md5(secretKey) // MD5加密

        Log.d(TAG, "secretKey: $secretKey")
        Log.d(TAG, "sign: $sign")

        val call = baiduTranslateService.translate(word, from, to, appid, salt.toString(), sign)

        call.enqueue(object : Callback<RespondBean> {
            override fun onResponse(call: Call<RespondBean>, response: Response<RespondBean>) {

            // 打印原始的未处理响应
                Log.d(TAG, "原始响应内容: ${response.raw()}")

                // 继续解析响应
                Log.d(TAG, "完整响应内容: ${response.body()}")

                if (response.isSuccessful) {
                    val result = response.body()?.trans_result?.get(0)?.dst
                    runOnUiThread {
                        tvTranslateResult.text = "释义：$result"  //////有这个，但是是空的
                    }
                    Log.d(TAG, "翻译结果: $result")
                } else {  //请求不成功，状态码非 2xx
                    runOnUiThread {
                        tvTranslateResult.text = "翻译失败：${response.errorBody()?.string()}"
                    }
                    Log.d(TAG, "请求失败: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<RespondBean>, t: Throwable) {
                Log.d(TAG, "请求失败: ${t.message}")
            }
        })
    }

    // 源URL https://fanyi-api.baidu.com/api/trans/vip/translate
    // 参数如下
    // String q 英文单词/中文
    // String from 原始语种 zh中文/en英文
    // String to 目标语种 zh中文/en英文
    // String appid appid的注解
    // String salt 随机数（整形转字符串）
    // String sign 签名 32位字母小写MD5编码的 appid+q+salt+密钥

    // 定义API接口
    interface BaiduTranslateService {
        @FormUrlEncoded
        @POST("translate")
        fun translate(
            @Field("q") q: String,
            @Field("from") from: String,
            @Field("to") to: String,
            @Field("appid") appid: String,    ////这里只是注解，并不需要使用我的appid，在调用时这个变量正确传递值就行
            @Field("salt") salt: String,
            @Field("sign") sign: String   //注解表示它会把你传入的 appid（sign） 变量的值以 "appid（sign）" 作为参数名发送到服务器
        ): Call<RespondBean>
    }

    // 定义响应数据模型，处理从翻译API接收到的数据，并在应用的用户界面中展示翻译结果。
    data class RespondBean(
        val from: String,
        val to: String,
        val trans_result: List<TransResultBean>
    )

    data class TransResultBean(
        val src: String,
        val dst: String
    )

    // MD5工具类
    object MD5Utils {
        fun md5(input: String): String {
            return try {
                val md = MessageDigest.getInstance("MD5")
                val digest = md.digest(input.toByteArray(Charsets.UTF_8))
                digest.fold("", { str, it -> str + "%02x".format(it) })
            } catch (e: Exception) {
                "MD5加密异常"
            }
        }

    }  //////

    // 初始化识别监听器，这是讯飞语音的SDK接口，当语音识别引擎有识别结果时会自动调用回调onResult方法
    private val recognizerListener by lazy {
        object : RecognizerListener {
            override fun onVolumeChanged(volume: Int, bytes: ByteArray?) { // 音量变化
                // 音量变化，可以用来动态更新 UI 中的音量指示器
                Log.d(TAG, "音量变化: $volume")

                // 获取 ProgressBar 实例
                val volumeBar = findViewById<ProgressBar>(R.id.volumeBar)

                // 更新进度条的进度值
                volumeBar.progress = volume
            }

            override fun onBeginOfSpeech() { // 开始说话
                Log.d(TAG, "开始说话")
            }

            override fun onEndOfSpeech() { // 结束说话
                Log.d(TAG, "结束说话")
                // 继续识别，并设置监听器
                speechRecognizer.startListening(this)
            }

            override fun onResult(recognizerResult: RecognizerResult?, isLast: Boolean) { // 返回结果
                if (recognizerResult == null) {
                    Log.d(TAG, "识别出来 onResult: $isLast  recognizerResult == null")
                } else {
                    Log.d(TAG, "识别出来 onResult: $isLast    content: ${recognizerResult.resultString}")   ///////这一步是有的

                    //更新UI结果的TextView
                    findViewById<TextView>(R.id.tvRecognizeResult).text = recognizerResult.resultString

                    //结合了百度翻译，实现中译英过程
                    //当用户说完话，SDK 识别出结果后，会调用 recognizerListener 的 onResult 方法然后开始显示翻译结果
                    translate(recognizerResult.resultString)  ///////////这里如果不调用中译英函数就不会闪退，并且能识别中文显示结果
                }
            }

            override fun onError(speechError: SpeechError) { // 错误回调
                Log.e(TAG, "语音识别出错: ${speechError.errorDescription}")
                if (isRecognizing) {
                    Log.e(TAG, "语音识别出错 onError()")
                }
            }

            override fun onEvent(eventType: Int, arg1: Int, arg2: Int, bundle: Bundle?) { // 扩展接口
                Log.d(TAG, "事件类型: $eventType")
                // 例如，处理录音开始、停止等特定事件
            }
        }
    }




}


