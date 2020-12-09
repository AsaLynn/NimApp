package com.zxn.netease.nimsdk.business.session.viewholder

import com.zxn.netease.nimsdk.R
import com.zxn.netease.nimsdk.business.session.activity.WatchMessagePictureActivity
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter

/**
 * 图片消息展示.
 */
class MsgViewHolderPicture(adapter: BaseMultiItemFetchLoadAdapter<*, *>) :
    MsgViewHolderThumbBase(adapter) {

    override fun thumbFromSourceFile(path: String?): String? = path

    override val contentResId: Int = R.layout.nim_message_item_picture

    override fun onItemClick() {
        WatchMessagePictureActivity.start(context, message)
    }

}