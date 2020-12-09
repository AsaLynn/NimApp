package com.netease.nim.demo.session.viewholder

import com.netease.nim.demo.session.extension.DefaultCustomAttachment
import com.zxn.netease.nimsdk.business.session.viewholder.MsgViewHolderText
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter

class MsgViewHolderDefCustom(adapter: BaseMultiItemFetchLoadAdapter<*, *>?) :
    MsgViewHolderText(adapter) {
    override fun getDisplayText(): String {
        val attachment = message.attachment as DefaultCustomAttachment
        return "type: " + attachment.type + ", data: " + attachment.content
    }
}