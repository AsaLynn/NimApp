package com.zxn.netease.nimsdk.api.model.contact

/**
 * UIKit 与 app 好友关系变化监听接口
 */
interface ContactChangedObserver {
    /**
     * 增加或者更新好友
     *
     * @param accounts 账号列表
     */
    fun onAddedOrUpdatedFriends(accounts: List<String>)

    /**
     * 删除好友
     *
     * @param accounts 账号列表
     */
    fun onDeletedFriends(accounts: List<String>)

    /**
     * 增加到黑名单
     *
     * @param accounts 账号列表
     */
    fun onAddUserToBlackList(accounts: List<String>)

    /**
     * 从黑名单移除
     *
     * @param accounts 账号列表
     */
    fun onRemoveUserFromBlackList(accounts: List<String>)
}