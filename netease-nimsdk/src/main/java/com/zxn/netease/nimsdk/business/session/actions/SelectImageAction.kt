package com.zxn.netease.nimsdk.business.session.actions

import com.netease.nimlib.sdk.msg.MessageBuilder
import com.zxn.netease.nimsdk.R
import com.zxn.netease.nimsdk.common.media.imagepicker.ImagePickerLauncher
import com.zxn.netease.nimsdk.common.media.imagepicker.option.DefaultImagePickerOption
import com.zxn.netease.nimsdk.common.media.imagepicker.option.ImagePickerOption
import java.io.File

/**
 * Created by zxn on 2020/10/20.
 */
class SelectImageAction : PickImageAction {
    constructor() : super(
        R.drawable.nim_message_plus_photo_selector,
        R.string.input_panel_photo,
        true
    ) {
    }

    constructor(iconResId: Int, titleId: Int, multiSelect: Boolean) : super(
        iconResId,
        titleId,
        multiSelect
    ) {
    }


    override fun showSelector(titleId: Int, requestCode: Int, multiSelect: Boolean) {
        val option = DefaultImagePickerOption.getInstance().setShowCamera(true).setPickType(
            ImagePickerOption.PickType.Image
        ).setMultiMode(multiSelect).setSelectMax(PICK_IMAGE_COUNT)
        ImagePickerLauncher.selectImage(activity, requestCode, option)
    }

    override fun onPicked(file: File?) {
        file?.let {
            val message = MessageBuilder.createImageMessage(account, sessionType, file, file.name)
            sendMessage(message)
        }
    }
}