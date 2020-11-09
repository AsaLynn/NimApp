package com.zxn.netease.nimsdk.business.session.module

import com.netease.nimlib.sdk.msg.model.IMMessage
import com.zxn.netease.nimsdk.api.model.CreateMessageCallback

interface IMultiRetweetMsgCreator {
    fun create(msgList: List<IMMessage?>?, shouldEncrypt: Boolean, callback: CreateMessageCallback?)
}