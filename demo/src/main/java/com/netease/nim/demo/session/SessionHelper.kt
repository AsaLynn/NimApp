package com.netease.nim.demo.session

import android.content.Context
import android.view.View
import com.netease.nim.demo.DemoCache.getAccount
import com.netease.nim.demo.R
import com.netease.nim.demo.contact.activity.UserProfileActivity
import com.netease.nim.demo.session.action.FileAction
import com.netease.nim.demo.session.action.GuessAction
import com.netease.nim.demo.session.action.SnapChatAction
import com.netease.nim.demo.session.extension.*
import com.netease.nim.demo.session.load.YinYuLoadMoreView
import com.netease.nim.demo.session.viewholder.*
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.attachment.FileAttachment
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.netease.nimlib.sdk.robot.model.RobotAttachment
import com.zxn.netease.nimsdk.api.NimUIKit
import com.zxn.netease.nimsdk.api.model.recent.RecentCustomization
import com.zxn.netease.nimsdk.api.model.session.SessionCustomization
import com.zxn.netease.nimsdk.api.model.session.SessionCustomization.OptionsButton
import com.zxn.netease.nimsdk.api.model.session.SessionEventListener
import com.zxn.netease.nimsdk.api.wrapper.NimMessageRevokeObserver
import com.zxn.netease.nimsdk.business.session.actions.BaseAction
import com.zxn.netease.nimsdk.business.session.actions.SelectImageAction
import com.zxn.netease.nimsdk.business.session.actions.TakePictureAction
import com.zxn.netease.nimsdk.business.session.module.MsgRevokeFilter
import com.zxn.netease.nimsdk.business.session.viewholder.MsgViewHolderUnknown
import com.zxn.netease.nimsdk.impl.customization.DefaultRecentCustomization
import com.zxn.utils.UIUtils
import java.util.*

/**
 * UIKit自定义消息界面用法展示类
 */
object SessionHelper {

    var p2pCustomization: SessionCustomization = SessionCustomization().apply {

        this.backgroundColor = UIUtils.getColor(R.color.colorPrimary)

        this.loadMoreView = YinYuLoadMoreView()

        this.actions = ArrayList<BaseAction>().apply {
            add(SelectImageAction())
            add(TakePictureAction())
        }

        this.buttons = ArrayList<OptionsButton>().apply {
            val cloudMsgButton: OptionsButton = object : OptionsButton() {
                override fun onClick(context: Context, view: View, sessionId: String) {}
            }.apply {
                iconId = R.drawable.nim_ic_messge_history
            }
            add(cloudMsgButton)
        }

        //定制底部按钮:

    }


    private var myP2pCustomization: SessionCustomization? = SessionCustomization().apply {
        // 定制加号点开后可以包含的操作， 默认已经有图片，视频等消息了
        val actions = ArrayList<BaseAction>()
        actions.add(SnapChatAction())
        actions.add(GuessAction())
        actions.add(FileAction())
        this.actions = actions
        this.withSticker = true
        // 定制ActionBar右边的按钮，可以加多个
        val buttons = ArrayList<OptionsButton>()
        val cloudMsgButton: OptionsButton = object : OptionsButton() {
            override fun onClick(context: Context, view: View, sessionId: String) {}
        }
        cloudMsgButton.iconId = R.drawable.nim_ic_messge_history
        buttons.add(cloudMsgButton)
        this.buttons = buttons
    }

    private var recentCustomization: RecentCustomization? = null
        private get() {
            if (field == null) {
                field = object : DefaultRecentCustomization() {
                }
            }
            return field
        }

    const val USE_LOCAL_ANTISPAM = true

    @JvmStatic
    fun init() {
        // 注册自定义消息附件解析器
        NIMClient.getService(MsgService::class.java)
            .registerCustomAttachmentParser(CustomAttachParser())
        // 注册各种扩展消息类型的显示ViewHolder
        registerViewHolders()
        // 设置会话中点击事件响应处理
        setSessionListener()
        // 注册消息转发过滤器
        registerMsgForwardFilter()
        // 注册消息撤回过滤器
        registerMsgRevokeFilter()
        // 注册消息撤回监听器
        registerMsgRevokeObserver()
        NimUIKit.setCommonP2PSessionCustomization(p2pCustomization)
        NimUIKit.setRecentCustomization(recentCustomization)
    }

