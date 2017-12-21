package com.vanke.robot.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/vanke")
public class SMSSetController {
	@ResponseBody
	@RequestMapping(value="/valid")
	public String validUpside(@RequestParam("echostr") String echostr){
		return echostr;
	}
}
