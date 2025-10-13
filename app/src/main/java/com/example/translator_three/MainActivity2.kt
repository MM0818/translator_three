package com.example.translator_three

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ComponentActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ui.AppBarConfiguration
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


@SuppressLint("RestrictedApi")
class MainActivity2 : AppCompatActivity() {
    var fromLanguage = "aoto" //目标语言,AppCompatActivity
    var toLanguage = "auto" //翻译语言
    val appId = "20240826002132755"
    val key = "uUnQQAsgQKUFPPZL3HZH"
    var myClipboard: ClipboardManager? = null // 复制文本
    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var tvTranslation: TextView
    private lateinit var edContent: EditText
    private lateinit var iVClear: ImageView
    private lateinit var result_lay: View
    private lateinit var tv_result: TextView
    private lateinit var iv_clear_tx:ImageView
    private lateinit var iv_voice : ImageView
    private lateinit var baiduTranslateService: BaiduTranslateService
    private val TAG = "MainActivity"
    private lateinit var  iv_copy_tx :ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

        setContentView(R.layout.activity_main2)

        // 初始化ContentMain 中的控件
        tvTranslation = findViewById(R.id.tvTranslation)
        edContent = findViewById(R.id.ed_content)
        iVClear = findViewById(R.id.iv_clear_tx)
        result_lay = findViewById(R.id.result_lay)
        tv_result = findViewById(R.id.tv_result)
        iv_clear_tx =findViewById(R.id.iv_clear_tx)
        iv_voice = findViewById(R.id.iv_voice)

//        setSupportActionBar(binding.appBarMain.toolbar)

//        binding.appBarMain.fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                .setAction("Action", null)
//                .setAnchorView(R.id.fab).show()
//        }
//        val drawerLayout: DrawerLayout = binding.drawerLayout
//        val navView: NavigationView = binding.navView
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
//            ), drawerLayout
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)

        onClick()
        //输入框监听
        editTextListener()

        // 初始化Retrofit配置
        val retrofitBaidu = Retrofit.Builder()
            .baseUrl("https://fanyi-api.baidu.com/api/trans/vip/")
            .addConverterFactory(GsonConverterFactory.create())  //添加Gson 转换器
            .build()

        baiduTranslateService = retrofitBaidu.create(BaiduTranslateService::class.java)
    }
    /**
     * 输入框监听
     */
    private fun editTextListener() {
        edContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                iVClear.visibility = View.VISIBLE
                val content = edContent.text.toString().trim()
                if (content.isEmpty()) {
                    result_lay.visibility = View.GONE
                    tvTranslation.visibility = View.VISIBLE
//                    before_lay.visibility = View.VISIBLE
//                    after_lay.visibility = View.GONE
                    iVClear.visibility = View.GONE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
    }

    /**
     * 点击
     */
    private fun onClick() {
        tvTranslation.setOnClickListener { //翻译
            transition()
        }
        iv_voice.setOnClickListener {
            val intent = Intent(this@MainActivity2,MainActivity::class.java)
            // Start the new activity
            startActivity(intent)
        }
        iv_clear_tx.setOnClickListener {//清空输入框
            edContent.text.clear()
        }
        iv_copy_tx = findViewById(R.id.iv_copy_tx)
        val myClipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        iv_copy_tx.setOnClickListener {//复制文本
            val result = tv_result.text.toString()
            myClipboard!!.setPrimaryClip(ClipData.newPlainText("text", result))
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
        val word = edContent.text.toString().trim() // 需查询的单词 q
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

        call.enqueue(object : Callback<TranslateResult> {
            override fun onResponse(call: Call<TranslateResult>, response: Response<TranslateResult>) {

                // 打印原始的未处理响应
                Log.d(TAG, "原始响应内容: ${response.raw()}")

                // 继续解析响应
                Log.d(TAG, "完整响应内容: ${response.body()}")

                if (response.isSuccessful) {
                    val result = response.body()?.trans_result?.get(0)?.dst
                    runOnUiThread {
//                        tvTranslation.text = "翻译结果：$result"
                        tvTranslation.visibility = View.GONE
                        //显示翻译的结果
                        tv_result.text = response.body()?.trans_result!![0].dst
                        result_lay.visibility = View.VISIBLE
                    }
                    Log.d(TAG, "翻译结果: $result")
                } else {  //请求不成功，状态码非 2xx
                    runOnUiThread {
                        tvTranslation.text = "翻译失败：${response.errorBody()?.string()}"
                    }
                    result_lay.visibility = View.VISIBLE
                    Log.d(TAG, "请求失败: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<TranslateResult>, t: Throwable) {
                Log.d(TAG, "请求失败: ${t.message}")
            }
        })
    }

    interface BaiduTranslateService {
        @FormUrlEncoded
        @POST("translate")
        fun translate(
            @Field("q") q: String,
            @Field("from") from: String,
            @Field("to") to: String,
            @Field("appid") appid: String,
            @Field("salt") salt: String,
            @Field("sign") sign: String   //表示它会把你传入的 appid（sign） 变量的值以 "appid（sign）" 作为参数名发送到服务器
        ): Call<TranslateResult>
    }
}