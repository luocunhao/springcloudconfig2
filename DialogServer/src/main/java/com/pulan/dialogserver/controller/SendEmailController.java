package com.pulan.dialogserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pulan.dialogserver.service.IMailService;
import com.pulan.dialogserver.shiro.entity.User;
import com.pulan.dialogserver.utils.DESPlus;
import com.pulan.dialogserver.utils.RedisClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/message")
public class SendEmailController {

    private Logger logger = LogManager.getLogger(SendEmailController.class);

    private IMailService iMailService;
    private RedisClient redisClient;
    @Autowired
    public SendEmailController(IMailService iMailService,RedisClient redisClient){
        this.iMailService =iMailService;
        this.redisClient =redisClient;
    }
    DESPlus desPlus = new DESPlus("aec504733cfb4112");

    @ResponseBody
    @RequestMapping(value = "sendemail",method = RequestMethod.GET)
    public String sendEmail(@RequestParam Boolean isSend,HttpServletRequest request){
        JSONObject result = new JSONObject();
        User user = (User) WebUtils.getSessionAttribute(request, "user");
        if (user == null) {
            result.put("error", "session过期");
            return result.toJSONString();
        }
        try {
            String coverKey = "email:" + user.getEmail() + ":" + user.getImei();
            if (isSend) {
                JSONObject jsonObject = JSON.parseObject(redisClient.get(1, coverKey));
                String userName = user.getMail_name();
                String passWord = desPlus.decrypt(user.getPassword());
                boolean flag = iMailService.sendHtmlMail(jsonObject, userName, passWord);
                redisClient.del(1, coverKey);
                result.put("type", "text");
                if (flag) {
                    result.put("resp", "邮件发送成功！");
                    result.put("type", "text");
                } else {
                    result.put("resp", "邮件发送失败,请重试！");
                    result.put("type", "error");
                }
            } else {
                redisClient.del(1, coverKey);
                result.put("resp", "已取消发送该邮件！");
                result.put("type", "text");
            }
        } catch (Exception e) {
            logger.error("Send Email Failed!" + e.getMessage());
        }
        return result.toJSONString();
    }
}
