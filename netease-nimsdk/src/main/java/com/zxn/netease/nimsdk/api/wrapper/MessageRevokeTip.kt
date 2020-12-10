package com.zxn.netease.nimsdk.api.wrapper

import android.text.TextUtils
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.robot.model.RobotAttachment
import com.zxn.netease.nimsdk.api.NimUIKit

/**
 * 消息撤回通知文案
 */
object MessageRevokeTip {
    @JvmStatic
    fun getRevokeTipContent(item: IMMessage, revokeAccount: String): String {
        var fromAccount = item.fromAccount
        if (item.msgType == MsgTypeEnum.robot) {
            val robotAttachment = item.attachment as RobotAttachment
            if (robotAttachment.isRobotSend) {
                fromAccount = robotAttachment.fromRobotAccount
            }
        }
        return if (!TextUtils.isEmpty(
                revokeAccount
            ) && revokeAccount != fromAccount
        ) {
            getRevokeTipOfOther(item.sessionId, item.sessionType, revokeAccount)
        } else {
            var revokeNick = "" // 撤回者
            if (item.sessionType == SessionTypeEnum.P2P) {
                revokeNick = if (item.fromAccount == NimUIKit.getAccount()) "你" else "对方"
            }
            revokeNick + "撤回了一条消息"
        }
    }

    // 撤回其他人的消息时，获取tip
    fun getRevokeTipOfOther(
        sessionID: String?,
        sessionType: SessionTypeEnum?,
        revokeAccount: String?
    ): String {
        return "撤回了一条消息"
    }
}