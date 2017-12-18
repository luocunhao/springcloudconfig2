package com.pulan.dialogserver.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pulan.dialogserver.service.ISlotsReplaceService;
import com.pulan.dialogserver.service.IPulanAiServer;
import com.pulan.dialogserver.utils.HanZi2PinYingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SlotReplaceImpl implements ISlotsReplaceService{

    private Logger logger = LogManager.getLogger(SlotReplaceImpl.class.getName());
    //pulan AI 服务
    @Autowired
    private IPulanAiServer pulanAiServer;
    //汉字转拼音服务
    @Autowired
    private HanZi2PinYingUtil hanZi2PinYingUtil;

    @Override
    public JSONObject slotReplace(String resp,String dataType,String openId,JSONObject result) {
        JSONObject slot_obj = pulanAiServer.slotTypeServer(resp, openId, "person");
        if (dataType.trim().equals("") || dataType.contains("person")) {
            if (slot_obj != null && slot_obj.containsKey("person")) {
                JSONArray rep_range = slot_obj.getJSONArray("person_range");
                List<String> response = hanZi2PinYingUtil.getCnNameByPinyin(slot_obj.getString("person"));
                int resSize =response.size();
                if (resSize < 1) {
                    result.put("resp", resp);
                    result.put("content", resp);
                    result.put("type", "voice");
                    result.put("error", "");
                    result.put("types", "item");
                    logger.info("result" + result);
                    return result;
                } else {
                    List<String> contentList = new ArrayList<>();
                    for (int i = 0; i < resSize; i++) {
                        contentList.add(resp.replace(resp.substring(rep_range.getIntValue(0), rep_range.getIntValue(1)), response.get(i)));
                    }
                    if (dataType.contains("person")){
                        result.put("resp", resSize == 1 ? response.get(0) : response);
                    }else {
                        result.put("resp", resSize == 1 ? contentList.get(0) : contentList);
                    }
                    result.put("content", resSize == 1 ? contentList.get(0) : contentList);
                    result.put("type", "voice");
                    result.put("types", resSize == 1 ? "item" : "list");
                    logger.info("result" + result);
                    return result;
                }
            } else {
                result.put("resp", resp);
                result.put("content", resp);
                result.put("type", "voice");
                result.put("error", "");
                result.put("types", "item");
                logger.info("PersonName Replace Result:" + result);
                return result;
            }
        } else {
            switch (dataType){
                case "date":
                    JSONObject slot_dateObj = pulanAiServer.slotTypeServer(resp,openId,dataType);
                    if (slot_dateObj != null && slot_dateObj.containsKey(dataType)) {
                        JSONArray rep_range = slot_dateObj.getJSONArray(dataType + "_range");
                        String date_time = slot_dateObj.getString(dataType);
                        logger.info("Data语义槽提取值：" + date_time);
                        result.put("resp", date_time);
                        result.put("content", resp.replace(resp.substring(rep_range.getIntValue(0), rep_range.getIntValue(1)), date_time));
                        result.put("type", "voice");
                    } else {
                        result.put("resp", resp);
                        result.put("content", resp);
                        result.put("type", "voice");
                        result.put("error", "");
                    }
                    result.put("types", "item");
                    logger.info("Date Replace Result:" + result.toJSONString());
                    return result;
                case "datetime":
                    JSONObject slot_datetimeObj = pulanAiServer.slotTypeServer(resp,openId,dataType);
                    if (slot_datetimeObj != null && slot_datetimeObj.containsKey(dataType)) {
                        JSONArray rep_range = slot_datetimeObj.getJSONArray(dataType + "_range");
                        String date_time = slot_datetimeObj.getString(dataType);
                        logger.info("DateTime语义槽提取值：" + date_time);
                        result.put("resp", date_time);
                        result.put("content", resp.replace(resp.substring(rep_range.getIntValue(0), rep_range.getIntValue(1)), date_time));
                        result.put("type", "voice");
                    } else {
                        result.put("resp", resp);
                        result.put("content", resp);
                        result.put("type", "voice");
                        result.put("error", "");
                    }
                    result.put("types", "item");
                    logger.info("Datetime Replace Result:" + result.toJSONString());
                    return result;
                default:// 将来有其他类型时再添加 case语句
                    result.put("resp", resp);
                    result.put("content", resp);
                    result.put("type", "voice");
                    result.put("types", "item");
                    logger.info("result" + result);
                    return result;
            }
        }
    }

    /**
     * 修正语意槽值
     * @param content
     * @param slotType
     * @return
     */
    @Override
    public String slotInputReplace(String content,String skillName,String slotType) {
        JSONObject slot_dateObj = pulanAiServer.skillNameSlotServer(content,skillName,"",slotType);
        if (slot_dateObj != null){
            String date_time = slot_dateObj.getString(slotType);
            logger.info("SlotReplace_语义槽提取值：" + date_time);
            return date_time;
        }else {
            return null;
        }
    }

}