    @JvmStatic
    @JvmOverloads
    fun startP2PSession(context: Context?, account: String, anchor: IMMessage? = null) {
        if (getAccount() != account) {
            NimUIKit.startP2PSession(context, account, anchor)
        } else {
            NimUIKit.startChatting(
                context,
                account,
                SessionTypeEnum.P2P,
                myP2pCustomization,
                anchor
            )
        }
    }

    private fun checkLocalAntiSpam(message: IMMessage): Boolean {
        if (!USE_LOCAL_ANTISPAM) {
            return true
        }
        val result = NIMClient.getService(MsgService::class.java).checkLocalAntiSpam(
            message.content,
            "**"
        )
        val operator = result?.operator ?: 0
        when (operator) {
            1 -> {
                message.content = result!!.content
                return true
            }
            2 -> return false
            3 -> {
                message.setClientAntiSpam(true)
                return true
            }
            0 -> {
            }
            else -> {
            }
        }
        return true
    }

    /**
     * 获取消息的简述
     *
     * @param msg 消息
     * @return 简述
     */
    private fun getMsgDigest(msg: IMMessage): String {
        return when (msg.msgType) {
            MsgTypeEnum.avchat, MsgTypeEnum.text, MsgTypeEnum.tip -> msg.content
            MsgTypeEnum.image -> "[图片]"
            MsgTypeEnum.video -> "[视频]"
            MsgTypeEnum.audio -> "[语音消息]"
            MsgTypeEnum.location -> "[位置]"
            MsgTypeEnum.file -> "[文件]"
            MsgTypeEnum.robot -> "[机器人消息]"
            else -> "[自定义消息] "
        }
    }

    private fun registerViewHolders() {
        NimUIKit.registerMsgItemViewHolder(
            FileAttachment::class.java,
            MsgViewHolderFile::class.java
        )
        NimUIKit.registerMsgItemViewHolder(
            GuessAttachment::class.java,
            MsgViewHolderGuess::class.java
        )
        NimUIKit.registerMsgItemViewHolder(
            CustomAttachment::class.java,
            MsgViewHolderDefCustom::class.java
        )
        NimUIKit.registerMsgItemViewHolder(
            StickerAttachment::class.java,
            MsgViewHolderSticker::class.java
        )
        NimUIKit.registerMsgItemViewHolder(
            MultiRetweetAttachment::class.java,
            MsgViewHolderMultiRetweet::class.java
        )
        registerRedPacketViewHolder()
        registerMultiRetweetCreator()
    }

    private fun registerRedPacketViewHolder() {
        NimUIKit.registerMsgItemViewHolder(
            RedPacketAttachment::class.java,
            MsgViewHolderUnknown::class.java
        )
        NimUIKit.registerMsgItemViewHolder(
            RedPacketOpenedAttachment::class.java,
            MsgViewHolderUnknown::class.java
        )
    }

    private fun registerMultiRetweetCreator() {
        /*val creator =
            IMultiRetweetMsgCreator { msgList: List<IMMessage?>?, shouldEncrypt: Boolean, callback: CreateMessageCallback? ->
                MessageHelper.createMultiRetweet(
                    msgList,
                    shouldEncrypt,
                    callback
                )
            }
        NimUIKit.registerMultiRetweetMsgCreator(creator)*/
    }

    private fun setSessionListener() {
        val listener: SessionEventListener = object : SessionEventListener {
            override fun onAvatarClicked(context: Context, message: IMMessage) {
                // 一般用于打开用户资料页面
                if (message.msgType == MsgTypeEnum.robot && message.direct == MsgDirectionEnum.In) {
                    val attachment = message.attachment as RobotAttachment
                }
                UserProfileActivity.start(context, message.fromAccount)
            }

            override fun onAvatarLongClicked(context: Context, message: IMMessage) {
                // 一般用于群组@功能，或者弹出菜单，做拉黑，加好友等功能
            }

            override fun onAckMsgClicked(context: Context, message: IMMessage) {}
        }
        NimUIKit.setSessionListener(listener)
    }

    /**
     * 消息转发过滤器
     */
    private fun registerMsgForwardFilter() {

    }

    /**
     * 消息撤回过滤器
     */
    private fun registerMsgRevokeFilter() {
        NimUIKit.setMsgRevokeFilter(object : MsgRevokeFilter {
            override fun shouldIgnore(message: IMMessage?): Boolean {
                return false
            }
        })
    }

    private fun registerMsgRevokeObserver() {
        NIMClient.getService(MsgServiceObserve::class.java)
            .observeRevokeMessage(NimMessageRevokeObserver(), true)
    }
}