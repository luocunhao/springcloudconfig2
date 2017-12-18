package com.pulan.dialogserver.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pulan.dialogserver.controller.AppMultiDialogController;
import com.pulan.dialogserver.entity.Function;
import com.pulan.dialogserver.entity.SemanticSlots;
import com.pulan.dialogserver.service.*;
import com.pulan.dialogserver.skills.response.ISkillsResService;
import com.pulan.dialogserver.utils.HanZi2PinYingUtil;
import com.pulan.dialogserver.utils.HelpUtils;
import com.pulan.dialogserver.utils.JdbcUtils;
import com.pulan.dialogserver.utils.RedisClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class MultiDialogServiceImpl implements IMultiDialogService {

    private Logger logger = LogManager.getLogger(AppMultiDialogController.class);
    @Autowired
    private IPulanAiServer pulanAiServer;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private JdbcUtils jdbcUtils;
    @Autowired
    private HelpUtils helpUtils;
    @Autowired
    private IFlyService iFlyService;
    @Autowired
    private ITuLingService iTuLingService;
    @Autowired
    private ISlotsReplaceService iSlotsReplaceService;
    @Autowired
    private HanZi2PinYingUtil hanZi2PinYingUtil;

    @Autowired
    private ISkillsResService iSkillsResService;

    /**
     * App 多轮会话逻辑控制
     *
     * @param msgData app输入飞文本
     * @param my_imei 当前用户APP上IMEI号
     * @param my_name 当前用户的邮箱账号
     * @param result  返回的结果
     * @return 返回值
     */
    @Override
    public String appMultiDialog(String msgData, String my_imei, String my_name, JSONObject result) {
        String converKey = my_name + ":" + my_imei;
        logger.info("Now User ModelKey：" + converKey);
        Boolean isSlotNull = false;
        try {
            String awaken = jdbcUtils.getAwakenWord();
            JSONObject msgObj = JSON.parseObject(msgData);
            String type = msgObj.getString("type"); // 返回语言/文字的类型。
            String voicetext = msgObj.getString("resp"); // 文本内容（标识语意槽修改，手动输入，值为input）
            String open_id = msgObj.getString("open_id"); // 设备编号 opend_id
            String inslot_type = msgObj.getString("slot_type"); //语意槽类型。
            // 如果语音内容是 你可以做什么
            if (helpUtils.isDoSomething(voicetext)) {
                List<Function> list = jdbcUtils.getFunctions();
                result.put("resp", list);
                result.put("type", "fun");
                // 语音唤醒
            } else if (voicetext.contains(awaken)) {
                /*List<String> lstx = redisClient.lrange(1,"hyn:hello");
                int resint = helpUtils.getRandomNumber(lstx.size());
                String content = lstx.get(resint);*/
                String content = "主人,我在,有什么我可以帮助您的？";
                if ("voice".equals(type)) {
                    result = iFlyService.text2Voice(content, result);
                    result.put("content", content);
                } else {
                    result.put("resp", content);
                    result.put("type", "text");
                }
            } else {
                // 判断当前用户是否已经存在对话语义问答模板对象。
                String converModel = redisClient.get(1, converKey); //当前用户存储的会话模板。
                if (StringUtils.isEmpty(converModel)) {
                    JSONObject retObj = pulanAiServer.aiServer(voicetext, open_id); //请求语义理解服务。
                    logger.info("PlServer语义结果:" + retObj.getString("retObj"));
                    // 语义理解能识别 error =null ;
                    if (StringUtils.isEmpty(retObj.getString("error"))) {
                        JSONObject retJson = retObj.getJSONObject("retObj");
                        String service = retJson.getString("service");
                        JSONObject semanticObj = retJson.getJSONArray("semantic").getJSONObject(0);
                        JSONObject slots = null;
                        if (semanticObj.containsKey("slots")) {
                            slots = semanticObj.getJSONArray("slots").getJSONObject(0);
                        }
                        String intent = semanticObj.getString("intent");
                        //根据意图去查询语义模版是否存在
                        Boolean isModelExist = jdbcUtils.isSemanticModelExist(service, intent);
                        if (isModelExist) {
                            List<SemanticSlots> semsot = jdbcUtils.getSemanticSlot(intent, service);
                            redisClient.saveSemanticModel(1, converKey, semsot);// 存储会话空模版
                            // 处理语义槽值, 没有任何语义槽时，直接返回第一个模板问题
                            if (slots == null || slots.isEmpty()) {
                                // 语义槽为空返回第一个语义槽为空的问题。
                                boolean isNull = false;
                                for (SemanticSlots slotObj : semsot) {
                                    String fsolt = slotObj.getSlotValue();
                                    if (StringUtils.isEmpty(fsolt)) {
                                        String question = slotObj.getPrompt();
                                        result.put("resp", question);
                                        result.put("data_type", slotObj.getSlotName());
                                        result.put("type", "text");
                                        isNull = true;
                                        break;
                                    }
                                }
                                //判断上面是否存在空语义槽。
                                if (!isNull) {
                                    String tempIntent = semsot.get(0).getTemplateIntent();
                                    result = iSkillsResService.oneSlotSkill(tempIntent, semsot, result);
                                    redisClient.del(1, converKey);
                                }
                            } else {
                                List<SemanticSlots> slotList = new ArrayList<>();
                                for (SemanticSlots sst : semsot) {
                                    String slotName = sst.getSlotName();
                                    if (slots.containsKey(slotName)) {
                                        String slot_value = slots.getString(slotName);
                                        sst.setSlotValue(slot_value);
                                    }
                                    slotList.add(sst);
                                }
                                // 存储填充后的模板
                                redisClient.saveSemanticModel(1, converKey, slotList);
                                // 再次遍历模板槽，查找是否存在空的槽
                                for (SemanticSlots sst : slotList) {
                                    String jsr_value = sst.getSlotValue();
                                    // 发现空槽，返回问题，提示用户回答
                                    if (StringUtils.isEmpty(jsr_value)) {
                                        String question = sst.getPrompt();
                                        result.put("resp", question);
                                        result.put("data_type", sst.getSlotName());
                                        result.put("type", "text");
                                        isSlotNull = true;
                                        break;
                                    }
                                }
                                // 1、判断第一次会话对象语义槽是否有null值。
                                // 2、语意槽没有null 值 则执行获取数据的服务，返回给用户数据，并且删除之前存储的用户语意模板。
                                // 3、此处是单论对话的处理逻辑。
                                if (!isSlotNull) {
                                    String tempIntent = slotList.get(0).getTemplateIntent();//语义理解服务类型
                                    switch (tempIntent) {
                                        case "Query": //花样年查询类服务
                                            String tempService = slotList.get(0).getTemplateService();
                                            if (tempService.equals("AirConditioner")) { //空调状态查询。
                                                result = iSkillsResService.airConditioner(tempIntent, slotList, converKey, result);
                                            } else {
                                                result = iSkillsResService.hynSingleDialogueRes(slotList, converKey, my_name, result);
                                            }
                                            break;
                                        case "Saturation":
                                            result = iSkillsResService.hynSingleDialogueRes(slotList, converKey, my_name, result);
                                            break;
                                        case "Dial":  //打电话服务
                                            result = iSkillsResService.oneSlotSkill(tempIntent, slotList, result);
                                            redisClient.del(1, converKey);
                                            break;
                                        case "Set": //设置提醒 当前imei号作为别名。
                                            result = iSkillsResService.reminderDoSomething(slotList, my_imei, result);
                                            redisClient.del(1, converKey);
                                            break;
                                        case "Visit": //打开xx应用类服务
                                            result = iSkillsResService.openApplications(tempIntent, slotList, result);
                                            redisClient.del(1, converKey);
                                            break;
                                        case "Booking": //预定会议室
                                            result = iSkillsResService.oneSlotSkill(tempIntent, slotList, result);
                                            redisClient.del(1, converKey);
                                            break;
                                        case "On": //打开空调
                                            result = iSkillsResService.airConditioner(tempIntent, slotList, converKey, result);
                                            break;
                                        case "Off": //关闭空调
                                            result = iSkillsResService.airConditioner(tempIntent, slotList, converKey, result);
                                            break;
                                        case "Control": //设置空调
                                            result = iSkillsResService.airConditioner(tempIntent, slotList, converKey, result);
                                            break;
                                        default: //没有匹配的模板
                                            result.put("resp", "技能模板正在开发中！");
                                            result.put("type", "error");
                                            redisClient.del(1, converKey);
                                            break;
                                    }
                                }
                            }
                        } else {
                            //找不到相符合的语义模版,调用图灵机器人。
                            redisClient.del(1, converKey);
                            result = iTuLingService.tuLintRobot(voicetext, open_id, result);
                            String tlError = result.getString("error");
                            if (!StringUtils.isEmpty(tlError)) {
                                result.put("resp", "主人，我还没有学会这个技能，换个试试吧！");
                                result.put("type", "text");
                            }
                        }
                        // 语义识别理解不了的情况处理。
                    } else {
                        // 语意理解错误处理。
                        redisClient.del(1, converKey);
                        result = iTuLingService.tuLintRobot(voicetext, open_id, result);
                        String tlError = result.getString("error");
                        if (!StringUtils.isEmpty(tlError)) {
                            result.put("resp", "主人，我还没有学会这个技能，换个试试吧！");
                            result.put("type", "text");
                        }
                    }
                    /*
                     * 2、此次会话之前走过plserver语义理解，回话对象存在。
                     * 2.1 获取会话对象，遍历属性为null的语义槽，返回对应的语义槽名称 获取到该语义槽的问题，返回给用户。
                     * 2.2 填充语义槽值。
                     */
                } else {
                    // 反问用户相关语义槽的值，填充redis存储的回话对象。
                    boolean find = false; // 是否找到对应值的空槽
                    List<SemanticSlots> secondSSot = JSON.parseArray(converModel, SemanticSlots.class); // 获取用户上次模板对话内容转换 list
                    isSlotNull = false;
                    boolean isBreak = false;
                    boolean isError = false;
                    boolean isTryAgain = false;
                    for (int i = 0; i < secondSSot.size(); i++) {
                        SemanticSlots smst = secondSSot.get(i);
                        String slot_value = smst.getSlotValue();
                        //循环遍历模板将第一个为空的语意槽的问题返回。
                        if (StringUtils.isEmpty(slot_value)) {
                            // 在下一个空槽时返回问题给用户回答
                            if (find) {
                                result.put("resp", smst.getPrompt());
                                result.put("type", "text");
                                result.put("data_type", smst.getSlotName());
                                isSlotNull = true;
                                break;
                            } else {
                                //语义模版语义槽值的填充，可以修改，人名输入默认正确，日期重新请求获取。
                                if (StringUtils.isEmpty(inslot_type)) {
                                    smst.setSlotValue(voicetext);
                                } else {
                                    switch (inslot_type) {
                                        case "date":
                                            String dateString = iSlotsReplaceService.slotInputReplace(voicetext,smst.getUtterance(),inslot_type);
                                            if (dateString !=null){
                                                String pattern = "\\d{4}-\\d{2}-\\d{2}";
                                                boolean isMatch = Pattern.matches(pattern, dateString);
                                                if (isMatch) {
                                                    smst.setSlotValue(dateString);
                                                    secondSSot.add(i, smst);
                                                    secondSSot.remove(i + 1);
                                                } else {
                                                    result.put("resp", "抱歉，您提供的日期'" + voicetext + "'格式不正确，请重试日期！");
                                                    result.put("type", "try");
                                                    int try_times = smst.getTryCount();
                                                    smst.setTryCount(try_times - 1);
                                                    smst.setSlotValue("");
                                                    secondSSot.add(i, smst);
                                                    secondSSot.remove(i + 1);
                                                    isBreak = true;
                                                    if (try_times <= 0) {
                                                        result.put("resp", "抱歉，花花已经很努力了还是无法解析您提供的日期格式，本轮会话结束！");
                                                        result.put("type", "text");
                                                        isError = true;
                                                    } else {
                                                        result.put("data_type", inslot_type);
                                                        isTryAgain = true;
                                                    }
                                                }
                                            }else {
                                                result.put("resp", "抱歉，您提供的日期'" + voicetext + "'格式不正确，请重试日期！");
                                                result.put("type", "try");
                                                int try_times = smst.getTryCount();
                                                smst.setTryCount(try_times - 1);
                                                smst.setSlotValue("");
                                                secondSSot.add(i, smst);
                                                secondSSot.remove(i + 1);
                                                isBreak = true;
                                                if (try_times <= 0) {
                                                    result.put("resp", "抱歉，花花已经很努力了还是无法解析您提供的日期格式，本轮会话结束！");
                                                    result.put("type", "text");
                                                    isError = true;
                                                } else {
                                                    result.put("data_type", inslot_type);
                                                    isTryAgain = true;
                                                }
                                            }
                                            break;
                                        case "datetime":
                                            String dateTimeString = iSlotsReplaceService.slotInputReplace(voicetext, smst.getUtterance(), inslot_type);
                                            if (dateTimeString !=null){
                                                String pattern2 = "\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}";
                                                boolean isMatchDateTime = Pattern.matches(pattern2, dateTimeString);
                                                if (isMatchDateTime) {
                                                    smst.setSlotValue(dateTimeString);
                                                    secondSSot.add(i, smst);
                                                    secondSSot.remove(i + 1);
                                                } else {
                                                    result.put("resp", "抱歉，您提供的时间'" + dateTimeString + "'格式不正确，请重试日期！");
                                                    result.put("type", "try");
                                                    int try_times = smst.getTryCount();
                                                    smst.setTryCount(try_times - 1);
                                                    smst.setSlotValue("");
                                                    secondSSot.add(i, smst);
                                                    secondSSot.remove(i + 1);
                                                    isBreak = true;
                                                    if (try_times <= 0) {
                                                        result.put("resp", "抱歉，花花已经很努力了还是无法解析您提供的日期格式，本轮会话结束！");
                                                        result.put("type", "text");
                                                        isError = true;
                                                    } else {
                                                        result.put("data_type", inslot_type);
                                                        isTryAgain = true;
                                                    }
                                                }
                                            }else {
                                                result.put("resp", "抱歉，您提供的时间'" + voicetext + "'格式不正确，请重试日期！");
                                                result.put("type", "try");
                                                int try_times = smst.getTryCount();
                                                smst.setTryCount(try_times - 1);
                                                smst.setSlotValue("");
                                                secondSSot.add(i, smst);
                                                secondSSot.remove(i + 1);
                                                isBreak = true;
                                                if (try_times <= 0) {
                                                    result.put("resp", "抱歉，花花已经很努力了还是无法解析您提供的日期格式，本轮回话结束！");
                                                    result.put("type", "text");
                                                    isError = true;
                                                } else {
                                                    result.put("data_type", inslot_type);
                                                    isTryAgain = true;
                                                }
                                            }

                                            break;
                                        case "person":
                                            String tempService = smst.getTemplateService();
                                            if (tempService.equals("Phone") || tempService.equals("Message")) { //打电话,发短信人名不需要查询数据库。
                                                smst.setSlotValue(voicetext);
                                            } else {
                                                if (voicetext.contains("我")) {
                                                    smst.setSlotValue("my");
                                                    secondSSot.add(i, smst);
                                                    secondSSot.remove(i + 1);
                                                } else {
                                                    String personName = iSlotsReplaceService.slotInputReplace(voicetext, smst.getUtterance(), inslot_type);
                                                    String email_name = jdbcUtils.getEmailByCnName(personName);
                                                    if (StringUtils.isEmpty(email_name)) {
                                                        String pinyin = hanZi2PinYingUtil.getNamePinYin(personName);
                                                        List<String> unameList = hanZi2PinYingUtil.getCnNameByPinyin(pinyin);
                                                        if (unameList.size() > 1) {
                                                            result.put("resp", unameList);
                                                            result.put("content", "请选择您想要查询的人员名称！");
                                                            result.put("type", "text");
                                                            result.put("types", "list");
                                                            logger.info("出现重复人名：" + result);
                                                            isBreak = true;
                                                            isTryAgain = true;
                                                            smst.setSlotValue("");
                                                            secondSSot.add(i, smst);
                                                            secondSSot.remove(i + 1);
                                                            // 存在多人名同音问题,重新让用户选择
                                                        } else if (unameList.size() == 1) {
                                                            email_name = jdbcUtils.getEmailByCnName(unameList.get(0));
                                                            smst.setSlotValue(email_name);
                                                            secondSSot.add(i, smst);
                                                            secondSSot.remove(i + 1);
                                                        } else {
                                                            String email_name2 = jdbcUtils.getEmailByCnName(voicetext);
                                                            if (StringUtils.isEmpty(email_name2)) {
                                                                String pinyin2 = hanZi2PinYingUtil.getNamePinYin(voicetext);
                                                                List<String> unameList2 = hanZi2PinYingUtil.getCnNameByPinyin(pinyin2);
                                                                if (unameList2.size() > 1) {
                                                                    result.put("resp", unameList2);
                                                                    result.put("content", "请选择您想要查询的人员名称！");
                                                                    result.put("type", "text");
                                                                    result.put("types", "list");
                                                                    logger.info("出现重复人名：" + result);
                                                                    isBreak = true;
                                                                    isTryAgain = true;
                                                                    smst.setSlotValue("");
                                                                    secondSSot.add(i, smst);
                                                                    secondSSot.remove(i + 1);
                                                                    // 存在多人名同音问题,重新让用户选择
                                                                } else if (unameList2.size() == 1) {
                                                                    email_name2 = jdbcUtils.getEmailByCnName(unameList2.get(0));
                                                                    smst.setSlotValue(email_name2);
                                                                    secondSSot.add(i, smst);
                                                                    secondSSot.remove(i + 1);
                                                                } else {
                                                                    result.put("resp", "抱歉，没有找到关于'" + voicetext + "'的人员信息，请重试要查询的人名！");
                                                                    result.put("type", "try");
                                                                    result.put("content", voicetext);
                                                                    result.put("data_type", inslot_type);
                                                                    smst.setSlotValue("");
                                                                    int try_times = smst.getTryCount();
                                                                    smst.setTryCount(try_times - 1);
                                                                    secondSSot.add(i, smst);
                                                                    secondSSot.remove(i + 1);
                                                                    isBreak = true;
                                                                    if (try_times <= 0) {
                                                                        result.put("resp", "抱歉，花花已经很努力了还是没有找到关于'" + voicetext + "'的人员信息，本轮会话结束！");
                                                                        result.put("type", "text");
                                                                        isError = true;
                                                                    } else {
                                                                        result.put("data_type", inslot_type);
                                                                        isTryAgain = true;
                                                                    }
                                                                }
                                                            } else {
                                                                smst.setSlotValue(email_name2);
                                                                secondSSot.add(i, smst);
                                                                secondSSot.remove(i + 1);
                                                            }
                                                        }
                                                    } else {
                                                        smst.setSlotValue(email_name);
                                                        secondSSot.add(i, smst);
                                                        secondSSot.remove(i + 1);
                                                    }
                                                }
                                            }
                                            break;
                                        default:
                                            String slotValue = iSlotsReplaceService.slotInputReplace(voicetext,smst.getUtterance(),inslot_type);
                                            if (slotValue != null) {
                                                smst.setSlotValue(slotValue);
                                                secondSSot.add(i, smst);
                                                secondSSot.remove(i + 1);
                                            } else {
                                                result.put("resp", "抱歉，您提供的'" + voicetext + "'和要求的语义槽格式不匹配，请重试！");
                                                result.put("type", "try");
                                                int try_times = smst.getTryCount();
                                                smst.setTryCount(try_times - 1);
                                                smst.setSlotValue("");
                                                secondSSot.add(i, smst);
                                                secondSSot.remove(i + 1);
                                                isBreak = true;
                                                if (try_times <= 0) {
                                                    result.put("resp", "抱歉，花花已经很努力了语义槽格式还是匹配不上，本轮会话结束！");
                                                    result.put("type", "text");
                                                    isError = true;
                                                } else {
                                                    result.put("data_type", inslot_type);
                                                    isTryAgain = true;
                                                }
                                            }
                                            break;
                                    }
                                }
                                redisClient.saveSemanticModel(1, converKey, secondSSot);
                                find = true;
                            }
                        }
                        if (isBreak) {
                            break;
                        }
                    }
                    if (isError) {
                        redisClient.del(1, converKey);
                        return JSON.toJSONString(result);
                    } else if (isTryAgain || isSlotNull) {
                        return JSON.toJSONString(result);
                    }
                    // 1、所有语意槽填满以后去获取业务数据。
                    // 2、多轮对话完成获取返回数据。
                    if (!isSlotNull) {
                        //满值语意槽处理拉取数据。
                        String tempIntent = secondSSot.get(0).getTemplateIntent();
                        switch (tempIntent) {
                            case "Query": //查询服务
                                String tempService = secondSSot.get(0).getTemplateService();
                                switch (tempService){
                                    case "AirConditioner": //空调查询
                                        result = iSkillsResService.airConditioner(tempIntent, secondSSot, converKey, result);
                                        break;
                                    case "Estate": //常青花园咨询
                                        result =iSkillsResService.oneSlotSkill(tempIntent, secondSSot, result);
                                        redisClient.del(1, converKey);
                                        break;
                                    default://花样年业务数据查询
                                        result = iSkillsResService.hynManyDialogueRes(secondSSot, converKey, my_name, result);
                                }
                                break;
                            case "Saturation": //饱和度查询
                                result = iSkillsResService.hynManyDialogueRes(secondSSot, converKey, my_name, result);
                                break;
                            case "Dial": //打电话
                                result = iSkillsResService.oneSlotSkill(tempIntent, secondSSot, result);
                                redisClient.del(1, converKey);
                                break;
                            case "Complaint":
                                result = iSkillsResService.oneSlotSkill(tempIntent, secondSSot, result);
                                redisClient.del(1, converKey);
                                break;
                            case "Fix":
                                result = iSkillsResService.oneSlotSkill(tempIntent, secondSSot, result);
                                redisClient.del(1, converKey);
                                break;
                            case "Help":
                                result = iSkillsResService.oneSlotSkill(tempIntent, secondSSot, result);
                                redisClient.del(1, converKey);
                                break;
                            case "Send": //发邮件
                                result = iSkillsResService.sendMessage(secondSSot, converKey, result);
                                break;
                            case "Set": //设置提醒
                                result = iSkillsResService.reminderDoSomething(secondSSot, my_imei, result);
                                redisClient.del(1, converKey);
                                break;
                            case "Visit": //打开xx应用类服务
                                result = iSkillsResService.openApplications(tempIntent, secondSSot, result);
                                redisClient.del(1, converKey);
                                break;
                            case "Control": //设置空调
                                result = iSkillsResService.airConditioner(tempIntent, secondSSot, converKey, result);
                                break;
                            case "On": //打开空调
                                result = iSkillsResService.airConditioner(tempIntent, secondSSot, converKey, result);
                                break;
                            case "Off": //关闭空调
                                result = iSkillsResService.airConditioner(tempIntent, secondSSot, converKey, result);
                                break;
                            default: //其他情况
                                result.put("resp", "技能模板正在开发中！");
                                result.put("type", "error");
                                redisClient.del(1, converKey);
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("语意技能出错Error：" + e.getMessage());
            try {
                redisClient.del(1, converKey);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            result.put("type", "text");
            result.put("resp", "抱歉，花花没有找到您想要的，请换个试试！");
        }
            return JSON.toJSONString(result);
    }

    /**
     * 穿山甲机器人多轮回话逻辑控制。
     *
     * @param msgData 机器人翻译的汉字输入。
     * @param result  返回给机器人的结果
     * @return
     */
    @Override
    public String manisRobotMultiDialog(String msgData, JSONObject result) {
        String my_name = "manisrobot@fantasia";
        String my_imei ="abc123456789";
        String converKey = my_name+":"+my_imei;
        logger.info("Now User ModelKey：" + converKey);
        Boolean isSlotNull = false;
        try {
            String awaken = jdbcUtils.getAwakenWord();
            JSONObject msgObj = JSON.parseObject(msgData);
            String type = msgObj.getString("type"); // 返回语言/文字的类型。
            String voicetext = msgObj.getString("resp"); // 文本内容（标识语意槽修改，手动输入，值为input）
            String open_id = "robot123456789"; // 设备编号 opend_id
            String inslot_type = msgObj.getString("slot_type"); //语意槽类型。
            // 如果语音内容是 你可以做什么
            if (helpUtils.isDoSomething(voicetext)) {
                List<Function> list = jdbcUtils.getFunctions();
                result.put("resp", list);
                result.put("type", "fun");
                // 语音唤醒
            } else if (voicetext.contains(awaken)) {
                String content ="您好，主人我在，请问有什么我可以帮助您的！";
                result.put("resp", content);
                result.put("type", "text");
            } else {
                // 判断当前用户是否已经存在对话语义问答模板对象。
                String converModel = redisClient.get(1, converKey); //当前用户存储的会话模板。
                if (StringUtils.isEmpty(converModel)) {
                    JSONObject retObj = pulanAiServer.aiServer(voicetext, open_id); //请求语义理解服务。
                    logger.info("PlServer语义结果:" + retObj.getString("retObj"));
                    // 语义理解能识别
                    if (StringUtils.isEmpty(retObj.getString("error"))) {
                        JSONObject retJson = retObj.getJSONObject("retObj");
                        String service = retJson.getString("service");
                        JSONArray semanticAry;
                        JSONObject semanticObj;
                        // 判断是否存在semantic语意键
                        if (retJson.containsKey("semantic")) {
                            semanticAry = retJson.getJSONArray("semantic");
                            semanticObj = semanticAry.getJSONObject(0);
                        } else {
                            throw new Exception("semantic is null");
                        }
                        JSONObject slots = null;
                        if (semanticObj.containsKey("slots")) {
                            slots = semanticObj.getJSONArray("slots").getJSONObject(0);
                        }
                        String intent = semanticObj.getString("intent");
                        //根据意图去查询语义模版是否存在
                        Boolean isModelExist = jdbcUtils.isSemanticModelExist(service, intent);
                        if (isModelExist) {
                            List<SemanticSlots> semsot = jdbcUtils.getSemanticSlot(intent, service);
                            redisClient.saveSemanticModel(1, converKey, semsot);// 存储会话空模版
                            // 处理语义槽值, 没有任何语义槽时，直接返回第一个模板问题
                            if (slots == null || slots.isEmpty()) {
                                SemanticSlots slotObj = semsot.get(0);
                                // 语义槽为空返回第一个问题。
                                String question = slotObj.getPrompt();
                                result.put("resp", question);
                                result.put("slot_type", slotObj.getSlotName());
                                result.put("type", "robot");
                            } else {
                                List<SemanticSlots> slotList = new ArrayList<>();
                                for (SemanticSlots sst : semsot) {
                                    String slotName = sst.getSlotName();
                                    if (slots.containsKey(slotName)) {
                                        String slot_value = slots.getString(slotName);
                                        sst.setSlotValue(slot_value);
                                    }
                                    slotList.add(sst);
                                }
                                // 存储填充后的模板
                                redisClient.saveSemanticModel(1, converKey, slotList);
                                // 再次遍历模板槽，查找是否存在空的槽
                                for (SemanticSlots sst : slotList) {
                                    String jsr_value = sst.getSlotValue();
                                    // 发现空槽，返回问题，提示用户回答
                                    if (StringUtils.isEmpty(jsr_value)) {
                                        String question = sst.getPrompt();
                                        result.put("resp", question);
                                        result.put("slot_type", sst.getSlotName());
                                        result.put("type", "robot");
                                        isSlotNull = true;
                                        break;
                                    }
                                }
                                // 1、判断第一次会话对象语义槽是否有null值。
                                // 2、语意槽没有null 值 则执行获取数据的服务，返回给用户数据，并且删除之前存储的用户语意模板。
                                // 3、此处是单论对话的处理逻辑。
                                if (!isSlotNull) {
                                    String tempIntent = slotList.get(0).getTemplateIntent();//语义理解服务类型
                                    switch (tempIntent) {
                                        case "Query": //花样年查询类服务
                                            String tempService = slotList.get(0).getTemplateService();
                                            if (tempService.equals("AirConditioner")) { //空调状态查询。
                                                result = iSkillsResService.airConditioner(tempIntent, slotList, converKey, result);
                                            } else {
                                                result = iSkillsResService.manisRobotSingleDialogueRes(slotList, converKey,result);
                                            }
                                            break;
                                        case "Saturation":
                                            result = iSkillsResService.manisRobotSingleDialogueRes(slotList, converKey, result);
                                            break;
                                        case "Dial":  //打电话服务
                                            result = iSkillsResService.oneSlotSkill(tempIntent, slotList, result);
                                            redisClient.del(1, converKey);
                                            break;
                                        case "Set": //设置提醒 当前imei号作为别名。
                                            result = iSkillsResService.reminderDoSomething(slotList, my_imei, result);
                                            redisClient.del(1, converKey);
                                            break;
                                        case "Visit": //打开xx应用类服务
                                            result = iSkillsResService.openApplications(tempIntent, slotList, result);
                                            redisClient.del(1, converKey);
                                            break;
                                        case "Booking": //预定会议室
                                            result = iSkillsResService.oneSlotSkill(tempIntent, slotList, result);
                                            redisClient.del(1, converKey);
                                            break;
                                        case "On": //打开空调
                                            result = iSkillsResService.airConditioner(tempIntent, slotList, converKey, result);
                                            break;
                                        case "Off": //关闭空调
                                            result = iSkillsResService.airConditioner(tempIntent, slotList, converKey, result);
                                            break;
                                        case "Control": //设置空调
                                            result = iSkillsResService.airConditioner(tempIntent, slotList, converKey, result);
                                            break;
                                        default: //没有匹配的模板
                                            result.put("resp", "技能模板正在开发中！");
                                            result.put("type", "error");
                                            redisClient.del(1, converKey);
                                            break;
                                    }
                                }
                            }
                        } else {
                            //找不到相符合的语义模版,调用图灵机器人。
                            redisClient.del(1, converKey);
                            result.put("resp", "");
                            result.put("error", "0000");
                        }
                        // 语义识别理解不了的情况处理。
                    } else {
                        // 语意理解错误处理。
                        redisClient.del(1, converKey);
                        result.put("resp", "");
                        result.put("error", "0000");
                    }
                    /**
                     * 2、此次会话之前走过plserver语义理解，回话对象存在。
                     * 2.1 获取会话对象，遍历属性为null的语义槽，返回对应的语义槽名称 获取到该语义槽的问题，返回给用户。
                     * 2.2 填充语义槽值。
                     */
                } else {
                    // 反问用户相关语义槽的值，填充redis存储的回话对象。
                    boolean find = false; // 是否找到对应值的空槽
                    List<SemanticSlots> secondSSot = JSON.parseArray(converModel, SemanticSlots.class); // 获取用户上次模板对话内容转换 list
                    isSlotNull = false;
                    boolean isBreak = false;
                    boolean isError = false;
                    boolean isTryAgain = false;
                    for (int i = 0; i < secondSSot.size(); i++) {
                        SemanticSlots smst = secondSSot.get(i);
                        String slot_value = smst.getSlotValue();
                        //循环遍历模板将第一个为空的语意槽的问题返回。
                        if (StringUtils.isEmpty(slot_value)) {
                            // 在下一个空槽时返回问题给用户回答
                            if (find) {
                                result.put("resp", smst.getPrompt());
                                result.put("type", "robot");
                                result.put("slot_type", smst.getSlotName());
                                isSlotNull = true;
                                break;
                            } else {
                                //语义模版语义槽值的填充，可以修改，人名输入默认正确，日期重新请求获取。
                                if (StringUtils.isEmpty(inslot_type)) {
                                    smst.setSlotValue(voicetext);
                                } else {
                                    switch (inslot_type) {
                                        case "date":
                                            String dateString = iSlotsReplaceService.slotInputReplace(voicetext,smst.getUtterance(),inslot_type);
                                            if (dateString !=null){
                                                String pattern = "\\d{4}-\\d{2}-\\d{2}";
                                                boolean isMatch = Pattern.matches(pattern, dateString);
                                                if (isMatch) {
                                                    smst.setSlotValue(dateString);
                                                    secondSSot.add(i, smst);
                                                    secondSSot.remove(i + 1);
                                                } else {
                                                    result.put("resp", "抱歉，您提供的日期'" + voicetext + "'格式不正确，请重试日期！");
                                                    result.put("type", "try");
                                                    int try_times = smst.getTryCount();
                                                    smst.setTryCount(try_times - 1);
                                                    smst.setSlotValue("");
                                                    secondSSot.add(i, smst);
                                                    secondSSot.remove(i + 1);
                                                    isBreak = true;
                                                    if (try_times <= 0) {
                                                        result.put("resp", "抱歉，花花已经很努力了还是无法解析您提供的日期格式，本轮会话结束！");
                                                        result.put("type", "text");
                                                        isError = true;
                                                    } else {
                                                        result.put("slot_type", inslot_type);
                                                        isTryAgain = true;
                                                    }
                                                }
                                            }else {
                                                result.put("resp", "抱歉，您提供的日期'" + voicetext + "'格式不正确，请重试日期！");
                                                result.put("type", "try");
                                                int try_times = smst.getTryCount();
                                                smst.setTryCount(try_times - 1);
                                                smst.setSlotValue("");
                                                secondSSot.add(i, smst);
                                                secondSSot.remove(i + 1);
                                                isBreak = true;
                                                if (try_times <= 0) {
                                                    result.put("resp", "抱歉，花花已经很努力了还是无法解析您提供的日期格式，本轮会话结束！");
                                                    result.put("type", "text");
                                                    isError = true;
                                                } else {
                                                    result.put("slot_type", inslot_type);
                                                    isTryAgain = true;
                                                }
                                            }
                                            break;
                                        case "datetime":
                                            String dateTimeString = iSlotsReplaceService.slotInputReplace(voicetext, smst.getUtterance(), inslot_type);
                                            if (dateTimeString !=null){
                                                String pattern2 = "\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}";
                                                boolean isMatchDateTime = Pattern.matches(pattern2, dateTimeString);
                                                if (isMatchDateTime) {
                                                    smst.setSlotValue(dateTimeString);
                                                    secondSSot.add(i, smst);
                                                    secondSSot.remove(i + 1);
                                                } else {
                                                    result.put("resp", "抱歉，您提供的时间'" + dateTimeString + "'格式不正确，请重试日期！");
                                                    result.put("type", "try");
                                                    int try_times = smst.getTryCount();
                                                    smst.setTryCount(try_times - 1);
                                                    smst.setSlotValue("");
                                                    secondSSot.add(i, smst);
                                                    secondSSot.remove(i + 1);
                                                    isBreak = true;
                                                    if (try_times <= 0) {
                                                        result.put("resp", "抱歉，花花已经很努力了还是无法解析您提供的日期格式，本轮会话结束！");
                                                        result.put("type", "text");
                                                        isError = true;
                                                    } else {
                                                        result.put("slot_type", inslot_type);
                                                        isTryAgain = true;
                                                    }
                                                }
                                            }else {
                                                result.put("resp", "抱歉，您提供的时间'" + voicetext + "'格式不正确，请重试日期！");
                                                result.put("type", "try");
                                                int try_times = smst.getTryCount();
                                                smst.setTryCount(try_times - 1);
                                                smst.setSlotValue("");
                                                secondSSot.add(i, smst);
                                                secondSSot.remove(i + 1);
                                                isBreak = true;
                                                if (try_times <= 0) {
                                                    result.put("resp", "抱歉，花花已经很努力了还是无法解析您提供的日期格式，本轮会话结束！");
                                                    result.put("type", "text");
                                                    isError = true;
                                                } else {
                                                    result.put("slot_type", inslot_type);
                                                    isTryAgain = true;
                                                }
                                            }

                                            break;
                                        case "person":
                                            String tempService = smst.getTemplateService();
                                            if (tempService.equals("Phone") || tempService.equals("Message")) { //打电话,发短信人名不需要查询数据库。
                                                smst.setSlotValue(voicetext);
                                            } else {
                                                if (voicetext.contains("我")) {
                                                    smst.setSlotValue("my");
                                                    secondSSot.add(i, smst);
                                                    secondSSot.remove(i + 1);
                                                } else {
                                                    String personName = iSlotsReplaceService.slotInputReplace(voicetext, smst.getUtterance(), inslot_type);
                                                    String email_name = jdbcUtils.getEmailByCnName(personName);
                                                    if (StringUtils.isEmpty(email_name)) {
                                                        String pinyin = hanZi2PinYingUtil.getNamePinYin(personName);
                                                        List<String> unameList = hanZi2PinYingUtil.getCnNameByPinyin(pinyin);
                                                        if (unameList.size() > 1) {
                                                            result.put("resp", unameList);
                                                            result.put("content", "请选择您想要查询的人员名称！");
                                                            result.put("type", "text");
                                                            result.put("types", "list");
                                                            logger.info("出现重复人名：" + result);
                                                            isBreak = true;
                                                            isTryAgain = true;
                                                            smst.setSlotValue("");
                                                            secondSSot.add(i, smst);
                                                            secondSSot.remove(i + 1);
                                                            // 存在多人名同音问题,重新让用户选择
                                                        } else if (unameList.size() == 1) {
                                                            email_name = jdbcUtils.getEmailByCnName(unameList.get(0));
                                                            smst.setSlotValue(email_name);
                                                            secondSSot.add(i, smst);
                                                            secondSSot.remove(i + 1);
                                                        } else {
                                                            String email_name2 = jdbcUtils.getEmailByCnName(voicetext);
                                                            if (StringUtils.isEmpty(email_name2)) {
                                                                String pinyin2 = hanZi2PinYingUtil.getNamePinYin(voicetext);
                                                                List<String> unameList2 = hanZi2PinYingUtil.getCnNameByPinyin(pinyin2);
                                                                if (unameList2.size() > 1) {
                                                                    result.put("resp", unameList2);
                                                                    result.put("content", "请选择您想要查询的人员名称！");
                                                                    result.put("type", "text");
                                                                    result.put("types", "list");
                                                                    logger.info("出现重复人名：" + result);
                                                                    isBreak = true;
                                                                    isTryAgain = true;
                                                                    smst.setSlotValue("");
                                                                    secondSSot.add(i, smst);
                                                                    secondSSot.remove(i + 1);
                                                                    // 存在多人名同音问题,重新让用户选择
                                                                } else if (unameList2.size() == 1) {
                                                                    email_name2 = jdbcUtils.getEmailByCnName(unameList2.get(0));
                                                                    smst.setSlotValue(email_name2);
                                                                    secondSSot.add(i, smst);
                                                                    secondSSot.remove(i + 1);
                                                                } else {
                                                                    result.put("resp", "抱歉，没有找到关于'" + voicetext + "'的人员信息，请重试要查询的人名！");
                                                                    result.put("type", "try");
                                                                    result.put("content", voicetext);
                                                                    result.put("slot_type", inslot_type);
                                                                    smst.setSlotValue("");
                                                                    int try_times = smst.getTryCount();
                                                                    smst.setTryCount(try_times - 1);
                                                                    secondSSot.add(i, smst);
                                                                    secondSSot.remove(i + 1);
                                                                    isBreak = true;
                                                                    if (try_times <= 0) {
                                                                        result.put("resp", "抱歉，花花已经很努力了还是没有找到关于'" + voicetext + "'的人员信息，本轮会话结束！");
                                                                        result.put("type", "text");
                                                                        isError = true;
                                                                    } else {
                                                                        result.put("slot_type", inslot_type);
                                                                        isTryAgain = true;
                                                                    }
                                                                }
                                                            } else {
                                                                smst.setSlotValue(email_name2);
                                                                secondSSot.add(i, smst);
                                                                secondSSot.remove(i + 1);
                                                            }
                                                        }
                                                    } else {
                                                        smst.setSlotValue(email_name);
                                                        secondSSot.add(i, smst);
                                                        secondSSot.remove(i + 1);
                                                    }
                                                }
                                            }
                                            break;
                                        default:
                                            String slotValue = iSlotsReplaceService.slotInputReplace(voicetext,smst.getUtterance(),inslot_type);
                                            if (slotValue != null) {
                                                smst.setSlotValue(slotValue);
                                                secondSSot.add(i, smst);
                                                secondSSot.remove(i + 1);
                                            } else {
                                                result.put("resp", "抱歉，您提供的'" + voicetext + "'和要求的格式不相符，请重试！");
                                                result.put("type", "try");
                                                int try_times = smst.getTryCount();
                                                smst.setTryCount(try_times - 1);
                                                smst.setSlotValue("");
                                                secondSSot.add(i, smst);
                                                secondSSot.remove(i + 1);
                                                isBreak = true;
                                                if (try_times <= 0) {
                                                    result.put("resp", "抱歉，花花已经很努力了还是不能理解您提供的内容，本轮会话结束！");
                                                    result.put("type", "text");
                                                    isError = true;
                                                } else {
                                                    result.put("slot_type", inslot_type);
                                                    isTryAgain = true;
                                                }
                                            }
                                            break;
                                    }
                                }
                                redisClient.saveSemanticModel(1, converKey, secondSSot);
                                find = true;
                            }
                        }
                        if (isBreak) {
                            break;
                        }
                    }
                    if (isError) {
                        redisClient.del(1, converKey);
                        return JSON.toJSONString(result);
                    } else if (isTryAgain || isSlotNull) {
                        return JSON.toJSONString(result);
                    }
                    // 1、所有语意槽填满以后去获取业务数据。
                    // 2、多轮对话完成获取返回数据。
                    if (!isSlotNull) {
                        //满值语意槽处理拉取数据。
                        String tempIntent = secondSSot.get(0).getTemplateIntent();
                        switch (tempIntent) {
                            case "Query": //查询服务
                                String tempService = secondSSot.get(0).getTemplateService();
                                switch (tempService){
                                    case "AirConditioner": //空调查询
                                        result = iSkillsResService.airConditioner(tempIntent, secondSSot, converKey, result);
                                        break;
                                    case "Estate": //常青花园咨询
                                        result =iSkillsResService.oneSlotSkill(tempIntent, secondSSot, result);
                                        redisClient.del(1, converKey);
                                        break;
                                    default://花样年业务数据查询
                                        result = iSkillsResService.manisRobotManyDialogueRes(secondSSot, converKey, result);
                                }
                                break;
                            case "Saturation": //饱和度查询
                                result = iSkillsResService.manisRobotManyDialogueRes(secondSSot, converKey, result);
                                break;
                            case "Dial": //打电话
                                result = iSkillsResService.oneSlotSkill(tempIntent, secondSSot, result);
                                redisClient.del(1, converKey);
                                break;
                            case "Complaint":
                                result = iSkillsResService.oneSlotSkill(tempIntent, secondSSot, result);
                                redisClient.del(1, converKey);
                                break;
                            case "Fix":
                                result = iSkillsResService.oneSlotSkill(tempIntent, secondSSot, result);
                                redisClient.del(1, converKey);
                                break;
                            case "Help":
                                result = iSkillsResService.oneSlotSkill(tempIntent, secondSSot, result);
                                redisClient.del(1, converKey);
                                break;
                            case "Send": //发邮件
                                result = iSkillsResService.sendMessage(secondSSot, converKey, result);
                                break;
                            case "Set": //设置提醒
                                result = iSkillsResService.reminderDoSomething(secondSSot, my_imei, result);
                                redisClient.del(1, converKey);
                                break;
                            case "Visit": //打开xx应用类服务
                                result = iSkillsResService.openApplications(tempIntent, secondSSot, result);
                                redisClient.del(1, converKey);
                                break;
                            case "Control": //设置空调
                                result = iSkillsResService.airConditioner(tempIntent, secondSSot, converKey, result);
                                break;
                            case "On": //打开空调
                                result = iSkillsResService.airConditioner(tempIntent, secondSSot, converKey, result);
                                break;
                            case "Off": //关闭空调
                                result = iSkillsResService.airConditioner(tempIntent, secondSSot, converKey, result);
                                break;
                            default: //其他情况
                                result.put("resp", "技能模板正在开发中！");
                                result.put("type", "error");
                                redisClient.del(1, converKey);
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("语意技能出错Error：" + e.getMessage());
            try {
                result.put("type", "text");
                result.put("resp", "抱歉，花花没有找到您想要的换个试试！");
                redisClient.del(1, converKey);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return JSON.toJSONString(result);
    }


}
