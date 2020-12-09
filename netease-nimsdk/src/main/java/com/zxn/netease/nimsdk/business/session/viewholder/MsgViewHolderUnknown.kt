package com.zxn.netease.nimsdk.business.session.viewholder

import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.zxn.netease.nimsdk.R
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter

/**
 * 未知消息类型
 */
class MsgViewHolderUnknown(adapter: BaseMultiItemFetchLoadAdapter<*, *>) :
    MsgViewHolderBase(adapter) {

//    fun isShowHeadImage(): Boolean {
//        return message.sessionType != SessionTypeEnum.ChatRoom
//    }

    override val contentResId: Int = R.layout.nim_message_item_unknown


    override fun inflateContentView() {}

    override fun bindContentView() {}
}