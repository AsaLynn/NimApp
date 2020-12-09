package com.netease.nim.demo.session.extension

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment
import com.netease.nimlib.sdk.msg.attachment.MsgAttachmentParser

/**
 * 自定义消息解析器.
 */
class CustomAttachParser : MsgAttachmentParser {

    override fun parse(json: String): MsgAttachment {
        var attachment: CustomAttachment? = null
        try {
            val `object` = JSON.parseObject(json)
            val type = `object`.getInteger(KEY_TYPE)
            val data = `object`.getJSONObject(KEY_DATA)
            attachment = when (type) {
                CustomAttachmentType.Guess -> GuessAttachment()
                CustomAttachmentType.SnapChat -> return SnapChatAttachment(data)
                CustomAttachmentType.Sticker -> StickerAttachment()
                CustomAttachmentType.RedPacket -> RedPacketAttachment()
                CustomAttachmentType.OpenedRedPacket -> RedPacketOpenedAttachment()
                CustomAttachmentType.MultiRetweet -> MultiRetweetAttachment()
                else -> DefaultCustomAttachment()
            }
            attachment.fromJson(data)
        } catch (e: Exception) {
        }
        return attachment!!
    }

    companion object {
        private const val KEY_TYPE = "type"
        private const val KEY_DATA = "data"
        @JvmStatic
        fun packData(type: Int, data: JSONObject?): String {
            val `object` = JSONObject()
            `object`[KEY_TYPE] = type
            if (data != null) {
                `object`[KEY_DATA] = data
            }
            return `object`.toJSONString()
        }
    }
}