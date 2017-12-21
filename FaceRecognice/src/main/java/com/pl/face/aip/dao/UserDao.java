package com.pl.face.aip.dao;

import com.pl.face.aip.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserDao {
    public List<User> getUserList();
    public List<User> getUserByPinyin(@Param("pinyinname")String pinyinname);
    public void addUser(User user);
    public User getUserById(String userid);
}
