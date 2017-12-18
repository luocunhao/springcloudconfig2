package com.pulan.dialogserver.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.pulan.dialogserver.config.SysConfig;
import com.pulan.dialogserver.service.IRPCProc;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MsgWork implements IRPCProc {

    private Logger logger = LogManager.getLogger(MsgWork.class);
    @Autowired
    private SysConfig sysConfig;
    @Override
    public JSONObject rpcProcess(JSONObject rpcParam) {

        return null;
    }
}
