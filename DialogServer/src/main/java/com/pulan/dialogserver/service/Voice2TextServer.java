package com.pulan.dialogserver.service;

import com.alibaba.fastjson.JSONObject;

public interface Voice2TextServer {
    JSONObject voice2Text(String voiceUrl);
}
