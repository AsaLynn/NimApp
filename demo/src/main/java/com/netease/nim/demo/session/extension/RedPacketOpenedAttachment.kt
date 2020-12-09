package com.netease.nim.demo.session.extension

import com.alibaba.fastjson.JSONObject
import com.netease.nim.demo.DemoCache.getAccount
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.zxn.netease.nimsdk.business.uinfo.UserInfoHelper

class RedPacketOpenedAttachment : CustomAttachment(CustomAttachmentType.OpenedRedPacket) {
    var sendAccount //发送红包ID
            : String? = null
        private set
    var openAccount //打开红包ID
            : String? = null
        private set
    var redPacketId //红包ID
            : String? = null
        private set
    var isRpGetDone //是否被领完
            = false
        private set

    fun getSendNickName(sessionTypeEnum: SessionTypeEnum, targetId: String): String {
        return if (getAccount() == sendAccount && getAccount() == openAccount) {
            "自己"
        } else getDisplayName(sessionTypeEnum, targetId, sendAccount)
    }

    fun getOpenNickName(sessionTypeEnum: SessionTypeEnum, targetId: String): String {
        return getDisplayName(sessionTypeEnum, targetId, openAccount)
    }

    // 我发的红包或者是我打开的红包
    fun belongTo(account: String?): Boolean {
        return if (openAccount == null || sendAccount == null || account == null) {
            false
        } else openAccount == account || sendAccount == account
    }

    private fun getDisplayName(
        sessionTypeEnum: SessionTypeEnum,
        targetId: String,
        account: String?
    ): String {
        return if (sessionTypeEnum == SessionTypeEnum.P2P) {
            UserInfoHelper.getUserDisplayNameEx(account, "你")
        } else {
            ""
        }
    }

    fun getDesc(sessionTypeEnum: SessionTypeEnum, targetId: String): String {
        val sender = getSendNickName(sessionTypeEnum, targetId)
        val opened = getOpenNickName(sessionTypeEnum, targetId)
        return String.format("%s领取了%s的红包", opened, sender)
    }

    private fun setSendAccount(sendAccount: String) {
        this.sendAccount = sendAccount
    }

    private fun setOpenAccount(openAccount: String) {
        this.openAccount = openAccount
    }

    private fun setRedPacketId(redPacketId: String) {
        this.redPacketId = redPacketId
    }

    private fun setIsGetDone(isGetDone: Boolean) {
        isRpGetDone = isGetDone
    }

    override fun parseData(data: JSONObject?) {
        sendAccount = data!!.getString(KEY_SEND)
        openAccount = data.getString(KEY_OPEN)
        redPacketId = data.getString(KEY_RP_ID)
        isRpGetDone = data.getBoolean(KEY_DONE)
    }

    override fun packData(): JSONObject? {
        val jsonObj = JSONObject()
        jsonObj[KEY_SEND] = sendAccount
        jsonObj[KEY_OPEN] = openAccount
        jsonObj[KEY_RP_ID] = redPacketId
        jsonObj[KEY_DONE] = isRpGetDone
        return jsonObj
    }

    companion object {
        private const val KEY_SEND = "sendPacketId"
        private const val KEY_OPEN = "openPacketId"
        private const val KEY_RP_ID = "redPacketId"
        private const val KEY_DONE = "isGetDone"
        fun obtain(
            sendPacketId: String,
            openPacketId: String,
            packetId: String,
            isGetDone: Boolean
        ): RedPacketOpenedAttachment {
            val model = RedPacketOpenedAttachment()
            model.setRedPacketId(packetId)
            model.setSendAccount(sendPacketId)
            model.setOpenAccount(openPacketId)
            model.setIsGetDone(isGetDone)
            return model
        }
    }
}