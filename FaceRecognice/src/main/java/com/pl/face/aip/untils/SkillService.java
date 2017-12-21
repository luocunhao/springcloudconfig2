package com.pl.face.aip.untils;

import com.alibaba.fastjson.JSON;
import com.pl.face.aip.entity.Todo;
import com.pl.face.aip.entity.User;
import com.pl.face.aip.service.TodoService;
import com.pl.face.aip.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkillService {
    @Autowired
    private UserService userServiceImpl;
    @Autowired
    private TodoService todoServiceImpl;
    public  String getMessageByService(String service ,String intent,String person,String userid,String date){
        String ret = "查无数据";
        if(!"我".equals(person)){
            userid = userServiceImpl.getUserByName(person).get(0).getUserid();
        }
        if("Task".equals(service)){
            if("Query".equals(intent)){
                List<Todo> todos = todoServiceImpl.getTodoByUserid(userid,date);
                ret = JSON.toJSONString(todos);
            }
        }
        return ret;
    }
}
