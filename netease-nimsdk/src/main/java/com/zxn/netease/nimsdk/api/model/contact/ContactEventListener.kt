package com.zxn.netease.nimsdk.api.model.contact

import android.content.Context

/**
 * 通讯录联系人列表一些点击事件的响应处理函数
 */
interface ContactEventListener {
    /**
     * 通讯录联系人项点击事件处理，一般打开会话窗口
     *
     * @param account 点击的联系人帐号
     */
    fun onItemClick(context: Context?, account: String?)

    /**
     * 通讯录联系人项长按事件处理，一般弹出菜单：移除好友、添加到星标好友等
     *
     * @param account 点击的联系人帐号
     */
    fun onItemLongClick(context: Context?, account: String?)

    /**
     * 联系人头像点击相应，一般跳转到用户资料页面
     *
     * @param account 点击的联系人帐号
     */
    fun onAvatarClick(context: Context?, account: String?)
}