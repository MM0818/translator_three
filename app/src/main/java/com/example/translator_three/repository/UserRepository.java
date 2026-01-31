package com.example.translator_three.repository;

import android.content.Context;

import com.example.translator_three.AppDatabase;
import com.example.translator_three.dao.UserDao;
import com.example.translator_three.model.User;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UserRepository {
    private final UserDao userDao;
    private final ExecutorService executorService; // 异步执行器

    public UserRepository(Context context) {
        AppDatabase db = AppDatabase.getDatabase(context);
        this.userDao = db.userDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // 注册用户（自动加密密码）
    public Future<Boolean> register(String username, String phone, String password) {
        return executorService.submit(() -> {
            String encryptedPwd = User.encryptPassword(password);
            User user = new User(username, phone, encryptedPwd);
            try {
                userDao.insertUser(user);
                return true;
            } catch (Exception e) {
                return false;
            }
        });
    }

    // 用户登录
    public Future<User> login(String phone, String password) {
        return executorService.submit(() -> {
            String encryptedPwd = User.encryptPassword(password);
            return userDao.login(phone, encryptedPwd);
        });
    }

    // 检查手机号是否已注册
    public Future<User> getUserByPhone(String phone) {
        return executorService.submit(() -> userDao.getUserByPhone(phone));
    }
}
