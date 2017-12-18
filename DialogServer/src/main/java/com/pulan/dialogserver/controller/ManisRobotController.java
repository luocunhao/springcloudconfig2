package com.pulan.dialogserver.controller;

import com.alibaba.fastjson.JSONObject;
import com.pulan.dialogserver.service.IMultiDialogService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/message")
public class ManisRobotController {
    private static final Logger logger = LogManager.getLogger(ManisRobotController.class);

    @Autowired
    private IMultiDialogService iMultiDialogService;

    @RequestMapping(value = "manisdplservertext", method = RequestMethod.GET)
    public String robotTextRequest(HttpServletRequest request){
        String msgData =request.getParameter("msgdata");
        String callback =request.getParameter("callback");
        logger.info("ManisRobot start .." + msgData);
        JSONObject result = new JSONObject();
        result.put("resp", "");
        result.put("error", "");
        String respResult = iMultiDialogService.manisRobotMultiDialog(msgData,result);
        String results =callback+"(%s)";
        respResult= String.format(results,respResult);
        logger.info("返回结果："+respResult);
        return respResult;
    }

}
