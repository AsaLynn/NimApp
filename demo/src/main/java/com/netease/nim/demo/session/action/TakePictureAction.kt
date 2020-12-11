package com.netease.nim.demo.session.action

import com.netease.nimlib.sdk.msg.MessageBuilder
import com.zxn.netease.nimsdk.R
import com.zxn.netease.nimsdk.business.session.actions.PickImageAction
import com.zxn.netease.nimsdk.common.media.imagepicker.ImagePickerLauncher
import java.io.File

/**
 * Created by zxn on 2020/10/20.
 */
open class TakePictureAction :
    PickImageAction(R.drawable.nim_message_plus_video_selector, R.string.input_panel_take, true) {
    override fun showSelector(titleId: Int, requestCode: Int, multiSelect: Boolean) {
        ImagePickerLauncher.takePhoto(activity, requestCode)
    }

    override fun onPicked(file: File?) {
        file?.let {
            val message = MessageBuilder.createImageMessage(account, sessionType, file, file.name)
            sendMessage(message)
        }
    }
}