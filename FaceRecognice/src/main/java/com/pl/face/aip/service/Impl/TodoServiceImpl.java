package com.pl.face.aip.service.Impl;

import com.pl.face.aip.dao.TodoDao;
import com.pl.face.aip.entity.Todo;
import com.pl.face.aip.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoServiceImpl implements TodoService {
    @Autowired
    private TodoDao todoDao;
    @Override
    public List<Todo> getTodoByUserid(String userid,String date) {
        return todoDao.getTodoByUserid(userid,date);
    }
}
