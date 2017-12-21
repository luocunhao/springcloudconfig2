package com.pl.face.aip.service;

import com.pl.face.aip.entity.User;

import java.util.List;

public interface UserService {
    public List<User> getUserList();
    public List<User> getUserByName(String name);
    public void addUser(User user);
    public User getUserById(String userid);
}
