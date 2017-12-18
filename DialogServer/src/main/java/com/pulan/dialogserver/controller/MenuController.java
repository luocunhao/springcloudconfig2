package com.pulan.dialogserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pulan.dialogserver.shiro.entity.User;
import com.pulan.dialogserver.utils.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController // (直接返回数据而不是视图)
@PropertySource(value = {"classpath:application.properties"},encoding="utf-8")
@RequestMapping(value = "/message")
public class MenuController {
	@Value("${attendance_key}")
	private String attendance; //考勤
	@Value("${meetting_key}")
	private String meetting_key; //会议
	@Value("${notify_key}")
	private String notify_key; //待阅
	@Value("${performacne_key}")
	private String perform_key; //饱和度
	@Value("${task_key}")
	private String task_key; //待办任务
	@Value("${schedule_key}")
	private String schedule_key; //日程安排
	@Value("${review_key}")
	private String review_key; //待审批流程

	@Autowired
	private RedisClient redisClient;

	@ResponseBody
	@RequestMapping(value = "sendmenutext", method = RequestMethod.POST)
	public String sendMessage(@RequestBody String msgData, HttpServletRequest request){
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		JSONObject result = new JSONObject();
		JSONObject msgObj = JSON.parseObject(msgData);
		HttpSession session = request.getSession(false);
		//Session过期
		if(session==null){
			result.put("error", "session过期");
			return JSON.toJSONString(result);
		}
		User user = (User)session.getAttribute("user");
		String username = user.getEmail();
		int type = msgObj.getIntValue("resp");
		Date date = new Date();
		String time = df.format(date); //今天
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DATE, -1);
		String time2 =df.format(calendar.getTime()); //前一天
		String key;
		switch (type){
			case 1:
				key = attendance+username+":"+time2+"*"; //考勤
				break;
			case 2:
				key = perform_key+username+":"+time2+"*"; //饱和度
				break;
			case 3:
				key = task_key+username+":"+time+"*"; //待办
				break;
			case 4:
				key = meetting_key+username+":"+time+"*"; //会议
				break;
			case 5:
				key = meetting_key+username+":"+time+"*"; //日程
				break;
			case 6:
				key = meetting_key+username+":"+time+"*"; //待阅
				break;
			case 7:
				key = meetting_key+username+":"+time+"*"; //审批
				break;
			default:
				key =username+":"+time+"*";
		}
		try {
			System.out.println("数据key："+key);
			Set<String> setrest = redisClient.muhuKey(2, key);
			if (setrest ==null ||setrest.size()==0){
				result.put("resp","抱歉，查无数据!");
				result.put("type","text");
			}else {
				List<String> list =new ArrayList<>();
				for (String str:setrest){
					String lst =redisClient.get(2,str);
					list.add(lst);
				}
				int size =list.size();
				result.put("resp",size ==1? list.get(0):list);
				result.put("type",size ==1?"text":"list");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result.toJSONString();
		
	}
}
