package com.pulan.dialogserver.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pulan.dialogserver.api.RobotRequest;
import com.pulan.dialogserver.config.SysConfig;
import com.pulan.dialogserver.service.IPulanAiServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class PulanAiServerImpl implements IPulanAiServer {

    private Logger logger = LogManager.getLogger(PulanAiServerImpl.class);
    @Autowired
    private RobotRequest robotRequest;
    @Autowired
    private SysConfig sysConfig;
    @Override
    public JSONObject aiServer(String content, String openid) {
        JSONObject retObj =new JSONObject();
        logger.info("Text Message to Ai:" + content);
        /*JSONObject plserver = new JSONObject();
        plserver.put("identity_key", SysConf.PulanApiKey);
        plserver.put("content",content);
        plserver.put("open_id",openid);*/
        String plrest = robotRequest.getRequest(sysConfig.getPulan_api(),content,"");
        if (StringUtils.isEmpty(plrest)){
            retObj.put("retObj","");
            retObj.put("error","PlServer Understand error!");
        }else {
            JSONObject pljsObj = JSON.parseArray(plrest).getJSONObject(0);
            Double rc = pljsObj.getDouble("rc");
            if (rc > sysConfig.getRc_val()){
                pljsObj.put("opend_id",openid);
                retObj.put("retObj",pljsObj);
                retObj.put("error","");
            }else {
                //没有识别的语义说法，在后期要存入mangledb 数据库当作是语义模型训练的数据源。暂时忽略不做处理。
                //语意理解错误处理。
                retObj.put("content",content);
                retObj.put("retObj","");
                retObj.put("error","PlServer Understand error!");
            }
        }
        return retObj;
    }

    @Override
    public JSONObject slotTypeServer(String content,String openid,String type) {
        logger.info("SlotType语意槽值提取："+content);
        RobotRequest plRobot = new RobotRequest();
        String plrest = plRobot.getRequest(sysConfig.getPlSlotApi(), content, type);
        try{
            JSONObject slotObj = JSON.parseObject(plrest);
            JSONArray slots;
            if (slotObj.containsKey("slots")){
                slots = slotObj.getJSONArray("slots");
                slotObj = slots.getJSONObject(0);
            }else {
                slotObj = null;
            }
            return slotObj;
        } catch(Exception e){
            logger.error("语意槽提取出错, Slots isEmpty!");
            return null;
        }
    }

    @Override
    public JSONObject skillNameSlotServer(String content, String skillName, String openid, String type) {
        logger.info("Utterance语意槽值提取："+content);
        String plrest = robotRequest.getRequest(sysConfig.getPlSlotApi(), content, skillName);
        try{
            JSONObject slotObj = JSON.parseObject(plrest);
            JSONArray slots;
            boolean isFind =false;
            if (slotObj.containsKey("slots")){
                slots = slotObj.getJSONArray("slots");
                JSONObject slopt =new JSONObject();
                for (int i = 0; i <slots.size() ; i++) {
                    slopt =slots.getJSONObject(i);
                    if (slopt.containsKey(type)){
                        isFind =true;
                        break;
                    }
                }
                if (isFind){
                    return slopt;
                }else {
                    return null;
                }
            }else {
                return null;
            }
        } catch(Exception e){
            logger.error("语意槽提取出错, Slots isEmpty!");
            return null;
        }
    }
}
