package com.pulan.dialogserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pulan.dialogserver.service.IMultiDialogService;
import com.pulan.dialogserver.shiro.entity.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * 文本语义接口。
 */
@RestController
@RequestMapping("/message")
public class AppMultiDialogController {

    private Logger logger = LogManager.getLogger(AppMultiDialogController.class);

    @Autowired
    private IMultiDialogService iMultiDialogService;

    @ResponseBody
    @RequestMapping(value = "dplservertext", method = RequestMethod.POST)
    public String sendTextMessage(@RequestBody String msgData, HttpServletRequest request) {
        //msgData 为null。
        logger.info("SendText start .." + msgData);
        JSONObject result = new JSONObject();
        result.put("resp", "");
        result.put("error", "");
        HttpSession session = request.getSession(false);
        if (session == null) {
            result.put("error", "session过期");
            return JSON.toJSONString(result);
        }
        User user = (User) session.getAttribute("user");
        logger.info("UserRequestSession:"+user);
        String my_name = user.getEmail();
        String my_imei = user.getImei();
        /*String my_name = "litao@163.com";
		String my_imei = "253896"*/;
        String respResult = iMultiDialogService.appMultiDialog(msgData,my_imei,my_name,result);
       return  respResult;
    }

}
