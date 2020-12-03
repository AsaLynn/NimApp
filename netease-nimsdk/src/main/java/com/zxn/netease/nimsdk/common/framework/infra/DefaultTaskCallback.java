package com.zxn.netease.nimsdk.common.framework.infra;

public interface DefaultTaskCallback {
    void onFinish(String key, int result, Object attachment);
}