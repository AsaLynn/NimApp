package com.netease.nim.demo.session.action

import com.netease.nim.demo.R
import com.netease.nim.demo.session.extension.GuessAttachment
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.zxn.netease.nimsdk.business.session.actions.BaseAction

/**
 * 发送猜拳的点击行为
 */
class GuessAction : BaseAction(R.drawable.message_plus_guess_selector, R.string.input_panel_guess) {
    override fun onClick() {
        val attachment = GuessAttachment()
        val message = MessageBuilder.createCustomMessage(
            account,
            sessionType,
            attachment.value.desc,
            attachment
        )
        sendMessage(message)
    }
}