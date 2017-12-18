package com.pulan.dialogserver.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pulan.dialogserver.service.IRPCProc;
import com.pulan.dialogserver.service.IRedisService;
import com.pulan.dialogserver.utils.RedisClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

@Service
public class RedisServiceImpl implements IRedisService {

    public static final String RPC_CLI_FIELD = "RPC-Client";
    public static final int RPC_SVR_TIMEOUT = 1800;
    private Logger logger = LogManager.getLogger(RedisServiceImpl.class);

    @Autowired
    private RedisClient redisClient;

    // 将微信 open_id 加密成仅包含字母和数字的串
    // 正确返回加密串，出错返回空串
    @Override
    public String EncodeOpenID(String open_id) {
        MessageDigest mDigest;
        String ret = "";
        try {
            mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(open_id.getBytes());
            ret = new BigInteger(1, mDigest.digest()).toString(16);
            redisClient.set(0,ret, open_id);
            redisClient.expire(1,ret, 1800);
        } catch (Exception e) {
            org.apache.log4j.Logger.getLogger(RedisClient.class).error("加密open_id出错：" + e.getMessage());
        }
        return ret;
    }

    // 找到加密串所对应的原始 open_id
    // 正确返回加密串所对饮的原始串，出错返回空串
    @Override
    public String DecodeOpenID(String md5ID) {
        String ret = "";
        try {
            ret = redisClient.get(0,md5ID);
        } catch (Exception e) {
            logger.error("获取原始open_id出错：" + e.getMessage());
        }
        return ret;
    }


    @Override
    public JSONObject rpcCall(String rpcServer, JSONObject rpcParam, int timeout) {
        JSONObject result = new JSONObject();
        result.put("retObj", null);
        result.put("error", "");
        try {
            String client = UUID.randomUUID().toString();
            rpcParam.put(RPC_CLI_FIELD, client);
            String ifly_stt = JSON.toJSONString(rpcParam);
            redisClient.lpush(rpcServer, ifly_stt);
            List<String> ret = redisClient.brpop(client, timeout);
            if (ret.size() < 1) {
                org.apache.log4j.Logger.getLogger(RedisClient.class).error("RPC No Response");
                result.put("error", "RPC No Response");
            } else {
                result = JSON.parseObject(ret.get(1).replaceAll("&"," "));
            }
        } catch(Exception e) {
            result.put("error", e.getMessage());
        }
        return result;
    }

    @Override
    public void rpcServerRun(String rpcServer, IRPCProc rpcProc) {
        while (true) {
            try {
                List<String> ret = redisClient.brpop(rpcServer, RPC_SVR_TIMEOUT);
                if (ret.size() < 1) continue;
                JSONObject rpcParam = JSON.parseObject(ret.get(1));
                String rpcClient = rpcParam.getString(RPC_CLI_FIELD);
                if (null == rpcClient || rpcClient.equals("")) {
                    logger.warn("rpc param missing client field's value");
                    continue;
                }
                JSONObject rpcResult = rpcProc.rpcProcess(rpcParam);
                redisClient.lpush(rpcClient, JSON.toJSONString(rpcResult));
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }
}
