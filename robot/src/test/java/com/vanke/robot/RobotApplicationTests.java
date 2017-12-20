package com.vanke.robot;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jsms.api.SendSMSResult;
import cn.jsms.api.common.SMSClient;
import cn.jsms.api.common.model.SMSPayload;
import cn.jsms.api.template.SendTempSMSResult;
import cn.jsms.api.template.TemplatePayload;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RobotApplicationTests {

	@Test
	public void sendSMSCode() {
//		String appkey = "1c4115f278f3a5feb5d29515";18773871079
//		String masterSecret = "34536e3de77681c9a90dbf17";
		String appkey = "24286bbb7fde1ab272b71a6e";
		String masterSecret = "4b9b1c29efd1109cfde08b84";
		Map<String,String> map = new HashMap<String,String>();
		map.put("name", "辣鸡");
		//map.put("location","202");
		SMSClient smsClient = new SMSClient(masterSecret,appkey);
		SMSPayload payload = SMSPayload.newBuilder()
				.setMobileNumber("18676762954")
				.setTempId(146998)
				.setTempPara(map)
				.build();
		try {
			SendSMSResult res
			= smsClient.sendTemplateSMS(payload);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
//	@Test
//	public void createTemplate() {
//		SMSClient smsClient = new SMSClient("34536e3de77681c9a90dbf17","1c4115f278f3a5feb5d29515");
//		TemplatePayload payload = TemplatePayload.newBuilder()
//				.setTempId(2)
//				.setTemplate("xixi{{name}}")
//				.setType(2).setRemark("")
//				.build();
//		try {
//			SendTempSMSResult res
//			= smsClient.createTemplate(payload);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
//	}

}
