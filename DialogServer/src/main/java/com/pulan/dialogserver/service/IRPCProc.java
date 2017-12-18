package com.pulan.dialogserver.service;

import com.alibaba.fastjson.JSONObject;

/**
 * 定期从redis 消息通道取出消息接口。
 */
public interface IRPCProc {
    JSONObject rpcProcess(JSONObject rpcParam);
}
