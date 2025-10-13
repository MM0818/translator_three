package com.example.translator_three;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.OnConflictStrategy;

@Dao
public interface UserDao {
    // 插入用户，手机号已存在则忽略
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertUser(User user);

    // 根据手机号查询用户
    @Query("SELECT * FROM users WHERE phone = :phone")
    User getUserByPhone(String phone);

    // 登录验证
    @Query("SELECT * FROM users WHERE phone = :phone AND password = :password")
    User login(String phone, String password);
}
