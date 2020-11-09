package com.zxn.netease.nimsdk.api.model.robot

import com.netease.nimlib.sdk.robot.model.NimRobotInfo
import com.zxn.netease.nimsdk.api.model.SimpleCallback

/**
 * 智能机器人信息提供者
 */
interface RobotInfoProvider {
    /**
     * 根据 id 获取智能机器人
     *
     * @param account 智能机器人id
     * @return NimRobotInfo
     */
    fun getRobotByAccount(account: String?): NimRobotInfo?

    /**
     * 获取所有的智能机器人
     *
     * @return 智能机器人列表
     */
    val allRobotAccounts: List<NimRobotInfo?>?

    /**
     * IM 模式下，获取(异步)智能机器人
     */
    fun fetchRobotList(callback: SimpleCallback<List<NimRobotInfo?>?>?)

    /**
     * 独立聊天室模式下，获取(异步)智能机器人
     *
     * @param roomId 聊天室id
     */
    fun fetchRobotListIndependent(roomId: String?, callback: SimpleCallback<List<NimRobotInfo?>?>?)
}