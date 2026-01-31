package com.example.translator_three.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.translator_three.model.User;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT) // 冲突时终止（避免重复注册）
    void insertUser(User user);

    @Query("SELECT * FROM users WHERE phone = :phone AND password = :password LIMIT 1")
    User login(String phone, String password);

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    User getUserByPhone(String phone);
}
