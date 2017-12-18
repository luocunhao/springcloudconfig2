package com.pulan.dialogserver.service;

import com.alibaba.fastjson.JSONObject;

public interface ISlotsReplaceService {

    JSONObject slotReplace(String content,String dataType,String openId,JSONObject result);
    String slotInputReplace(String content,String skillName,String slotType);
}
