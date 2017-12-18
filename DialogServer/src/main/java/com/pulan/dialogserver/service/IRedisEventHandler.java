package com.pulan.dialogserver.service;

/**
 * Redis过期Key回调接口。
 */
public interface IRedisEventHandler {
    void onEvent(String channel, String message);
}
