package com.pl.face.aip.controller;



import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jsms.api.SendSMSResult;
import cn.jsms.api.common.SMSClient;
import cn.jsms.api.common.model.SMSPayload;
import com.alibaba.fastjson.JSON;
import com.pl.face.aip.entity.ReturnMsg;
import com.pl.face.aip.entity.User;
import com.pl.face.aip.service.TodoService;
import com.pl.face.aip.service.UserService;
import com.pl.face.aip.untils.SkillService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.pl.face.aip.untils.UtilHelper;

import javax.swing.plaf.synth.SynthOptionPaneUI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
public class VankeController {
	@Autowired
	private UserService userServiceImpl;
	@Autowired
	private SkillService skillService;
	@Autowired
	private TodoService todoServiceImpl;
	@Autowired
	@Qualifier("smsClient")
	private SMSClient smsClient;
	@Value("${jiguang.templateId}")
	private  int templateId;
	@RequestMapping(value = "/multDialog", method = RequestMethod.POST)
	@ResponseBody
	public String multDialog(@RequestBody String msgBody){
		ReturnMsg returnMsg = new ReturnMsg();
		String userid = JSONObject.parseObject(msgBody).getString("userid");
		String content = JSONObject.parseObject(msgBody).getString("content");
		//找人话术
		if(content.contains("我找")){
			String name = content.substring(2,content.length());
			List<User> users = userServiceImpl.getUserByName(name);
			JSONObject jo = new JSONObject();
			jo.put("open_id",userid);
			jo.put("rc","1");
			jo.put("resp",JSON.toJSONString(users));
			jo.put("type","list");
			returnMsg.setCode("0");
			returnMsg.setContent(jo.toJSONString());
			return JSON.toJSONString(returnMsg);
		}
		String respJson = UtilHelper.postRequest("http://210.75.8.38:8021/pulan/api", msgBody);
		JSONObject ret = JSONObject.parseObject(respJson);
		if(respJson==null){
			returnMsg.setCode("1");
			returnMsg.setErrorMsg("服务连接超时");
			return JSON.toJSONString(returnMsg);
		}
		String type = ret.getString("type");
		//多轮对话理解处理
		if("pl_semantic".equals(type)){
			JSONObject jo = new JSONObject();
			try {
				JSONObject resp = ret.getJSONObject("resp");
				String person = resp.getString("person");
				String service = resp.getString("service");
				String date = resp.getString("date");
				String intent = resp.getString("intent");
				String message = skillService.getMessageByService(service,intent,person,userid,date);
				returnMsg.setCode("0");
				returnMsg.setContent(message);
				return JSON.toJSONString(returnMsg);
			} catch (Exception e) {
				// TODO: handle exception
				returnMsg.setCode("1");
				returnMsg.setContent("技能还在开发中");
				return JSON.toJSONString(returnMsg);
			}
		}
		returnMsg.setCode("0");
		returnMsg.setContent(ret.toJSONString());
		return JSON.toJSONString(returnMsg);
	}
	@RequestMapping(value = "/sendMessage", method = RequestMethod.POST)
	@ResponseBody
	public void sendMessage(@RequestBody String msgBody){
		String phone = JSONObject.parseObject(msgBody).getString("phone");
//		String name = JSONObject.parseObject(msgBody).getString("name");
//		Map<String,String> map = new HashMap<String,String>();
//		map.put("name",name);
		SMSPayload payload = SMSPayload.newBuilder()
				.setMobileNumber(phone)
				.setTempId(templateId)
//				.setTempPara(map)
				.build();
		try {
			SendSMSResult res = smsClient.sendTemplateSMS(payload);
		} catch (APIConnectionException e) {
			e.printStackTrace();
		} catch (APIRequestException e) {
			e.printStackTrace();
		}
	}
	@RequestMapping(value = "/updown", method = RequestMethod.POST)
	@ResponseBody
	public String updown(@RequestBody String msgBody,@Param("echostr") String echostr){
		System.out.println("msgBody:"+msgBody);
		System.out.println("echostr:"+echostr);
		return echostr;
	}
	@RequestMapping(value = "/getUserList", method = RequestMethod.POST)
	@ResponseBody
	public List<User> getUserList(){
		System.out.println(userServiceImpl.getUserById("1"));
		return null;
	}
}
