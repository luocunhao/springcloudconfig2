package com.pulan.dialogserver.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pulan.dialogserver.api.RobotRequest;
import com.pulan.dialogserver.config.SysConfig;
import com.pulan.dialogserver.service.ITuLingService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TuLingServiceImpl implements ITuLingService{

    private Logger logger = LogManager.getLogger(TuLingServiceImpl.class);

    @Autowired
    private RobotRequest robotRequest;
    @Autowired
    private SysConfig sysConfig;

    @Override
    public JSONObject tuLintRobot(String content,String open_id,JSONObject result) {
        logger.info("TuLingMessage:"+content);
        JSONObject tuLing = new JSONObject();
        tuLing.put("key",sysConfig.getRobbotApiKey());
        tuLing.put("info", content);
        tuLing.put("userid", open_id);
        String tuLintresult = robotRequest.postRequest(sysConfig.getRobbotAPI(), JSON.toJSONString(tuLing));
        logger.info("TuLingRobotResponse-----:"+tuLintresult);
        if (StringUtils.isEmpty(tuLintresult)){
            logger.error("Tuling robot Recognitied Failed!");
            result.put("error", "Tuling robot cant not response this question!");
            result.put("resp","");
        }else {
            JSONObject tlObj = JSON.parseObject(tuLintresult);
            int code = tlObj.getIntValue("code");
            switch (code){
                case 100000: // 文本类
                    String textContent = tlObj.getString("text");
                    result.put("open_id", open_id);
                    result.put("resp", textContent);
                    result.put("type", "text");
                    break;
                case 200000: // 链接类
                    String cont = tlObj.getString("text");
                    String url = tlObj.getString("url");
                    result.put("open_id", open_id);
                    result.put("resp", cont);
                    result.put("type", "link");
                    result.put("url", url);
                    break;
                case 302000: // 新闻类
                    String cont2 = tlObj.getString("text");
                    JSONArray list = tlObj.getJSONArray("list");
                    result.put("open_id", open_id);
                    result.put("resp", cont2);
                    result.put("type", "news");
                    result.put("list", list);
                    break;
                case 308000: // 菜谱类
                    String cont3 = tlObj.getString("text");
                    JSONArray list3 = tlObj.getJSONArray("list");
                    result.put("open_id", open_id);
                    result.put("resp", cont3);
                    result.put("type", "menus");
                    result.put("list", list3);
                    break;
                default: // 其他类
                    String cont4 = tlObj.getString("text");
                    result.put("open_id", open_id);
                    result.put("resp", cont4);
                    result.put("type", "other");
                    break;
            }
        }
        return result;
    }
}
