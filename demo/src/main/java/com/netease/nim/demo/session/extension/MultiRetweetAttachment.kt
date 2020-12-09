package com.netease.nim.demo.session.extension

import android.text.TextUtils
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject

/*
{
"sessionID":"session_id",
"sessionName":"session_name",
"url": "https://netease.im",//nos
"md5": "d5ff5301b95fb9a23566ed407ebbc177",//nos file
"compressed": false,
"encrypted": true,
"password": "b95fb9a23566ed40",//rc4
"messageAbstract":
[//消息摘要，前两条消息摘要Json
{
"sender": "allday1",
"message": "123313123123123123"//文本消息,最大32字，超过32使用32字 + "......"
},
{
"sender": "allday1",
"message": "[视频]"
}
]
}
*/
class MultiRetweetAttachment : CustomAttachment {
    var sessionID: String? = null
    var sessionName: String? = null

    /** nos文件存储地址  */
    var url: String? = null
    var md5: String? = null
    var isCompressed = false
    var isEncrypted = false
    var password: String? = null

    /** 第一条消息的发送者ID  */
    var sender1: String? = null

    /** 第一条消息在合并转发消息中的展示文案  */
    var message1: String? = null

    /** 第二条消息的发送者ID  */
    var sender2: String? = null

    /** 第二条消息在合并转发消息中的展示文案  */
    var message2: String? = null

    constructor() : super(CustomAttachmentType.MultiRetweet)
    constructor(
        sessionID: String?,
        sessionName: String?,
        url: String?,
        md5: String?,
        compressed: Boolean,
        encrypted: Boolean,
        password: String?,
        sender1: String?,
        message1: String?,
        sender2: String?,
        message2: String?
    ) : super(CustomAttachmentType.MultiRetweet) {
        this.sessionID = sessionID
        this.sessionName = sessionName
        this.url = url
        this.md5 = md5
        isCompressed = compressed
        isEncrypted = encrypted
        this.password = password
        this.sender1 = sender1
        this.message1 = message1
        this.sender2 = sender2
        this.message2 = message2
    }

    override fun parseData(data: JSONObject?) {
        //如果Json格式包含外层部分，则先进入内层
        var data = data
        if (data!!.containsKey("data")) {
            data = data.getJSONObject("data")
        }
        try {
            sessionID = data!!.getString(keySessionId)
            sessionName = data.getString(keySessionName)
            url = data.getString(keyUrl)
            md5 = data.getString(keyMd5)
            isCompressed = data.getBooleanValue(keyCompressed)
            isEncrypted = data.getBooleanValue(keyEncrypted)
            password = data.getString(keyPassword)
            val msgAbs = data.getJSONArray(keyMessageAbstract)
            val obj1 = msgAbs.getJSONObject(0)
            sender1 = obj1.getString(keySender)
            message1 = obj1.getString(keyMessage)
            if (msgAbs.size > 1) {
                val obj2 = msgAbs.getJSONObject(1)
                sender2 = obj2.getString(keySender)
                message2 = obj2.getString(keyMessage)
            }
        } catch (e: Exception) {
            //转化失败，条目显示null字符
            e.printStackTrace()
        }
    }

    override fun packData(): JSONObject? {
        val data = JSONObject()
        data[keySessionId] = sessionID
        data[keySessionName] = sessionName
        data[keyUrl] = url
        data[keyMd5] = md5
        data[keyCompressed] = isCompressed
        data[keyEncrypted] = isEncrypted
        data[keyPassword] = password
        val messageAbstract = JSONArray()
        val obj1 = JSONObject()
        obj1[keySender] = sender1
        obj1[keyMessage] = message1
        messageAbstract.add(obj1)
        //只有一条消息时，不传递第二组的字段
        if (!TextUtils.isEmpty(sender2)) {
            val obj2 = JSONObject()
            obj2[keySender] = sender2
            obj2[keyMessage] = message2
            messageAbstract.add(obj2)
        }
        data[keyMessageAbstract] = messageAbstract
        return data
    }

    companion object {
        const val keySessionId = "sessionID"
        const val keySessionName = "sessionName"
        const val keyUrl = "url"
        const val keyMd5 = "md5"
        const val keyCompressed = "compressed"
        const val keyEncrypted = "encrypted"
        const val keyPassword = "password"
        const val keyMessageAbstract = "messageAbstract"
        const val keySender = "sender"
        const val keyMessage = "message"
    }
}