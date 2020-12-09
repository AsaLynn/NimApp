package com.netease.nim.demo.session.extension

import com.alibaba.fastjson.JSONObject
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment
/**
 *  自定义消息附件基类
 *  Created by zxn on 2020/12/9.
 */
abstract class CustomAttachment internal constructor(var type: Int) : MsgAttachment {

    fun fromJson(data: JSONObject?) {
        data?.let { parseData(it) }
    }

    override fun toJson(send: Boolean): String {
        return CustomAttachParser.packData(type, packData())
    }

    protected abstract fun parseData(data: JSONObject?)

    protected abstract fun packData(): JSONObject?
}