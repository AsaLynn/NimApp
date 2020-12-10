package com.zxn.netease.nimsdk.api.wrapper;

import android.text.TextUtils;

import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.robot.model.RobotAttachment;
import com.zxn.netease.nimsdk.api.NimUIKit;

/**
 * 消息撤回通知文案
 */

public class MessageRevokeTip {

    public static String getRevokeTipContent(IMMessage item, String revokeAccount) {

        String fromAccount = item.getFromAccount();
        if (item.getMsgType() == MsgTypeEnum.robot) {
            RobotAttachment robotAttachment = (RobotAttachment) item.getAttachment();
            if (robotAttachment.isRobotSend()) {
                fromAccount = robotAttachment.getFromRobotAccount();
            }
        }

        if (!TextUtils.isEmpty(
                revokeAccount) && !revokeAccount.equals(fromAccount)) {
            return getRevokeTipOfOther(item.getSessionId(), item.getSessionType(), revokeAccount);
        } else {
            String revokeNick = ""; // 撤回者
            if (item.getSessionType() == SessionTypeEnum.P2P) {
                revokeNick = item.getFromAccount().equals(NimUIKit.getAccount()) ? "你" : "对方";
            }
            return revokeNick + "撤回了一条消息";
        }
    }

    // 撤回其他人的消息时，获取tip
    public static String getRevokeTipOfOther(String sessionID, SessionTypeEnum sessionType, String revokeAccount) {
        return "撤回了一条消息";
    }
}
