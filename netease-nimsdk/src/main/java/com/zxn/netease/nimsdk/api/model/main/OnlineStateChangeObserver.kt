package com.zxn.netease.nimsdk.api.model.main


interface OnlineStateChangeObserver {
    /**
     * 通知在线状态事件变化
     *
     * @param account 在线状态事件发生变化的账号
     */
    fun onlineStateChange(account: Set<String?>?)
}