package com.netease.nim.demo.session.viewholder

import com.netease.nim.demo.session.extension.DefaultCustomAttachment
import com.zxn.netease.nimsdk.business.session.viewholder.MsgViewHolderText
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter

/**
 * Copyright(c) ${}YEAR} ZhuLi co.,Ltd All Rights Reserved.
 *
 * @className:
 * @description: TODO 类描述
 * @version: v0.0.1
 * @author: zxn < a href=" ">zhangxiaoning@17biyi.com</ a>
 * @date: 2020/12/9 15:43
 * @updateUser: 更新者：
 * @updateDate: 2020/12/9 15:43
 * @updateRemark: 更新说明：
 * @version: 1.0
 * */
class MsgViewHolderDefCustom(adapter: BaseMultiItemFetchLoadAdapter<*, *>) :
    MsgViewHolderText(adapter) {

//    override var displayText: String?
//    = "type: " + (message!!.attachment as DefaultCustomAttachment).type + ", data: " + (message!!.attachment as DefaultCustomAttachment).content


    /*fun getDisplayText(): String {
        val attachment = message.attachment as DefaultCustomAttachment
        return "type: " + attachment.type + ", data: " + attachment.content
    }*/

    override fun displayText(): String {
        val attachment = message?.attachment as DefaultCustomAttachment
        return "type: " + attachment.type + ", data: " + attachment.content
    }

}