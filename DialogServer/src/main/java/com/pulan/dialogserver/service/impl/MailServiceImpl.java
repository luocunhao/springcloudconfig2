package com.pulan.dialogserver.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.pulan.dialogserver.service.IMailService;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class MailServiceImpl implements IMailService {
    private Logger logger = LogManager.getLogger(MailServiceImpl.class);

    @Override
    public Boolean sendHtmlMail(JSONObject respObj, String my_name, String password) {
        if (respObj ==null || "".equals(my_name)){
            return false;
        }
        Boolean flag =false;
        try{
            //String address =respObj.getString("address");
            String myaddress ="mm2468579@163.com";
            String subject =respObj.getString("subject");
            String content =respObj.getString("content");
            ExchangeService excService =new ExchangeService(ExchangeVersion.Exchange2010_SP1); //新建server版本
            ExchangeCredentials credentials =new WebCredentials(my_name, password);  //用户名，密码，域名
            excService.setCredentials(credentials);
            excService.setUrl(new URI("https://mail.cnfantasia.com/EWS/Exchange.asmx")); //outlook.spacex.com 改为自己的邮箱服务器地址
            EmailMessage msg =new EmailMessage(excService);
            msg.setSubject(subject); //邮件主题
            msg.setBody(MessageBody.getMessageBodyFromText(content)); //邮件内容
            msg.getToRecipients().add(myaddress); //收件人
            //msg.getCcRecipients().add("test2@test.com"); //抄送人
            //msg.getAttachments().addFileAttachment("D:\\Downloads\\EWSJavaAPI_1.2\\EWSJavaAPI_1.2\\Getting started with EWS Java API.RTF"); //附件
            msg.send();
            flag =true;
            logger.info("Send Email Success!"+flag);
        }catch (Exception e){
            logger.error("Send Email Error："+e.getMessage());
        }
        return flag;
    }
}
