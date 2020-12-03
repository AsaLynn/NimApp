package com.zxn.netease.nimsdk.business.session.module

import com.netease.nimlib.sdk.msg.model.IMMessage

/**
 * 会话窗口提供给子模块的代理接口。
 */
interface ModuleProxy {
    /**
     * 发送消息
     *
     * @param msg 被发送的消息
     */
    fun sendMessage(msg: IMMessage?): Boolean

    /**
     * 消息输入区展开时候的处理
     */
    fun onInputPanelExpand()

    /**
     * 应当收起输入区
     */
    fun shouldCollapseInputPanel()

    /**
     * 是否正在录音
     *
     * @return 是否正在录音
     */
    val isLongClickEnabled: Boolean
    fun onItemFooterClick(message: IMMessage?)

    /**
     * 用户进行回复操作
     *
     * @param replyMsg 被回复的消息
     */
    fun onReplyMessage(replyMsg: IMMessage?)
}