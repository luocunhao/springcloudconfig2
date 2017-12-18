package com.pulan.dialogserver.utils;

import com.alibaba.fastjson.JSONObject;
import com.pulan.dialogserver.shiro.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class ShiroDao {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public User findByUserName(String username){
		String sql = "select * from ai_user where mail_name = ?";
		List<User> list= null;
	    list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<User>(User.class),username);
	    if(!list.isEmpty()){
	    	return list.get(0);
	    }else{
	    	return new User();
	    }
    }
	//根据userid获取该用户的所有权限
	public List<String> getAllPermissionByUserid(String userid){
		String sql = "select b.permission_name  from ai_user_role as a ,ai_permission as b where a.role_id = b.role_id and a.user_id = ?";
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql,userid);
		List<String> list = new ArrayList<String>();
		if(!rows.isEmpty()){  
			for (Map<String, Object> map : rows) {
				String permission_name = map.get("permission_name").toString();
				list.add(permission_name);
			 }
	       }
		return list;
	    }
	//查询用户的角色
	public List<JSONObject> getAllUserPermission(int fromIndex, int pageSize){
		String sql = "SELECT a.id,a.mail_name,a.cn_name,c.role_name FROM ai_user AS a ,ai_user_role AS b ,ai_role AS c WHERE a.id = b.user_id AND b.role_id = c.id limit ?,?";
		List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql,fromIndex,pageSize);
		List<JSONObject> list = new ArrayList<JSONObject>();
		if(!rows.isEmpty()){
		for (Map<String, Object> map : rows) {
			JSONObject jo = new JSONObject();
			String id = map.get("id").toString();
			String cn_name = map.get("cn_name").toString();
			String role_name = map.get("role_name").toString();
			String mail_name = map.get("mail_name").toString();
			jo.put("id", id);
			jo.put("mail_name", mail_name);
			jo.put("role_name", role_name);
			jo.put("cn_name",cn_name);
			list.add(jo);
		}
		return list;
		}else{
			return list;
		}
	}
	//修改用户的角色
	//新增用户的角色
	//删除用户的角色
	//查看角色的权限
	//修改角色的权限
	//新增角色的权限
	//删除角色的权限
	}
