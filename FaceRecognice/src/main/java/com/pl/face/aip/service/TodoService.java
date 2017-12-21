package com.pl.face.aip.service;

import com.pl.face.aip.entity.Todo;

import java.util.List;


public interface TodoService {
    public List<Todo> getTodoByUserid(String userid,String date);
}
