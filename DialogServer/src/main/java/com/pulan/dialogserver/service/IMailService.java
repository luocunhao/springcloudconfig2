package com.pulan.dialogserver.service;

import com.alibaba.fastjson.JSONObject;

public interface IMailService {

     Boolean sendHtmlMail(JSONObject respObj,String my_name,String password);
}
