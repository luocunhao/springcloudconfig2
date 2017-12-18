package com.pulan.dialogserver.skills.request;

import com.alibaba.fastjson.JSONObject;

public interface ISkillsReqService {

    JSONObject hynQuerySkills(String msgData,String my_imei,String my_name,JSONObject result);

    JSONObject dialSkills(String msgData,String my_imei,String my_name,JSONObject result);
}
