package com.zxn.netease.nimsdk.api.model.user

/**
 * UIKit 与 app 好友关系变化监听接口
 */
interface UserInfoObserver {
    /**
     * 用户信息变更
     *
     * @param accounts 账号列表
     */
    fun onUserInfoChanged(accounts: List<String>?)
}