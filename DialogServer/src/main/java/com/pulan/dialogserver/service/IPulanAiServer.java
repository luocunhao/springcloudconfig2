package com.pulan.dialogserver.service;

import com.alibaba.fastjson.JSONObject;

public interface IPulanAiServer {
    //pulan 语意理解服务。
    JSONObject aiServer(String content,String openid);
    //用slot type 做语义槽提取
    JSONObject slotTypeServer(String content,String openid,String type);
    //用技能名做语义槽提取
    JSONObject skillNameSlotServer(String content,String skillName,String openid,String type);
}
