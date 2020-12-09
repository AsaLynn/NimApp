package com.netease.nim.demo.session.extension

import com.alibaba.fastjson.JSONObject

/**
 * 默认的自定义消息附件解析器
 */
class DefaultCustomAttachment : CustomAttachment(0) {
    var content: String? = null
        private set

    override fun parseData(data: JSONObject?) {
        content = data!!.toJSONString()
    }

    override fun packData(): JSONObject? {
        var data: JSONObject? = null
        try {
            data = JSONObject.parseObject(content)
        } catch (e: Exception) {
        }
        return data
    }
}