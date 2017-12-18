package com.pulan.dialogserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SysConfig {

    //微信消息通道
    @Value("${wx_channel}")
    private String rpcServer;
    //文本长度限制转语音
    @Value("${text.size}")
    private int textSize;
    //图灵API地址
    @Value("${robbot.api}")
    private String robbotAPI;
    //图灵apiKey
    @Value("${robbot.apiKey}")
    private String robbotApiKey;
    //上传语音文件路径
    @Value("${voice_url}")
    private String voice_url;
    //pulan 语义理解 api
    @Value("${plserver.api}")
    private String pulan_api;
    //pulan slot 提取服务 api。
    @Value("${plslotserver.api}")
    private String plSlotApi;
    //pulan 语义 apikey
    @Value("${plserver.apikey}")
    private String pulan_apikey;
    @Value("${ifly_channel}")
    private String ifly_channel;
    //语意匹配度
    @Value("${rc_value}")
    private Double rc_val;
    public SysConfig(){

    }

    public String getRpcServer() {
        return rpcServer;
    }

    public void setRpcServer(String rpcServer) {
        this.rpcServer = rpcServer;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public String getRobbotAPI() {
        return robbotAPI;
    }

    public void setRobbotAPI(String robbotAPI) {
        this.robbotAPI = robbotAPI;
    }

    public String getRobbotApiKey() {
        return robbotApiKey;
    }

    public void setRobbotApiKey(String robbotApiKey) {
        this.robbotApiKey = robbotApiKey;
    }

    public String getVoice_url() {
        return voice_url;
    }

    public void setVoice_url(String voice_url) {
        this.voice_url = voice_url;
    }

    public String getPulan_api() {
        return pulan_api;
    }

    public void setPulan_api(String pulan_api) {
        this.pulan_api = pulan_api;
    }

    public String getPulan_apikey() {
        return pulan_apikey;
    }

    public void setPulan_apikey(String pulan_apikey) {
        this.pulan_apikey = pulan_apikey;
    }

    public String getIfly_channel() {
        return ifly_channel;
    }

    public void setIfly_channel(String ifly_channel) {
        this.ifly_channel = ifly_channel;
    }

    public String getPlSlotApi() {
        return plSlotApi;
    }

    public void setPlSlotApi(String plSlotApi) {
        this.plSlotApi = plSlotApi;
    }

    public Double getRc_val() {
        return rc_val;
    }

    public void setRc_val(Double rc_val) {
        this.rc_val = rc_val;
    }
}
