package com.netease.nim.demo.session.action

import com.netease.nim.demo.R
import com.netease.nim.demo.msg.MsgActivity
import com.zxn.netease.nimsdk.business.session.actions.BaseAction
import com.zxn.netease.nimsdk.common.ToastHelper
import com.zxn.netease.nimsdk.common.util.sys.NetworkUtil


class AVChatAction : BaseAction(
    R.drawable.message_plus_audio_chat_selector, R.string.input_panel_audio_call
) {

    companion object {
        private const val TAG = "AVChatAction"
    }

    override fun onClick() {
        if (NetworkUtil.isNetAvailable(activity)) {
            if (activity is MsgActivity) {
                (activity as MsgActivity).call()
            }
        } else {
            activity?.let {
                ToastHelper.showToast(it, R.string.network_is_not_available)
            }
        }
    }


    /**
     * 邀请别人
     */
    private fun inviteOther() {
        /*if (channelInfo == null) {
            Toast.makeText(activity, "请先创建频道或加入频道", Toast.LENGTH_SHORT).show()
            return
        }
        val account: String = edtInviteAccount.getText().toString()
        if (TextUtils.isEmpty(account)) {
            Toast.makeText(activity, "请输入对方帐号", Toast.LENGTH_SHORT).show()
            return
        }
        invitedRequestId = System.currentTimeMillis().toString() + "_id"
        val param = InviteParamBuilder(channelFullInfo.getChannelId(), account, invitedRequestId)
        param.offlineEnabled(true)
        service.invite(param).setCallback(object : RequestCallback<Void?> {
            override fun onSuccess(param: Void?) {
                Toast.makeText(
                    activity,
                    "邀请成功 ：channelId = " + channelFullInfo.getChannelId()
                        .toString() + ", requestId = " + invitedRequestId,
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFailed(code: Int) {
                Toast.makeText(activity, "邀请失败 ：code = $code", Toast.LENGTH_SHORT).show()
            }

            override fun onException(exception: Throwable) {
                Toast.makeText(activity, "邀请异常 ：exception = $exception", Toast.LENGTH_SHORT).show()
            }
        })*/
    }


}