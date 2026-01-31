package com.example.translator_three

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.translator_three.model.User

class RegistActivity : AppCompatActivity() {
    private var db: AppDatabase? = null
    private var usernameEt: EditText? = null
    private var phoneEt: EditText? = null
    private var pwdEt: EditText? = null
    private var confirmPwdEt: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_regist)


        // 初始化数据库
        db = AppDatabase.getDatabase(this)


        // 绑定控件
        usernameEt = findViewById(R.id.register_user_name)
        phoneEt = findViewById(R.id.register_phone) // 确保是EditText
        pwdEt = findViewById(R.id.register_user_password)
        confirmPwdEt = findViewById(R.id.confirm_pwd)
        val registerBtn = findViewById<TextView>(R.id.register_success)
        val loginBtn = findViewById<TextView>(R.id.login)

        // 注册按钮点击事件
        registerBtn.setOnClickListener { v: View? -> register() }


        // 跳转到登录页面
        loginBtn.setOnClickListener { v: View? ->
            startActivity(
                Intent(
                    this@RegistActivity,
                    LoginActivity::class.java
                )
            )
        }
    }

    private fun register() {
        // 获取输入内容
        val username = usernameEt!!.text.toString().trim { it <= ' ' }
        val phone = phoneEt!!.text.toString().trim { it <= ' ' }
        val password = pwdEt!!.text.toString().trim { it <= ' ' }
        val confirmPwd = confirmPwdEt!!.text.toString().trim { it <= ' ' }

        // 重置错误提示
        usernameEt!!.error = null
        phoneEt!!.error = null
        pwdEt!!.error = null
        confirmPwdEt!!.error = null

        // 验证逻辑
        var isValid = true


        // 手机号验证（11位数字）
        if (phone.isEmpty()) {
            phoneEt!!.error = "手机号不能为空"
            isValid = false
        } else if (!phone.matches("^1\\d{10}$".toRegex())) {
            phoneEt!!.error = "请输入正确的11位手机号"
            isValid = false
        }


        // 用户名验证
        if (username.isEmpty()) {
            usernameEt!!.error = "昵称不能为空"
            isValid = false
        }


        // 密码验证
        if (password.isEmpty()) {
            pwdEt!!.error = "密码不能为空"
            isValid = false
        } else if (!password.matches("^(?![0-9]+$)(?![a-zA-Z]+$)(?!([^(0-9a-zA-Z)]|[\\(\\)])+$)([^(0-9a-zA-Z)]|[\\(\\)]|[a-zA-Z]|[0-9]){6,16}$".toRegex())) {
            pwdEt!!.error = "密码为6~16位，需包含数字、字母或符号中的至少两种"
            isValid = false
        }


        // 确认密码验证
        if (confirmPwd.isEmpty()) {
            confirmPwdEt!!.error = "确认密码不能为空"
            isValid = false
        } else if (confirmPwd != password) {
            confirmPwdEt!!.error = "两次密码不一致"
            isValid = false
        }

        // 所有验证通过
        if (isValid) {
            // 检查手机号是否已注册
            val existingUser = db!!.userDao().getUserByPhone(phone)
            if (existingUser != null) {
                phoneEt!!.error = "该手机号已注册"
                Toast.makeText(this, "注册失败", Toast.LENGTH_SHORT).show()
            } else {
                // 保存新用户
                val newUser = User(username, phone, password)
                db!!.userDao().insertUser(newUser)


                // 注册成功，跳转到登录页
                Toast.makeText(this, "注册成功，请登录", Toast.LENGTH_SHORT).show()
                val intent = Intent(
                    this@RegistActivity,
                    LoginActivity::class.java
                )
                intent.putExtra("phone", phone) // 传递手机号
                startActivity(intent)
                finish() // 关闭当前页面
            }
        }
    }
}