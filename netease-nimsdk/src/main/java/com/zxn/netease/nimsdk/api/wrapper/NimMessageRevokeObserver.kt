package com.zxn.netease.nimsdk.api.wrapper

import android.util.Log
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.msg.constant.RevokeType
import com.netease.nimlib.sdk.msg.model.RevokeMsgNotification
import com.zxn.netease.nimsdk.business.session.helper.MessageHelper

/**
 * 云信消息撤回观察者
 */
class NimMessageRevokeObserver : Observer<RevokeMsgNotification?> {
    override fun onEvent(notification: RevokeMsgNotification?) {
        if (notification == null || notification.message == null) {
            return
        }
        Log.i(
            TAG,
            String.format(
                "notification type=%s, postscript=%s",
                notification.notificationType,
                notification.customInfo
            )
        )
        if (notification.revokeType == RevokeType.P2P_ONE_WAY_DELETE_MSG ||
            notification.revokeType == RevokeType.TEAM_ONE_WAY_DELETE_MSG
        ) {
            return
        }
        MessageHelper.getInstance()
            .onRevokeMessage(notification.message, notification.revokeAccount)
    }

    companion object {
        private const val TAG = "NimMsgRevokeObserver"
    }
}