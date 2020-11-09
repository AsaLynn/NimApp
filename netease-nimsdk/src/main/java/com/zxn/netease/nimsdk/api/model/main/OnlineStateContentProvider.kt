package com.zxn.netease.nimsdk.api.model.main


interface OnlineStateContentProvider {
    // 用于展示最近联系人界面的在线状态
    fun getSimpleDisplay(account: String?): String?

    // 用于展示聊天界面的在线状态
    fun getDetailDisplay(account: String?): String?
}