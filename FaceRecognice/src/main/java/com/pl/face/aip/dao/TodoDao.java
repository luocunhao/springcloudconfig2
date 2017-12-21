package com.pl.face.aip.dao;

import com.pl.face.aip.entity.Todo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TodoDao {
    public List<Todo> getTodoByUserid(@Param("userid") String userid,@Param("date") String date);
}
