package com.zxn.netease.nimsdk.business.session.module;

import com.zxn.netease.nimsdk.api.model.CreateMessageCallback;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.List;

public interface IMultiRetweetMsgCreator {
    void create(List<IMMessage> msgList, boolean shouldEncrypt,  CreateMessageCallback callback);
}
