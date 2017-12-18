package com.pl.face.aip.controller;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.pl.face.aip.untils.UtilHelper;

@RestController
public class VankeController {
	@RequestMapping(value = "/multDialog", method = RequestMethod.POST)
	@ResponseBody
	public JSONObject multDialog(@RequestBody String msgBody){
		String userid = JSONObject.parseObject(msgBody).getString("userid");
//		HttpSession session = request.getSession();
		String respJson = UtilHelper.postRequest("http://210.75.8.38:8021/pulan/api", msgBody);
		JSONObject ret = JSONObject.parseObject(respJson);
//		int newrc = ret.getIntValue("rc");
//		int oldrc = (session.getAttribute(userid)==null)?-2:(int)session.getAttribute(userid);
		//type = 'pl_semantic' 语义槽值填满 做业务数据查询
		if(respJson==null){
			ret.put("type", "error");
			ret.put("resp", "服务连接超时");
			return ret;
		}
		String type = ret.getString("type");
		if("pl_semantic".equals(type)){
			JSONObject jo = new JSONObject();
			try {
				String person = ret.getJSONObject("resp").getString("person");
				String service = ret.getJSONObject("resp").getString("service");
				String date = ret.getJSONObject("resp").getString("date");
				jo.put("resp", service+":没有"+person+date+"的数据");
				return jo;
			} catch (Exception e) {
				// TODO: handle exception
				jo.put("resp","技能还在开发中");
				return jo;
			}
		}
//		session.setAttribute(userid, newrc);
		return ret;
	}
}
