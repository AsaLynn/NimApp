package com.netease.nim.demo.session.extension

import android.text.TextUtils
import com.alibaba.fastjson.JSONObject
import com.netease.nim.demo.session.extension.CustomAttachParser.Companion.packData
import com.netease.nimlib.sdk.msg.attachment.FileAttachment

class SnapChatAttachment : FileAttachment {
    constructor(data: JSONObject) {
        load(data)
    }

    override fun toJson(send: Boolean): String {
        val data = JSONObject()
        try {
            if (!send && !TextUtils.isEmpty(path)) {
                data[KEY_PATH] = path
            }
            if (!TextUtils.isEmpty(md5)) {
                data[KEY_MD5] = md5
            }
            data[KEY_URL] = url
            data[KEY_SIZE] = size
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return packData(CustomAttachmentType.SnapChat, data)
    }

    private fun load(data: JSONObject) {
        path = data.getString(KEY_PATH)
        md5 = data.getString(KEY_MD5)
        url = data.getString(KEY_URL)
        size = if (data.containsKey(KEY_SIZE)) data.getLong(KEY_SIZE) else 0
    }

    companion object {
        private const val KEY_PATH = "path"
        private const val KEY_SIZE = "size"
        private const val KEY_MD5 = "md5"
        private const val KEY_URL = "url"
    }
}