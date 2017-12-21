package com.pl.face.aip.service.Impl;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinFormat;
import com.github.stuxuhai.jpinyin.PinyinHelper;
import com.pl.face.aip.dao.UserDao;
import com.pl.face.aip.entity.User;
import com.pl.face.aip.service.UserService;
import com.pl.face.aip.untils.UtilHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private UserDao userdao;
    @Override
    public List<User> getUserList() {
        return userdao.getUserList();
    }

    @Override
    public List<User> getUserByName(String name) {
        String pinyin = UtilHelper.hanzi2pinyin(name);
        return userdao.getUserByPinyin(pinyin);
    }

    @Override
    public void addUser(User user) {
        userdao.addUser(user);
    }

    @Override
    public User getUserById(String userid) {
        return userdao.getUserById(userid);
    }
}
