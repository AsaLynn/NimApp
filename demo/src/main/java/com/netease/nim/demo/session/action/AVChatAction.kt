package com.netease.nim.demo.session.action

import com.netease.nim.avchatkit.AVChatKit
import com.netease.nim.avchatkit.activity.AVChatActivity
import com.netease.nim.demo.R
import com.netease.nimlib.sdk.avchat.constant.AVChatType
import com.zxn.netease.nimsdk.business.session.actions.BaseAction
import com.zxn.netease.nimsdk.business.uinfo.UserInfoHelper
import com.zxn.netease.nimsdk.common.util.sys.NetworkUtil


class AVChatAction() : BaseAction(
    R.drawable.message_plus_audio_chat_selector, R.string.input_panel_audio_call
) {

    override fun onClick() {
        if (NetworkUtil.isNetAvailable(activity)) {
            startAudioVideoCall(AVChatType.AUDIO)
        } else {
            activity?.let {
                //ToastHelper.showToast(this, R.string.network_is_not_available)
            }

        }
    }

    /************************ 音视频通话  */
    fun startAudioVideoCall(avChatType: AVChatType) {
        AVChatKit.outgoingCall(
            activity,
            account,
            UserInfoHelper.getUserDisplayName(account),
            avChatType.value,
            AVChatActivity.FROM_INTERNAL
        )
    }

}