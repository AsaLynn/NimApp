package com.netease.nim.demo.session.extension

import com.alibaba.fastjson.JSONObject

class RedPacketAttachment : CustomAttachment(CustomAttachmentType.RedPacket) {
    var rpContent //  消息文本内容
            : String? = null
    var rpId //  红包id
            : String? = null
    var rpTitle // 红包名称
            : String? = null

    override fun parseData(data: JSONObject?) {
        rpContent = data!!.getString(KEY_CONTENT)
        rpId = data.getString(KEY_ID)
        rpTitle = data.getString(KEY_TITLE)
    }

    override fun packData(): JSONObject? {
        val data = JSONObject()
        data[KEY_CONTENT] = rpContent
        data[KEY_ID] = rpId
        data[KEY_TITLE] = rpTitle
        return data
    }

    companion object {
        private const val KEY_CONTENT = "content"
        private const val KEY_ID = "redPacketId"
        private const val KEY_TITLE = "title"
    }
}