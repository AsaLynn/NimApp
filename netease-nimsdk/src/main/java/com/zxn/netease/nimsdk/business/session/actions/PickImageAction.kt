package com.zxn.netease.nimsdk.business.session.actions

import android.content.Intent
import com.zxn.netease.nimsdk.R
import com.zxn.netease.nimsdk.business.session.constant.RequestCode
import com.zxn.netease.nimsdk.business.session.helper.SendImageHelper
import com.zxn.netease.nimsdk.common.ToastHelper.showToastLong
import com.zxn.netease.nimsdk.common.media.imagepicker.ImagePickerLauncher
import com.zxn.netease.nimsdk.common.media.imagepicker.option.DefaultImagePickerOption
import com.zxn.netease.nimsdk.common.media.imagepicker.option.ImagePickerOption
import java.io.File

abstract class PickImageAction protected constructor(
    iconResId: Int,
    titleId: Int,
    private val multiSelect: Boolean
) : BaseAction(iconResId, titleId) {
    protected abstract fun onPicked(file: File?)

    override fun onClick() {
        val requestCode = makeRequestCode(RequestCode.PICK_IMAGE)
        showSelector(titleId, requestCode, multiSelect)
    }

    /**
     * 打开图片选择器
     */
    protected open fun showSelector(titleId: Int, requestCode: Int, multiSelect: Boolean) {
        val option = DefaultImagePickerOption.getInstance().setShowCamera(true).setPickType(
            ImagePickerOption.PickType.Image
        ).setMultiMode(multiSelect).setSelectMax(PICK_IMAGE_COUNT)
        ImagePickerLauncher.selectImage(activity, requestCode, option, titleId)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            RequestCode.PICK_IMAGE -> onPickImageActivityResult(requestCode, data)
        }
    }

    /**
     * 图片选取回调
     */
    private fun onPickImageActivityResult(requestCode: Int, data: Intent?) {
        if (data == null) {
            activity?.let {
                showToastLong(it, R.string.picker_image_error)
            }
            return
        }
        sendImageAfterSelfImagePicker(data)
    }

    /**
     * 发送图片
     */
    private fun sendImageAfterSelfImagePicker(data: Intent) {
        SendImageHelper.sendImageAfterSelfImagePicker(activity, data) { file, isOrig ->
            onPicked(
                file
            )
        }
    }

    protected val PICK_IMAGE_COUNT = 9

    companion object {

        const val MIME_JPEG = "image/jpeg"
    }
}