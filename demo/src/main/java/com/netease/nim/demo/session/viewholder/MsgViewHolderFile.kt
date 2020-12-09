package com.netease.nim.demo.session.viewholder

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.netease.nim.demo.R
import com.netease.nim.demo.file.FileIcons
import com.netease.nim.demo.session.activity.FileDownloadActivity
import com.netease.nimlib.sdk.msg.attachment.FileAttachment
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum
import com.zxn.netease.nimsdk.business.session.viewholder.MsgViewHolderBase
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter
import com.zxn.netease.nimsdk.common.util.file.AttachmentStore
import com.zxn.netease.nimsdk.common.util.file.FileUtil

/**
 * Created by zhoujianghua on 2015/8/6.
 */
class MsgViewHolderFile(adapter: BaseMultiItemFetchLoadAdapter<*, *>?) :
    MsgViewHolderBase(adapter) {
    private var fileIcon: ImageView? = null
    private var fileNameLabel: TextView? = null
    private var fileStatusLabel: TextView? = null
    private val progressBar: ProgressBar? = null
    private var msgAttachment: FileAttachment? = null
    override fun getContentResId(): Int {
        return R.layout.nim_message_item_file
    }

    override fun inflateContentView() {
        fileIcon = view.findViewById(R.id.message_item_file_icon_image)
        fileNameLabel = view.findViewById(R.id.message_item_file_name_label)
        fileStatusLabel = view.findViewById(R.id.message_item_file_status_label)
        progressBar = view.findViewById(R.id.message_item_file_transfer_progress_bar)
    }

    override fun bindContentView() {
        msgAttachment = message.attachment as FileAttachment
        val path = msgAttachment!!.path
        initDisplay()
        if (!TextUtils.isEmpty(path)) {
            loadImageView()
        } else {
            val status = message.attachStatus
            when (status) {
                AttachStatusEnum.def -> updateFileStatusLabel()
                AttachStatusEnum.transferring -> {
                    fileStatusLabel!!.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE
                    val percent = (msgAdapter.getProgress(message) * 100).toInt()
                    progressBar.progress = percent
                }
                AttachStatusEnum.transferred, AttachStatusEnum.fail -> updateFileStatusLabel()
            }
        }
    }

    private fun loadImageView() {
        fileStatusLabel!!.visibility = View.VISIBLE
        // 文件长度
        val sb = StringBuilder()
        sb.append(FileUtil.formatFileSize(msgAttachment!!.size))
        fileStatusLabel!!.text = sb.toString()
        progressBar.visibility = View.GONE
    }

    private fun initDisplay() {
        val iconResId = FileIcons.smallIcon(msgAttachment!!.displayName)
        fileIcon!!.setImageResource(iconResId)
        fileNameLabel!!.text = msgAttachment!!.displayName
    }

    private fun updateFileStatusLabel() {
        fileStatusLabel!!.visibility = View.VISIBLE
        progressBar.visibility = View.GONE

        // 文件长度
        val sb = StringBuilder()
        sb.append(FileUtil.formatFileSize(msgAttachment!!.size))
        sb.append("  ")
        // 下载状态
        val path = msgAttachment!!.pathForSave
        if (AttachmentStore.isFileExist(path)) {
            sb.append(context.getString(R.string.file_transfer_state_downloaded))
        } else {
            sb.append(context.getString(R.string.file_transfer_state_undownload))
        }
        fileStatusLabel!!.text = sb.toString()
    }

    override fun onItemClick() {
        FileDownloadActivity.start(context, message)
    }

    override fun leftBackground(): Int {
        return R.drawable.nim_message_left_white_bg
    }

    override fun rightBackground(): Int {
        return R.drawable.nim_message_right_blue_bg
    }
}