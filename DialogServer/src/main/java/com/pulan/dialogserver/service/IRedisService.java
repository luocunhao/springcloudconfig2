package com.pulan.dialogserver.service;

import com.alibaba.fastjson.JSONObject;

public interface IRedisService {
    /**
     * 加密opend_id
     * @param open_id
     * @return
     */
    String EncodeOpenID(String open_id);

    /**
     * 解密opend_id
     * @param md5ID
     * @return
     */
    String DecodeOpenID(String md5ID);

    /**
     * reids远程服务调用
     * @param rpcServer
     * @param rpcParam
     * @param timeout
     * @return
     */
    JSONObject rpcCall(String rpcServer, JSONObject rpcParam, int timeout);

    /**
     * rpc服务进程，用于微信公众号机器人服务。
     * @param rpcServer
     * @param rpcProc
     */
    void rpcServerRun(String rpcServer, IRPCProc rpcProc);

}
