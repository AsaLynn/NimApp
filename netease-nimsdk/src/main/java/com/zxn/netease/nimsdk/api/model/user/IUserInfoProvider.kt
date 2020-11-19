package com.zxn.netease.nimsdk.api.model.user

import com.netease.nimlib.sdk.uinfo.model.UserInfo
import com.zxn.netease.nimsdk.api.model.SimpleCallback


interface IUserInfoProvider<T : UserInfo?> {
    /**
     * 同步获取userInfo
     *
     * @param account 账号
     * @return userInfo
     */
    fun getUserInfo(account: String): T

    /**
     * 同步获取userInfo列表
     *
     * @param accounts 账号
     * @return userInfo
     */
    fun getUserInfo(accounts: List<String?>): List<T>?

    /**
     * 异步获取userInfo
     *
     * @param account  账号id
     * @param callback 回调
     */
    fun getUserInfoAsync(account: String, callback: SimpleCallback<T>?)

    /**
     * 异步获取userInfo列表
     *
     * @param accounts 账号id 集合
     * @param callback 回调
     */
    fun getUserInfoAsync(accounts: List<String?>, callback: SimpleCallback<List<T>?>?)
}