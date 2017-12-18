package com.pulan.dialogserver.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pulan.dialogserver.config.SysConfig;
import com.pulan.dialogserver.service.IFlyService;
import com.pulan.dialogserver.service.IRedisService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class IFlyServiceImpl implements IFlyService {
    private Logger logger = LogManager.getLogger(IFlyServiceImpl.class);
    @Autowired
    private SysConfig sysConfig;
    @Autowired
    private IRedisService iRedisService;

    // 讯飞语音听写
    @Override
    public JSONObject voice2Text(String voiceUrl) {
        JSONObject result =iFlyServer("text",voiceUrl);
        logger.info("讯飞语音转文字结果："+result.toJSONString());
        return result;
    }

    // 讯飞语音合成
    @Override
    public JSONObject text2Voice(String text,JSONObject restObj) {
        if (text.length()> sysConfig.getTextSize()){
            restObj.put("resp",text);
            restObj.put("type","text");
        }else {
            JSONObject result =iFlyServer("voice",text);
            logger.info("讯飞语音合成结果:" + result.toJSONString());
            String iflyVoice = result.getString("retObj");
            if (iflyVoice.length()>5){
                String voiceUrl = sysConfig.getVoice_url() + iflyVoice.substring(5, iflyVoice.length());
                restObj.put("resp",voiceUrl);
                restObj.put("type","voice");
            }else {
                restObj.put("resp",text);
                restObj.put("type","text");
            }
        }
        return restObj;
    }

    // 讯飞语义理解。
    @Override
    public JSONObject iflySemanticUnderstand(String text,JSONObject result) {
        if (StringUtils.isEmpty(text)){
            return result;
        }
        JSONObject senseResult =iFlyServer("sense",text);
        logger.info("讯飞语义理解结果："+senseResult.toJSONString());
        String error =senseResult.getString("error");
        String iflyRet =senseResult.getString("retObj");
        if (StringUtils.isEmpty(error)||StringUtils.isEmpty(iflyRet)){
            result.put("resp","哎呀，讯飞技能理解出错了！");
            result.put("type","text");
        }else {
          JSONObject iflyObj =JSON.parseObject(senseResult.getString("retObj"));
          int rc =iflyObj.getIntValue("rc");
          if (rc ==0 ||rc ==3){
              String service =iflyObj.getString("service");
              switch (service){
                  case "weather": //问天气
                      String restObj =iflyObj.getJSONObject("answer").getString("text");
                      result.put("resp",restObj);
                      result.put("type","text");
                      break;
                  case "openQA": //开放问答
                      String restObj1 =iflyObj.getJSONObject("answer").getString("text");
                      result.put("resp",restObj1);
                      result.put("type","text");
                      break;
                  case "poetry": //诗词对答
                      String restObj2 =iflyObj.getJSONObject("answer").getString("text");
                      result.put("resp",restObj2);
                      result.put("type","text");
                      break;
                  case "datetime": //问时间
                      String restObj3 =iflyObj.getJSONObject("answer").getString("text");
                      result.put("resp",restObj3);
                      result.put("type","text");
                      break;
                  case "telephone": //打电话
                      JSONObject telpObj =new JSONObject();
                      JSONObject semantic = JSON.parseObject(iflyObj.getJSONArray("semantic").get(0).toString());
                      String intent = semantic.getString("intent");//语义意图
                      result.put("intent",intent);
                      JSONArray slots = semantic.getJSONArray("slots");
                      if (slots.size()>0) {
                          for (int i = 0; i < slots.size(); i++) {
                              JSONObject o = JSONObject.parseObject(slots.get(i).toString());
                              String name = o.getString("name");
                              String value = o.getString("value");
                              telpObj.put(name, value);
                          }
                          result.put("resp",telpObj.toJSONString());
                          result.put("type","linking");
                      }else {
                          String answer = iflyObj.getJSONObject("answer").getString("text");
                          result.put("resp",answer);
                          result.put("type","text");
                      }
                      break;
                  case "flight": //问航班
                      String answer = iflyObj.getJSONObject("answer").getString("text");
                      result.put("resp",answer);
                      result.put("type","text");
                      break;
                  case "joke": //讲笑话
                      String anwser = iflyObj.getJSONObject("answer").getString("text");
                      String path = iflyObj.getJSONObject("data").getJSONArray("result").getJSONObject(0).getString("mp3Url");
                      result.put("content", anwser);
                      result.put("resp", path);
                      result.put("type", "voice");
                      break;
                  case "cookbook": //问菜谱
                      String anwser1 = iflyObj.getJSONObject("answer").getString("text");
                      JSONObject cookObj = iflyObj.getJSONObject("data").getJSONArray("result").getJSONObject(0);
                      cookObj.remove("id");
                      cookObj.remove("intro");
                      cookObj.remove("status");
                      result.put("content", anwser1);
                      result.put("resp", cookObj.toJSONString());
                      result.put("type", "cookbook");
                      break;
                  default:
                      result.put("resp","还没有对此技能做解析，请联系管理员！");
                      result.put("type","text");
              }
          }else {
              result.put("reps","啊，主人你问的问题太难了，花花也是醉了，我们换个话题吧！");
              result.put("type","text");
          }
        }
        return result;
    }

    private JSONObject iFlyServer(String type,String content) {
        content =content.replaceAll(" ","&");
        logger.info("AIUI处理的信息:" +type+"::"+content);
        JSONObject ret = new JSONObject();
        ret.put("error", "");
        ret.put("retObj", null);
        if (StringUtils.isEmpty(content)) {
            logger.info("IFlyServer没有收到请求数据!");
            ret.put("error","IFlyServer没有收到请求数据，讯飞服务请求失败！");
            return ret;
        }
        JSONObject iflyObj = new JSONObject();
        String iflyChannel = sysConfig.getIfly_channel();
        switch (type){
            case "voice": //请求语音合成
                iflyObj.put("iFlyFunc", 1);
                iflyObj.put("content", content);
                break;
            case "text": //请求语音识别
                iflyObj.put("iFlyFunc", 0);
                iflyObj.put("content", content);
                break;
            default: //请求语义理解
                iflyObj.put("iFlyFunc", 3);
                iflyObj.put("content", content);
                break;
        }
        ret = iRedisService.rpcCall(iflyChannel, iflyObj, 10);
        return ret;
    }
}
