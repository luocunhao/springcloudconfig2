package com.pulan.dialogserver.controller;

import com.alibaba.fastjson.JSONObject;
import com.pulan.dialogserver.service.IFlyService;
import com.pulan.dialogserver.service.ISlotsReplaceService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

/**
 * 1、app上传语言文件，调用讯飞语音听写转换成文字。
 * 2、调用pulan ai 语义理解提取替换人名和时间。
 */
@RestController
@RequestMapping("/message")
public class Voice2TextController {
    private Logger logger = LogManager.getLogger(Voice2TextController.class.getName());
    //讯飞服务
    @Autowired
    private IFlyService iFlyService;
    @Autowired
    private ISlotsReplaceService iSlotsReplaceService;

    @ResponseBody
    @RequestMapping(value = "dplservervoice", method = RequestMethod.POST)
    public JSONObject sendVoiceMessage(@RequestParam("filename") MultipartFile file, HttpServletRequest request) {
        String err = "";
        String target = "";
        JSONObject voiceObj =new JSONObject();
        JSONObject result = new JSONObject();
        result.put("resp", "");
        result.put("error", "");
        String strDataType = "";
        // 语音上传服务器+语音转文字
        if (!file.isEmpty()) {
            try {
                strDataType = file.getOriginalFilename();
                logger.info("语音请求类型:" + strDataType + ",size:" + strDataType.length());
                File dir = new File("");
                String voicePath = dir.getAbsolutePath() + "/voice/appvoice";
                File folder = new File(voicePath);
                if (folder.exists() && folder.isDirectory()) {
                } else {
                    folder.mkdirs();
                }
                String voiceFile = voicePath + "/" + UUID.randomUUID().toString() + ".amr";
                byte[] bytes = file.getBytes();
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(voiceFile)));
                stream.write(bytes);
                stream.flush();
                stream.close();
                // 格式转换amr -> wav
                target = voiceFile.substring(0, voiceFile.length() - 3) + "wav";
                Process process = Runtime.getRuntime().exec("ffmpeg -i " + voiceFile + " " + target);
                process.waitFor();
                // 上传完成，录音文件已保存到 voiceFile，准备语音识别、语义解析, 将返回结果填充到 result 变量中，最后将
                // voiceFile 文件删除。
                // 先做语音识别
                voiceObj =iFlyService.voice2Text(target);
            } catch (Exception e) {
                err = "failed to upload " + file.getOriginalFilename() + " => " + e.getMessage();
                result.put("error", err);
            }
        } else {
            err = "failed to upload " + file.getOriginalFilename() + " because the file was empty.";
            result.put("error", err);
        }
        String resp =voiceObj.getString("retObj");
        if (!StringUtils.isEmpty(resp)) {
            result =iSlotsReplaceService.slotReplace(resp,strDataType,"",result);
            return result;
        } else {
            String errorStr = voiceObj.getString("error");
            result.put("error", "Failed :Voice to Text error!:"+errorStr);
            return result;
        }
    }

    // 语音转文字接口测试。
   /* @ResponseBody
    @RequestMapping(value = "tdplservervoice", method = RequestMethod.POST)
    private JSONObject testVoice(@RequestBody String msgBody){
        JSONObject restObj =JSON.parseObject(msgBody);
        String voiceUrl =restObj.getString("url");
       JSONObject  voiceObj =iFlyService.voice2Text(voiceUrl);
       return voiceObj;
    }*/

}
