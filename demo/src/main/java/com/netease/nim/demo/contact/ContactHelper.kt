package com.netease.nim.demo.contact

import android.content.Context
import com.netease.nim.demo.contact.activity.UserProfileActivity
import com.netease.nim.demo.msg.MsgActivity
import com.zxn.netease.nimsdk.api.NimUIKit
import com.zxn.netease.nimsdk.api.model.contact.ContactEventListener

/**
 * UIKit联系人列表定制展示类
 */
object ContactHelper {
    @JvmStatic
    fun init() {
        setContactEventListener()
    }

    private fun setContactEventListener() {
        NimUIKit.setContactEventListener(object : ContactEventListener {
            override fun onItemClick(context: Context?, account: String?) {
                //UserProfileActivity.start(context, account);
                //NimUIKit.startP2PSession(context, account)
                //msg,MsgActivity
                context?.let {
                    MsgActivity.jumpTo(context,account)
                }
            }

            override fun onItemLongClick(context: Context?, account: String?) {}
            override fun onAvatarClick(context: Context?, account: String?) {
                UserProfileActivity.start(context, account)
            }
        })
    }
}