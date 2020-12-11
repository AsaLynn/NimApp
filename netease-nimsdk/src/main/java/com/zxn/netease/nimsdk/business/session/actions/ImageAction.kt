package com.zxn.netease.nimsdk.business.session.actions

import com.netease.nimlib.sdk.msg.MessageBuilder
import com.zxn.netease.nimsdk.R
import java.io.File

/**
 * 图片选择
 */
class ImageAction :
    PickImageAction(R.drawable.nim_message_plus_photo_selector, R.string.input_panel_photo, true) {
    override fun onPicked(file: File?) {
        val message = MessageBuilder.createImageMessage(account, sessionType, file, file!!.name)
        sendMessage(message)
    }
}