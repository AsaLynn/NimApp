package com.zxn.netease.nimsdk.business.session.viewholder

import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.attachment.FileAttachment
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.zxn.netease.nimsdk.R
import com.zxn.netease.nimsdk.common.ui.imageview.MsgThumbImageView
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter
import com.zxn.netease.nimsdk.common.util.media.BitmapDecoder
import com.zxn.netease.nimsdk.common.util.media.ImageUtil
import com.zxn.netease.nimsdk.common.util.string.StringUtil
import com.zxn.netease.nimsdk.common.util.sys.ScreenUtil
import java.io.File

/**
 *adapter: BaseMultiItemFetchLoadAdapter<*, *>
 */
abstract class MsgViewHolderThumbBase(adapter: BaseMultiItemFetchLoadAdapter<*, *>) :
    MsgViewHolderBase(adapter) {

    protected var thumbnail: MsgThumbImageView? = null
    protected var progressCover: View? = null
    protected var progressLabel: TextView? = null
    override fun inflateContentView() {
        thumbnail = findViewById(R.id.message_item_thumb_thumbnail)
        progressBar = findViewById(R.id.message_item_thumb_progress_bar) // 覆盖掉
        progressCover = findViewById(R.id.message_item_thumb_progress_cover)
        progressLabel = findViewById(R.id.message_item_thumb_progress_text)
    }

    override fun bindContentView() {
        message?.let { message ->
            val msgAttachment = message.attachment as FileAttachment
            val path = msgAttachment.path
            val thumbPath = msgAttachment.thumbPath
            if (!TextUtils.isEmpty(thumbPath)) {
                loadThumbnailImage(thumbPath, false, msgAttachment.extension)
            } else if (!TextUtils.isEmpty(path)) {
                loadThumbnailImage(thumbFromSourceFile(path), true, msgAttachment.extension)
            } else {
                loadThumbnailImage(null, false, msgAttachment.extension)
                if (message.attachStatus == AttachStatusEnum.transferred
                    || message.attachStatus == AttachStatusEnum.def
                ) {
                    downloadAttachment(object : RequestCallback<Void?> {
                        override fun onSuccess(param: Void?) {
                            loadThumbnailImage(
                                msgAttachment.thumbPath,
                                false,
                                msgAttachment.extension
                            )
                            refreshStatus()
                        }

                        override fun onFailed(code: Int) {}
                        override fun onException(exception: Throwable) {}
                    })
                }
            }
            refreshStatus()
        }

    }

    private fun refreshStatus() {
        message?.let { message ->
            val attachment = message.attachment as FileAttachment
            if (TextUtils.isEmpty(attachment.path) && TextUtils.isEmpty(attachment.thumbPath)) {
                if (message.attachStatus == AttachStatusEnum.fail || message.status == MsgStatusEnum.fail) {
                    alertButton.visibility = View.VISIBLE
                } else {
                    alertButton.visibility = View.GONE
                }
            }
            if (message.status == MsgStatusEnum.sending
                || isReceivedMessage && message.attachStatus == AttachStatusEnum.transferring
            ) {
                progressCover!!.visibility = View.VISIBLE
                progressBar?.visibility = View.VISIBLE
                progressLabel!!.visibility = View.VISIBLE
                progressLabel!!.text = StringUtil.getPercentString(msgAdapter.getProgress(message))
            } else {
                progressCover!!.visibility = View.GONE
                progressBar?.visibility = View.GONE
                progressLabel!!.visibility = View.GONE
            }
        }
    }

    private fun loadThumbnailImage(path: String?, isOriginal: Boolean, ext: String) {
        setImageSize(path)
        if (path != null) {
            //thumbnail.loadAsPath(thumbPath, getImageMaxEdge(), getImageMaxEdge(), maskBg());
            thumbnail!!.loadAsPath(path, imageMaxEdge, imageMaxEdge, maskBg(), ext)
        } else {
            thumbnail!!.loadAsResource(R.drawable.nim_image_default, maskBg())
        }
    }

    private fun setImageSize(thumbPath: String?) {
        message?.let { message ->
            var bounds: IntArray? = null
            if (thumbPath != null) {
                bounds = BitmapDecoder.decodeBound(File(thumbPath))
            }
            if (bounds == null) {
                if (message.msgType == MsgTypeEnum.image) {
                    val attachment = message.attachment as ImageAttachment
                    bounds = intArrayOf(attachment.width, attachment.height)
                } else if (message.msgType == MsgTypeEnum.video) {
                    val attachment = message.attachment as VideoAttachment
                    bounds = intArrayOf(attachment.width, attachment.height)
                }
            }
            if (bounds != null) {
                val imageSize = ImageUtil.getThumbnailDisplaySize(
                    bounds[0].toFloat(),
                    bounds[1].toFloat(),
                    imageMaxEdge.toFloat(),
                    imageMinEdge.toFloat()
                )
                setLayoutParams(imageSize.width, imageSize.height, thumbnail!!)
            }
        }
    }

    private fun maskBg(): Int {
        return R.drawable.nim_message_item_round_bg
    }

    protected abstract fun thumbFromSourceFile(path: String?): String?

    companion object {
        @JvmStatic
        val imageMaxEdge: Int
            get() = (165.0 / 320.0 * ScreenUtil.screenWidth).toInt()

        @JvmStatic
        val imageMinEdge: Int
            get() = (76.0 / 320.0 * ScreenUtil.screenWidth).toInt()
    }
}