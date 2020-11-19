package com.zxn.netease.nimsdk.api.model

import com.netease.nimlib.sdk.msg.model.IMMessage

interface CreateMessageCallback {
    fun onFinished(message: IMMessage)
    fun onFailed(code: Int)
    fun onException(exception: Throwable)

    companion object {
        //不支持的消息类型
        const val FAILED_CODE_NOT_SUPPORT = 1
    }
}