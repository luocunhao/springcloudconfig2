package com.pulan.dialogserver.skills.response.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pulan.dialogserver.api.RobotRequest;
import com.pulan.dialogserver.entity.SemanticSlots;
import com.pulan.dialogserver.service.IFlyService;
import com.pulan.dialogserver.skills.response.ISkillsResService;
import com.pulan.dialogserver.utils.HanZi2PinYingUtil;
import com.pulan.dialogserver.utils.JdbcUtils;
import com.pulan.dialogserver.utils.RedisClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class SkillsResImpl implements ISkillsResService {

    private static final Logger logger = LogManager.getLogger(SkillsResImpl.class);
    @Autowired
    private JdbcUtils jdbcUtils;

    @Autowired
    private HanZi2PinYingUtil hanZi2PinYingUtil;

    @Autowired
    private RedisClient redisClient;

    @Autowired
    private IFlyService iFlyService;

    @Autowired
    private RobotRequest request;

    /**
     * Hyn 业务查询技能，单论对话结果处理返回。
     *
     * @param slotList  语意模板槽值集合
     * @param converKey 用户唯一Redis模板存储Key
     * @param my_name   用户的登陆账号（邮箱地址）
     * @param result    返回结果。
     * @return
     */
    @Override
    public JSONObject hynSingleDialogueRes(List<SemanticSlots> slotList, String converKey, String my_name, JSONObject result) {
        boolean isError = false;
        try {
            //满值语意槽处理拉取数据。
            StringBuffer keyBuffer = new StringBuffer();
            keyBuffer.append(slotList.get(0).getTemplateService().toLowerCase());
            for (int i = 0; i <slotList.size() ; i++) {
                SemanticSlots slot =slotList.get(i);
                String slot_code = slot.getSlotName();
                switch (slot_code) {
                    case "person":
                        String slot_value = slot.getSlotValue();
                        if (slot_value.contains("我")) {
                            keyBuffer.append(":my:" + my_name);
                        } else {
                            String email_name = jdbcUtils.getEmailByCnName(slot_value);
                            if (StringUtils.isEmpty(email_name)) {
                                String pinyin = hanZi2PinYingUtil.getNamePinYin(slot_value);
                                List<String> unameList = hanZi2PinYingUtil.getCnNameByPinyin(pinyin);
                                if (unameList.size() > 1) {
                                    result.put("resp", unameList);
                                    result.put("content", "请选择您想要查询的人员名称！");
                                    result.put("type", "text");
                                    result.put("types", "list");
                                    logger.info("出现重复人名：" + result);
                                    isError = true;
                                    slot.setSlotValue("");
                                    slotList.add(i, slot);
                                    slotList.remove(i + 1);
                                    redisClient.saveSemanticModel(1, converKey, slotList);// 存在多人名同音问题,重新让用户选择
                                } else if (unameList.size() == 1) {
                                    email_name = jdbcUtils.getEmailByCnName(unameList.get(0));
                                    keyBuffer.append(":his:" + email_name);
                                } else {
                                    result.put("resp", "抱歉，没有找到关于'" + slot_value + "'的人员信息，请重试要查询的人名！");
                                    result.put("type", "try");
                                    result.put("content", slot_value);
                                    result.put("data_type", slot_code);
                                    slot.setSlotValue("");
                                    slot.setTryCount(slot.getTryCount() - 1);
                                    slotList.add(i, slot);
                                    slotList.remove(i + 1);
                                    redisClient.saveSemanticModel(1, converKey, slotList);
                                    isError = true;//出错结束，返回错误信息
                                }
                            }else {
                                keyBuffer.append(":his:" + email_name);
                            }
                        }
                        break;
                    case "date":
                        String slot_date = slot.getSlotValue().replaceAll("-", "");
                        keyBuffer.append(":" + slot_date);
                        break;
                    default:
                        keyBuffer.append(":" + slot.getSlotValue());
                        break;
                }
                if (isError){
                    break;
                }
            }
            if (isError) {
                return result;
            }else{
                redisClient.del(1, converKey);
                String dateKey = keyBuffer.toString();
                logger.info("单轮会话,请求后台数据,Key:" + dateKey);
                result = getRedisQueryData(dateKey, result);
                String dataType = result.getString("type");
                String content = result.getString("resp");
                //语音合成
                if (dataType.equals("text")) {
                    result = iFlyService.text2Voice(content, result);
                }
                result.put("content", content);
            }
        } catch (Exception e) {
            logger.info("HynSingleDialogueRep Error:" + e.getMessage());
        }
        return result;
    }

    /**
     * Hyn 业务技能查询（多轮对话结束后对填满的语意槽结果处理）
     *
     * @param secondSSot 对应语意技能模板，语意槽值集合
     * @param converKey  用户Redis模板存储唯一Key
     * @param my_name    当前用户登陆账号（hyn用户邮箱Key）
     * @param result     结果返回。
     * @return
     */
    @Override
    public JSONObject hynManyDialogueRes(List<SemanticSlots> secondSSot, String converKey, String my_name, JSONObject result) {
        boolean isError = false; //尝试次数是否用完。
        boolean isTryAgain = false; // 是否尝试。
        boolean isBreak =false; //是否中断循环。
        try {
            StringBuffer keyBuffer = new StringBuffer();
            keyBuffer.append(secondSSot.get(0).getTemplateService().toLowerCase());
            for (int i = 0; i <secondSSot.size() ; i++) {
                SemanticSlots slot =secondSSot.get(i);
                String slot_code = slot.getSlotName();
                switch (slot_code) {
                    case "person":
                        String slot_value = slot.getSlotValue();
                        if (slot_value.contains("我")||slot_value.contains("my")) {
                            keyBuffer.append(":my:" + my_name);
                        }else if (slot_value.contains("@")){
                            keyBuffer.append(":his:" + slot_value);
                        } else {
                            String email_name = jdbcUtils.getEmailByCnName(slot_value);
                            if (StringUtils.isEmpty(email_name)) {
                                String pinyin = hanZi2PinYingUtil.getNamePinYin(slot_value);
                                List<String> unameList = hanZi2PinYingUtil.getCnNameByPinyin(pinyin);
                                if (unameList.size() > 1) {
                                    result.put("resp", unameList);
                                    result.put("content", "请选择您想要查询的人员名称！");
                                    result.put("type", "text");
                                    result.put("types", "list");
                                    logger.info("出现重复人名：" + result);
                                    isBreak = true;
                                    isTryAgain = true;
                                    slot.setSlotValue("");
                                    secondSSot.add(i, slot);
                                    secondSSot.remove(i + 1);
                                    redisClient.saveSemanticModel(1, converKey, secondSSot);
                                    // 存在多人名同音问题,重新让用户选择
                                } else if (unameList.size() == 1) {
                                    email_name = jdbcUtils.getEmailByCnName(unameList.get(0));
                                    keyBuffer.append(":his:" + email_name);
                                } else {
                                    result.put("resp", "抱歉，没有找到关于'" + slot_value + "'的人员信息，请重试要查询的人名！");
                                    result.put("type", "try");
                                    result.put("content", slot_value);
                                    result.put("data_type", slot_code);
                                    slot.setSlotValue("");
                                    int try_times = slot.getTryCount();
                                    slot.setTryCount(try_times - 1);
                                    secondSSot.add(i, slot);
                                    secondSSot.remove(i + 1);
                                    redisClient.saveSemanticModel(1, converKey, secondSSot);
                                    isBreak = true;
                                    if (try_times <= 0) {
                                        result.put("resp", "抱歉，花花已经很努力了还是没有找到关于'" + slot_value + "'的人员信息，本轮回话结束！");
                                        result.put("type", "text");
                                        isError = true;
                                    } else {
                                        result.put("data_type", slot_code);
                                        isTryAgain = true;
                                    }
                                }
                            } else {
                                keyBuffer.append(":his:" + email_name);
                            }
                        }
                        break;
                    case "date":
                        String slot_date = slot.getSlotValue().replace("-","");
                        String pattern = "\\d{8}";
                        boolean isMatch = Pattern.matches(pattern, slot_date);
                        if (isMatch) {
                            keyBuffer.append(":" + slot_date);
                        } else {
                            result.put("resp", "抱歉，您提供的日期'" + slot_date + "'格式不正确，请重试日期！");
                            result.put("type", "try");
                            result.put("content", slot_date);
                            int try_times = slot.getTryCount();
                            slot.setTryCount(try_times - 1);
                            slot.setSlotValue("");
                            secondSSot.add(i, slot);
                            secondSSot.remove(i + 1);
                            isBreak = true;
                            redisClient.saveSemanticModel(1, converKey, secondSSot);
                            if (try_times <= 0) {
                                result.put("resp", "抱歉，花花已经很努力了还是无法解析您提供的日期格式，本轮回话结束！");
                                result.put("type", "text");
                                isError = true;
                            } else {
                                result.put("data_type", slot_code);
                                isTryAgain = true;
                            }
                        }
                        break;
                    default:
                        keyBuffer.append(":" + slot.getSlotValue());
                        break;
                }
                if (isBreak){
                    break;
                }
            }
            if (isError) {
                redisClient.del(1, converKey);
                return result;
            } else if (isTryAgain) {
                return result;
            } else {
                String dateKey = keyBuffer.toString();
                result = getRedisQueryData(dateKey, result);
                String dataType = result.getString("type");
                String content = result.getString("resp");
                if (dataType.equals("text")) {
                    result = iFlyService.text2Voice(content, result);
                }
                result.put("content", content);
                logger.info("多轮会话,请求后台数据,Key:" + dateKey);
                redisClient.del(1, converKey);
            }
        } catch (Exception e) {
            logger.info("HynManyDialogueRes:" + e.getMessage());
        }
        return result;
    }

    /**
     * 穿山甲机器人单论对话。
     * @param slotList
     * @param converKey
     * @param result
     * @return
     */
    @Override
    public JSONObject manisRobotSingleDialogueRes(List<SemanticSlots> slotList, String converKey, JSONObject result) {
        boolean isError = false;
        try {
            //满值语意槽处理拉取数据。
            StringBuffer keyBuffer = new StringBuffer();
            keyBuffer.append(slotList.get(0).getTemplateService().toLowerCase());
            for (int i = 0; i <slotList.size() ; i++) {
                SemanticSlots slot =slotList.get(i);
                String slot_code = slot.getSlotName();
                switch (slot_code) {
                    case "person":
                        String slot_value = slot.getSlotValue();
                        if (slot_value.contains("我")) {
                            keyBuffer.append(":my:" + "123456");
                        } else {
                            String email_name = jdbcUtils.getEmailByCnName(slot_value);
                            if (StringUtils.isEmpty(email_name)) {
                                String pinyin = hanZi2PinYingUtil.getNamePinYin(slot_value);
                                List<String> unameList = hanZi2PinYingUtil.getCnNameByPinyin(pinyin);
                                if (unameList.size() > 1) {
                                    result.put("resp", unameList);
                                    result.put("content", "请选择您想要查询的人员名称！");
                                    result.put("type", "text");
                                    result.put("types", "list");
                                    logger.info("出现重复人名：" + result);
                                    isError = true;
                                    slot.setSlotValue("");
                                    slotList.add(i, slot);
                                    slotList.remove(i + 1);
                                    redisClient.saveSemanticModel(1, converKey, slotList);// 存在多人名同音问题,重新让用户选择
                                } else if (unameList.size() == 1) {
                                    email_name = jdbcUtils.getEmailByCnName(unameList.get(0));
                                    keyBuffer.append(":his:" + email_name);
                                } else {
                                    result.put("resp", "抱歉，没有找到关于'" + slot_value + "'的人员信息，请重试要查询的人名！");
                                    result.put("type", "try");
                                    result.put("content", slot_value);
                                    result.put("data_type", slot_code);
                                    slot.setSlotValue("");
                                    slot.setTryCount(slot.getTryCount() - 1);
                                    slotList.add(i, slot);
                                    slotList.remove(i + 1);
                                    redisClient.saveSemanticModel(1, converKey, slotList);
                                    isError = true;//出错结束，返回错误信息
                                }
                            }else {
                                keyBuffer.append(":his:" + email_name);
                            }
                        }
                        break;
                    case "date":
                        String slot_date = slot.getSlotValue().replaceAll("-", "");
                        keyBuffer.append(":" + slot_date);
                        break;
                    default:
                        keyBuffer.append(":" + slot.getSlotValue());
                        break;
                }
                if (isError){
                    break;
                }
            }
            if (isError) {
                return result;
            } else {
                String dateKey = keyBuffer.toString();
                logger.info("单轮会话,请求后台数据,Key:" + dateKey);
                result = getRedisQueryData(dateKey, result);
                result.put("content", result.getString("resp"));
                redisClient.del(1, converKey);
            }
        } catch (Exception e) {
            logger.info("HynSingleDialogueRep Error:" + e.getMessage());
        }
        return result;
    }

    /**
     * 穿山甲机器人多轮对话。
     * @param secondSSot 多轮对话语义槽对象列表
     * @param converKey 当前用户保存的模版Key。
     * @param result 返回的结果。
     * @return
     */
    @Override
    public JSONObject manisRobotManyDialogueRes(List<SemanticSlots> secondSSot, String converKey, JSONObject result) {
        boolean isError = false; //尝试次数是否用完。
        boolean isTryAgain = false; // 是否尝试。
        boolean isBreakIn =false; //是否打断
        try {
            StringBuffer keyBuffer = new StringBuffer();
            keyBuffer.append(secondSSot.get(0).getTemplateService().toLowerCase());
            for (int i = 0; i < secondSSot.size() ; i++) {
                SemanticSlots slot =secondSSot.get(i);
                String slot_code = slot.getSlotName();
                switch (slot_code) {
                    case "person":
                        String slot_value = slot.getSlotValue();
                        if (slot_value.contains("我")||slot_value.contains("my")) {
                            keyBuffer.append(":my:" + "123456");
                        }else if (slot_value.contains("@")){
                            keyBuffer.append(":his:" + slot_value);
                        } else {
                            String email_name = jdbcUtils.getEmailByCnName(slot_value);
                            if (StringUtils.isEmpty(email_name)) {
                                String pinyin = hanZi2PinYingUtil.getNamePinYin(slot_value);
                                List<String> unameList = hanZi2PinYingUtil.getCnNameByPinyin(pinyin);
                                if (unameList.size() > 1) {
                                    result.put("resp", unameList);
                                    result.put("content", "请选择您想要查询的人员名称！");
                                    result.put("type", "text");
                                    result.put("types", "list");
                                    logger.info("出现重复人名：" + result);
                                    isBreakIn = true;
                                    isTryAgain = true;
                                    slot.setSlotValue("");
                                    secondSSot.add(i, slot);
                                    secondSSot.remove(i + 1);
                                    redisClient.saveSemanticModel(1, converKey, secondSSot);
                                    // 存在多人名同音问题,重新让用户选择
                                } else if (unameList.size() == 1) {
                                    email_name = jdbcUtils.getEmailByCnName(unameList.get(0));
                                    keyBuffer.append(":his:" + email_name);
                                } else {
                                    result.put("resp", "抱歉，没有找到关于'" + slot_value + "'的人员信息，请重试要查询的人名！");
                                    result.put("type", "try");
                                    result.put("content", slot_value);
                                    result.put("data_type", slot_code);
                                    slot.setSlotValue("");
                                    int try_times = slot.getTryCount();
                                    slot.setTryCount(try_times - 1);
                                    secondSSot.add(i, slot);
                                    secondSSot.remove(i + 1);
                                    redisClient.saveSemanticModel(1, converKey, secondSSot);
                                    isBreakIn = true;
                                    if (try_times <= 0) {
                                        result.put("resp", "抱歉，花花已经很努力了还是没有找到关于'" + slot_value + "'的人员信息，本轮回话结束！");
                                        result.put("type", "text");
                                        isError = true;
                                    } else {
                                        result.put("data_type", slot_code);
                                        isTryAgain = true;
                                    }
                                }
                            } else {
                                keyBuffer.append(":his:" + email_name);
                            }
                        }
                        break;
                    case "date":
                        String slot_date = slot.getSlotValue().replace("-","");
                        String pattern = "\\d{8}";
                        boolean isMatch = Pattern.matches(pattern, slot_date);
                        if (isMatch) {
                            keyBuffer.append(":" + slot_date);
                        } else {
                            result.put("resp", "抱歉，您提供的日期'" + slot_date + "'格式不正确，请重试日期！");
                            result.put("type", "try");
                            result.put("content", slot_date);
                            int try_times = slot.getTryCount();
                            slot.setTryCount(try_times - 1);
                            slot.setSlotValue("");
                            secondSSot.add(i, slot);
                            secondSSot.remove(i + 1);
                            isBreakIn = true;
                            redisClient.saveSemanticModel(1, converKey, secondSSot);
                            if (try_times <= 0) {
                                result.put("resp", "抱歉，花花已经很努力了还是无法解析您提供的日期格式，本轮回话结束！");
                                result.put("type", "text");
                                isError = true;
                            } else {
                                result.put("data_type", slot_code);
                                isTryAgain = true;
                            }
                        }
                        break;
                    default:
                        keyBuffer.append(":" + slot.getSlotValue());
                        break;
                }
                if (isBreakIn){
                    break;
                }
            }
            if (isError) {
                redisClient.del(1, converKey);
                return result;
            } else if (isTryAgain) {
                return result;
            } else {
                String dateKey = keyBuffer.toString();
                result = getRedisQueryData(dateKey, result);
                result.put("content", result.getString("resp"));
                logger.info("多轮会话,请求后台数据,Key:" + dateKey);
                redisClient.del(1, converKey);
            }
        } catch (Exception e) {
            logger.info("ManisRobotManyDialogueRes:" + e.getMessage());
        }
        return result;
    }

    /**
     *  发送邮件。
     * @param secondSSot
     * @param converKey
     * @param result
     * @return
     */
    @Override
    public JSONObject hynSendEmail(List<SemanticSlots> secondSSot, String converKey, JSONObject result) {
        boolean isError = false; //尝试次数是否用完。
        boolean isTryAgain = false; // 是否尝试。
        boolean isBreakIn =false; //是否打断。
        try {
            JSONObject emailObj = new JSONObject();
            for (int i = 0; i < secondSSot.size(); i++) {
                SemanticSlots slot = secondSSot.get(i);
                String slot_code = slot.getSlotName();
                switch (slot_code) {
                    case "person":
                        String slot_value = slot.getSlotValue();
                        if (slot_value.contains("@")){ //判断语义槽值是否已经是邮箱账号。
                            emailObj.put("address", slot_value);
                        }else {
                            String email_name = jdbcUtils.getEmailByCnName(slot_value);
                            if (StringUtils.isEmpty(email_name)) {
                                String pinyin = hanZi2PinYingUtil.getNamePinYin(slot_value);
                                List<String> unameList = hanZi2PinYingUtil.getCnNameByPinyin(pinyin);
                                if (unameList.size() > 1) {
                                    result.put("resp", unameList);
                                    result.put("content", "请选择您想要查询的人员名称！");
                                    result.put("type", "text");
                                    result.put("types", "list");
                                    logger.info("出现重复人名：" + result);
                                    isTryAgain = true;
                                    slot.setSlotValue("");
                                    secondSSot.add(i, slot);
                                    secondSSot.remove(i + 1);
                                    redisClient.saveSemanticModel(1, converKey, secondSSot);
                                    isBreakIn = true; // 存在多人名同音问题,重新让用户选择
                                } else if (unameList.size() == 1) {
                                    email_name = jdbcUtils.getEmailByCnName(unameList.get(0));
                                    emailObj.put("address", email_name);
                                } else {
                                    result.put("resp", "抱歉，没有找到关于'" + slot_value + "'的人员信息，请重试要查询的人名！");
                                    result.put("type", "robot");
                                    result.put("content", slot_value);
                                    result.put("slot_type", slot_code);
                                    slot.setSlotValue("");
                                    int try_times = slot.getTryCount();
                                    slot.setTryCount(try_times - 1);
                                    secondSSot.add(i, slot);
                                    secondSSot.remove(i + 1);
                                    redisClient.saveSemanticModel(1, converKey, secondSSot);
                                    if (try_times <= 0) {
                                        result.put("resp", "抱歉，花花已经很努力了还是没有找到关于'" + slot_value + "'的人员信息，本轮会话结束！");
                                        result.put("type", "text");
                                        isError = true;
                                    } else {
                                        result.put("data_type", slot_code);
                                        isTryAgain = true;
                                    }
                                    isBreakIn = true; //出错结束，返回错误信息
                                }
                            } else {
                                emailObj.put("address", email_name);
                            }
                        }
                        break;
                    case "subject":
                        emailObj.put("subject", slot.getSlotValue());
                        break;
                    default:
                        emailObj.put("content", slot.getSlotValue());
                        break;
                }
                if (isBreakIn) {
                    break;
                }
            }
            if (isError) {
                redisClient.del(1, converKey);
                return result;
            } else if (isTryAgain) {
                return result;
            } else {
                logger.info("多轮会话,发送邮件文体内容:" + emailObj.toJSONString());
                redisClient.del(1, converKey);
                redisClient.saveSemanticModel(1, "email:" + converKey, emailObj);
                result.put("resp", emailObj);
                result.put("type", "email");
            }
        } catch (Exception e) {
            logger.info("HynSendEmailRes:" + e.getMessage());
        }
        return result;
    }

    /**
     * 提醒做什么
     * @param slotList
     * @param my_imei
     * @param result
     * @return
     */
    @Override
    public JSONObject reminderDoSomething(List<SemanticSlots> slotList,String my_imei, JSONObject result) {
        JSONObject remindObj = new JSONObject();
        remindObj.put("msg_name", "设置提醒");
        boolean isError =false;
        remindObj.put("msg_type","set_reminder");
        long overtime =0;
        try {
            String slot_value;
            for (SemanticSlots slot : slotList) {
                String slot_code = slot.getSlotName();
                switch (slot_code) {
                    case "person":
                        remindObj.put("alias", my_imei);
                        break;
                    case "datetime":
                        slot_value = slot.getSlotValue();
                        String pattern = "\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}";
                        boolean isMatch = Pattern.matches(pattern, slot_value);
                        if (isMatch){
                            DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            LocalDateTime  date = LocalDateTime.parse(slot_value,f);
                            long setTime = date.toInstant(ZoneOffset.ofHours(8)).getEpochSecond();
                            Instant now = Instant.now();
                            long nowTime =now.getEpochSecond();
                            overtime= setTime-nowTime;
                            remindObj.put("reminder_time",slot_value);
                        }else {
                            isError =true;
                            result.put("resp","抱歉，设定提醒的时间'"+slot_value+"'格式出错啦！请稍后重试。");
                            result.put("type","error");
                        }
                        break;
                    case "content":
                        slot_value =slot.getSlotValue();
                        remindObj.put("msg_content",slot_value);
                        break;
                    default:
                        slot_value =slot.getSlotValue();
                        remindObj.put("other",slot_value);
                        break;
                }
                if (isError){
                    break;
                }
            }
            if (!isError){
                String reminderKey =remindObj.toJSONString();
                logger.info("Set Reminder Object:"+reminderKey);
                redisClient.set(0,reminderKey,"timedtask");
                redisClient.expire(0,reminderKey,(int)overtime);
                String respText = "好的，到%s的时候我会提醒您的！";
                result.put("resp",String.format(respText,remindObj.getString("reminder_time")));
                result.put("type","text");
            }else {
                return result;
            }
        } catch (Exception e) {
            logger.info("ReminderResp:" + e.getMessage());
            result.put("resp","设置提醒失败，请重试！");
            result.put("type","error");
        }
        return result;
    }

    /**
     * 打电话
     * @param tempService
     * @param slotsList
     * @param result
     * @return
     */
    @Override
    public JSONObject oneSlotSkill(String tempService,List<SemanticSlots> slotsList, JSONObject result) {
        switch (tempService){
            case "Dial":
                for (SemanticSlots slot : slotsList) {
                    result.put("resp", slot.getSlotValue());
                }
                result.put("type", tempService.toLowerCase());
                break;
            case "Booking":
                for (SemanticSlots slot : slotsList) {
                    result.put("resp", slot.getSlotValue());
                }
                result.put("type", "custom");
                break;
            case "Query":
                result.put("resp","抱歉，花花没有找到你想要的信息！");
                result.put("type","text");
                break;
            case "Complaint":
                result.put("resp","好的，我已经将您的投诉内容记下，尽快反馈给我们的负责人，在2个工作日内反馈给您的投诉处理结果，感谢您对我公司的关注！");
                result.put("type","text");
                break;
            case "Fix":
                result.put("resp","好的，我已经帮您安排了修理师傅，师傅将会在半小时内与您取得联系！");
                result.put("type","text");
                break;
            case "Help":
                result.put("resp","好的，请稍等，已经帮您通知物业管理人员！");
                result.put("type","text");
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * 打开应用
     * @param tempService
     * @param slotsList
     * @param result
     * @return
     */
    @Override
    public JSONObject openApplications(String tempService, List<SemanticSlots> slotsList, JSONObject result) {
        for (SemanticSlots slot : slotsList) {
            String urlName = jdbcUtils.getNetUrl(slot.getSlotValue());
            if (StringUtils.isEmpty(urlName)) {
                result.put("resp", "抱歉，没有找到您想要的应用,请联系管理员！");
                result.put("type", "error");
            } else {
                result.put("resp", urlName);
                result.put("type", "weburl");
            }
        }
        return result;
    }

    /**
     * 发送邮件，短信。
     * @param secondSSot
     * @param converKey
     * @param result
     * @return
     */
    @Override
    public JSONObject sendMessage(List<SemanticSlots> secondSSot, String converKey, JSONObject result) {
        String skillService =secondSSot.get(0).getTemplateService();
        try {
            switch (skillService){
                case "Message":
                    JSONObject smsObj =new JSONObject();
                    for (SemanticSlots slot:secondSSot){
                        String slot_code = slot.getSlotName();
                        if (slot_code.equals("person")){
                            smsObj.put("sms_person",slot.getSlotValue());
                        }else if (slot_code.equals("content")){
                            smsObj.put("content",slot.getSlotValue());
                        }
                    }
                    result.put("resp",smsObj);
                    result.put("type","sms");
                    redisClient.del(1,converKey);
                    break;
                case "Email":
                    result =hynSendEmail(secondSSot,converKey,result);
                    break;
                default:
                    result.put("resp","Send Message Error!");
                    result.put("type","error");
                    redisClient.del(1,converKey);
                    break;
            }
        }catch (Exception e){
            logger.error("Send Message Error!"+e.getMessage());
        }
        return result;
    }

    /**
     * 空调控制
     * @param slotList
     * @param converKey
     * @param result
     * @return
     */
    @Override
    public JSONObject airConditioner(String tempService,List<SemanticSlots> slotList, String converKey, JSONObject result) {
        String content ;
        StringBuffer buffer =new StringBuffer();
       // result.put("type","AirConditioner");
        result.put("type","text");
        try {
            switch (tempService){
                case "On":
                    String [] strings ={"亲，花花已经帮你打%s","%s的空调,","温度：%s、","模式：%s、","风速：%s。"};
                    for (int i = 0; i <slotList.size() ; i++) {
                        content = String.format(strings[i],slotList.get(i).getSlotValue());
                        buffer.append(content);
                    }
                    result.put("resp",buffer.toString());
                    break;
                case "Off":
                    String resp1 ="亲，花花已经帮你把%s的空调关闭了！";
                    for (int i = 1; i <slotList.size() ; i++) {
                        content = String.format(resp1,slotList.get(i).getSlotValue());
                        resp1 =content;
                    }
                    result.put("resp",resp1);
                    break;
                case "Control":
                    String [] strings1 ={"亲，花花已经帮你重新设置了%s的空调,","温度：%s、","模式：%s、","风速：%s。"};
                    for (int i = 0; i <slotList.size() ; i++) {
                        content =String.format(strings1[i],slotList.get(i).getSlotValue());
                        buffer.append(content);
                    }
                    result.put("resp",buffer.toString());
                    break;
                case "Query":
                    JSONObject retObj  =new JSONObject();
                    retObj.put("intent","Query");
                    retObj.put("service","AirConditioner");
                    for (SemanticSlots slots :slotList){
                        retObj.put(slots.getSlotName(),slots.getSlotValue());
                    }
                    result.put("resp",retObj);
                    break;
                default:
                    result.put("resp","AirConditioner Is Error!");
                    break;
            }
            redisClient.del(1,converKey);
        }catch (Exception e){

        }
        return result;
    }


    /**
     * 从Redis获取数据。
     * @param key
     * @param resultObj
     * @return
     */
    private JSONObject getRedisQueryData(String key, JSONObject resultObj) {
        String ruleKey = key + "*";
        try {
            Set<String> setrest = redisClient.muhuKey(2, ruleKey);
            if (setrest == null || setrest.size() == 0) {
                resultObj.put("resp", "抱歉，查无数据!");
                resultObj.put("type", "text");
            } else {
                List<String> list = new ArrayList<>();
                for (String str : setrest) {
                    String lst = redisClient.get(2, str);
                    list.add(lst);
                }
                int size = list.size();
                resultObj.put("resp", size == 1 ? list.get(0) : list);
                resultObj.put("type", size == 1 ? "text" : "list");
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return resultObj;
    }


}
