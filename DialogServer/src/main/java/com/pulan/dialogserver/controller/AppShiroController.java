package com.pulan.dialogserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pulan.dialogserver.shiro.entity.User;
import com.pulan.dialogserver.utils.Constants;
import com.pulan.dialogserver.utils.DESPlus;
import com.pulan.dialogserver.utils.JdbcUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping(value = "/message/app")
public class AppShiroController {
	private Logger logger = Logger.getLogger(AppShiroController.class.getName());
	@Autowired
	private JdbcUtils jdbcUtils;
	DESPlus desPlus = new DESPlus("aec504733cfb4112");
	@ResponseBody
	@RequestMapping(value="/isImeiValid",method = RequestMethod.POST)
	public JSONObject isImeiValid(@RequestBody String msgData, HttpSession session){
		JSONObject msgObj = JSON.parseObject(msgData);
		JSONObject ret = new JSONObject();
		String imei = msgObj.getString("imei");
		logger.info("imei是否合法>>>>>>>>>>>>>>>:"+imei);
		JSONObject user = jdbcUtils.getImei(imei);
		String username = user.getString("mail_name");
		String password = user.getString("password");
		if(!"".equals(username)&&username!=null){
			UsernamePasswordToken usernamePasswordToken=new UsernamePasswordToken(username,desPlus.decrypt(password));
			Subject subject = SecurityUtils.getSubject();
	        subject.login(usernamePasswordToken);   //完成登录
			User user1=(User) subject.getPrincipal();
			session.setAttribute("user", user1);
			//imei认证成功
			ret.put("resp", "0");
			ret.put("user",user1);
		}else{
			//认证失败
			ret.put("resp", "-1");
		}
		return ret;
	}
	@ResponseBody
	@RequestMapping(value="/isRegisterIMEI",method = RequestMethod.POST)
	public JSONObject isRegisterIMEI(@RequestBody String msgData, HttpSession session){
			JSONObject ret = new JSONObject();
			JSONObject msgObj = JSON.parseObject(msgData);
			String username = msgObj.getString("username");
			String password = msgObj.getString("password");
			String imei = msgObj.getString("imei");
			UsernamePasswordToken usernamePasswordToken=new UsernamePasswordToken(username,password);
		try {
			Subject subject = SecurityUtils.getSubject();
			subject.login(usernamePasswordToken);   //完成登录
			jdbcUtils.registImei(imei, username, desPlus.encrypt(password));
			User user = (User) subject.getPrincipal();
			user.setImei(imei);
			logger.info("UserRegist:"+user.toString());
			session.setAttribute("user", user);
			ret.put("resp", "登录成功");
			ret.put("user",user);
			return ret;
		} catch (Exception e) {
			// TODO: handle exception
			ret.put("resp", "登录失败");
			return ret;
		}
		}
	@ResponseBody
    @RequestMapping(value="/logout",method = RequestMethod.GET)
	public JSONObject logout(RedirectAttributes redirectAttributes){
	        //使用权限管理工具进行用户的退出，跳出登录，给出提示信息
		JSONObject ret = new JSONObject();
		User user = (User) SecurityUtils.getSubject().getPrincipal();
		jdbcUtils.updateImei(user.getMail_name());
	    SecurityUtils.getSubject().logout();
	    redirectAttributes.addFlashAttribute("message", "您已安全退出！");
	    ret.put("resp", "退出成功");
	    return ret;
	    }
	//更改密码
	@ResponseBody
	@RequestMapping(value="/changePassword",method = RequestMethod.POST)
	public JSONObject changePassword(@RequestBody String msgData, HttpSession session){
		JSONObject ret = new JSONObject();
		JSONObject msgObj = JSON.parseObject(msgData);
		String password1 = msgObj.getString("password1");
		String password2 = msgObj.getString("password2");
		if(password1.equals(password2)){
			String user = (String) session.getAttribute(Constants.HTTP_SESSION_USER);
			//讲user的Imei置空
			jdbcUtils.updateImei(user);
			//更改密码
			jdbcUtils.updatePassword(user, desPlus.encrypt(password1));
			ret.put("resp", "0");
		}else{
			ret.put("resp", "-1");
		}
		return ret;
	}

	@ResponseBody
	@RequestMapping(value="/login",method = RequestMethod.GET)
	public JSONObject loginApp(){
		//使用权限管理工具进行用户的退出，跳出登录，给出提示信息
		JSONObject ret = new JSONObject();
		ret.put("resp","-1");
		return ret;
	}
}
	
