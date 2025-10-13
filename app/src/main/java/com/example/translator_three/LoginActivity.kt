package com.example.translator_three

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    private lateinit var db: AppDatabase  // 使用lateinit延迟初始化
    private lateinit var phoneEt: EditText  // 改为lateinit不可变
    private lateinit var pwdEt: EditText    // 改为lateinit不可变

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 初始化数据库
        db = AppDatabase.getDatabase(this)

        // 绑定控件（使用lateinit不需要判空）
        phoneEt = findViewById(R.id.et_account)
        pwdEt = findViewById(R.id.et_password)
        val loginBtn: Button = findViewById(R.id.login)
        val registerBtn: TextView = findViewById(R.id.register)

        // 自动填充手机号
        val registeredPhone = intent.getStringExtra("phone")
        if (!registeredPhone.isNullOrEmpty()) {
            phoneEt.setText(registeredPhone)
        }

        // 登录按钮点击事件
        loginBtn.setOnClickListener {
            login()
        }

        // 跳转到注册页面
        registerBtn.setOnClickListener {
            startActivity(Intent(this, RegistActivity::class.java))
        }
    }

    private fun login() {
        val phone = phoneEt.text.toString().trim()
        val password = pwdEt.text.toString().trim()

        // 重置错误提示
        phoneEt.error = null
        pwdEt.error = null

        var isValid = true

        // 手机号验证
        if (phone.isEmpty()) {
            phoneEt.error = "手机号不能为空"
            isValid = false
        } else if (!phone.matches("^1\\d{10}$".toRegex())) {
            phoneEt.error = "请输入正确的11位手机号"
            isValid = false
        }

        // 密码验证
        if (password.isEmpty()) {
            pwdEt.error = "密码不能为空"
            isValid = false
        }

        if (isValid) {
            // 查询数据库（注意：Kotlin中Room操作需要在协程中执行）
            lifecycleScope.launch {
                val matchedUser = withContext(Dispatchers.IO) {
                    db.userDao().login(phone, password)
                }

                if (matchedUser != null) {
                    Toast.makeText(this@LoginActivity, "登录成功！", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@LoginActivity, MainActivity2::class.java)
                    intent.putExtra("username", matchedUser.username)
                    intent.putExtra("phone", matchedUser.phone)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "手机号或密码错误", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
