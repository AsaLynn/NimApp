package com.zxn.netease.nimsdk.api

import com.netease.nimlib.sdk.media.record.RecordType
import com.zxn.netease.nimsdk.R

/**
 * UI参数全局配置:
 */
class UIKitOptions {
    /**
     * 配置 APP 保存图片/语音/文件/log等数据缓存的目录(一般配置在SD卡目录)
     *
     * 默认为 /sdcard/{packageName}/
     */
    @JvmField
    var appCacheDir: String? = null

    /**
     * 独立聊天室模式，没有 IM 业务
     */
    @JvmField
    var independentChatRoom = false

    /**
     * 是否加载表情贴图
     */
    @JvmField
    var loadSticker = true

    /**
     * 开启@功能
     */
    @JvmField
    var aitEnable = true

    /**
     * 支持@群成员
     */
    @JvmField
    var aitTeamMember = true

    /**
     * 支持在 IM 聊天中@机器人
     */
    @JvmField
    var aitIMRobot = false

    /**
     * 支持在 Chat Room 中@机器人
     */
    @JvmField
    var aitChatRoomRobot = false

    /**
     * UIKit 异步初始化
     * 使用异步方式构建能缩短初始化时间，但同时必须查看初始化状态或者监听初始化成功通知
     */
    @JvmField
    var initAsync = false

    /**
     * 使用云信托管账号体系，构建缓存
     */
    @JvmField
    var buildNimUserCache = true

    /**
     * 构建群缓存
     */
    @JvmField
    var buildTeamCache = true

    /**
     * 构建群好友关系缓存
     */
    @JvmField
    var buildFriendCache = true

    /**
     * 构建智能机器人缓存
     */
    @JvmField
    var buildRobotInfoCache = true

    /**
     * 构建聊天室成员缓存
     */
    @JvmField
    var buildChatRoomMemberCache = true

    /**
     * 消息列表每隔多久显示一条消息时间信息，默认五分钟
     */
    @JvmField
    var displayMsgTimeWithInterval = 5 * 60 * 1000.toLong()

    /**
     * 单次抓取消息条数配置
     */
    @JvmField
    var messageCountLoadOnce = 20

    /**
     * IM 接收到的消息时，内容区域背景的drawable id
     */
    @JvmField
    var messageLeftBackground = R.drawable.nim_message_item_left_selector

    /**
     * IM 发送出去的消息时，内容区域背景的drawable id
     */
    @JvmField
    var messageRightBackground = R.drawable.nim_message_item_right_selector

    /**
     * chat room 接收到的消息时，内容区域背景的drawable id
     */
    @JvmField
    var chatRoomMsgLeftBackground = 0

    /**
     * chat room 发送出去的消息时，内容区域背景的drawable id
     */
    @JvmField
    var chatRoomMsgRightBackground = 0

    /**
     * 全局是否使用消息已读，如果设置为false，UIKit 组件将不会发送、接收已读回执
     */
    @JvmField
    var shouldHandleReceipt = true

    /**
     * 文本框最大输入字符数目
     */
    @JvmField
    var maxInputTextLength = 5000

    /**
     * 录音类型，默认aac
     */
    @JvmField
    var audioRecordType = RecordType.AAC

    /**
     * 录音时长限制，单位秒，默认最长60s
     */
    @JvmField
    var audioRecordMaxTime = 60

    /**
     * 不显示语音消息未读红点
     */
    @JvmField
    var disableAudioPlayedStatusIcon = false

    /**
     * 禁止音频轮播
     */
    @JvmField
    var disableAutoPlayNextAudio = false

    /**
     * 是否开启允许群管理员撤回他人消息
     */
    @JvmField
    var enableTeamManagerRevokeMsg = true

    companion object {
        /**
         * 返回默认的针对独立模式聊天室的 UIKitOptions
         * @return
         */
        fun buildForIndependentChatRoom(): UIKitOptions {
            val options = UIKitOptions()
            options.buildFriendCache = false
            options.buildNimUserCache = false
            options.buildTeamCache = false
            options.buildRobotInfoCache = false
            options.loadSticker = false
            options.independentChatRoom = true
            return options
        }
    }
}