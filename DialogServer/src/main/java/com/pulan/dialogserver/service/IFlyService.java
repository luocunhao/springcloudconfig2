package com.pulan.dialogserver.service;

import com.alibaba.fastjson.JSONObject;

public interface IFlyService {
    JSONObject voice2Text(String voiceUrl);
    JSONObject text2Voice(String text,JSONObject jsonObject);
    JSONObject iflySemanticUnderstand(String text,JSONObject result);
}
