package com.zxn.netease.nimsdk.api.model.session

import android.content.Context
import com.netease.nimlib.sdk.msg.model.IMMessage
/**
 * 会话窗口消息列表一些点击事件的响应处理函数
 */
interface SessionEventListener {
    // 头像点击事件处理，一般用于打开用户资料页面
    fun onAvatarClicked(context: Context, message: IMMessage)

    // 头像长按事件处理，一般用于群组@功能，或者弹出菜单，做拉黑，加好友等功能
    fun onAvatarLongClicked(context: Context, message: IMMessage)

    // 已读回执事件处理，用于群组的已读回执事件的响应，弹出消息已读详情
    fun onAckMsgClicked(context: Context, message: IMMessage)
}