package com.zxn.netease.nimsdk.business.session.fragment

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.ResponseCode
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.MemberPushOption
import com.netease.nimlib.sdk.msg.model.MessageReceipt
import com.netease.nimlib.sdk.robot.model.RobotMsgType
import com.zxn.netease.nimsdk.R
import com.zxn.netease.nimsdk.api.model.session.SessionCustomization
import com.zxn.netease.nimsdk.business.ait.AitManager
import com.zxn.netease.nimsdk.business.session.actions.BaseAction
import com.zxn.netease.nimsdk.business.session.actions.SelectImageAction
import com.zxn.netease.nimsdk.business.session.actions.TakePictureAction
import com.zxn.netease.nimsdk.business.session.constant.Extras
import com.zxn.netease.nimsdk.business.session.module.Container
import com.zxn.netease.nimsdk.business.session.module.ModuleProxy
import com.zxn.netease.nimsdk.business.session.module.input.InputPanel
import com.zxn.netease.nimsdk.business.session.module.list.MessageListPanelEx
import com.zxn.netease.nimsdk.common.CommonUtil.isEmpty
import com.zxn.netease.nimsdk.common.fragment.TFragment
import com.zxn.netease.nimsdk.impl.NimUIKitImpl
import com.zxn.utils.UIUtils
import java.util.*

/**
 * 聊天界面基类
 */
open class MessageFragment : TFragment(), ModuleProxy {

    private var rootView: View? = null
    private var customization: SessionCustomization? = null

    /**
     * p2p对方Account或者群id
     */
    protected var sessionId: String? = null

    /**
     * 会话类型
     */
    protected var sessionType: SessionTypeEnum? = null

    // modules
    protected var inputPanel: InputPanel? = null
    protected var messageListPanel: MessageListPanelEx? = null
    protected var aitManager: AitManager? = null
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        parseIntent()
        UIUtils.init(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.nim_message_fragment, container, false)
        return rootView
    }

    override fun onPause() {
        super.onPause()
        NIMClient.getService(MsgService::class.java)
            .setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None)
        inputPanel!!.onPause()
        messageListPanel!!.onPause()
    }

    override fun onResume() {
        super.onResume()
        messageListPanel!!.onResume()
        NIMClient.getService(MsgService::class.java).setChattingAccount(sessionId, sessionType)
        activity!!.volumeControlStream = AudioManager.STREAM_VOICE_CALL // 默认使用听筒播放
    }

    override fun onDestroy() {
        super.onDestroy()
        messageListPanel!!.onDestroy()
        registerObservers(false)
        if (inputPanel != null) {
            inputPanel!!.onDestroy()
        }
        if (aitManager != null) {
            aitManager!!.reset()
        }
    }

    fun onBackPressed(): Boolean {
        return inputPanel!!.collapse(true)
    }

    fun refreshMessageList() {
        messageListPanel!!.refreshMessageList()
    }

    private fun parseIntent() {
        var anchor: IMMessage? = null
        arguments?.let {
            sessionId = it.getString(Extras.EXTRA_ACCOUNT)
            sessionType = it.getSerializable(Extras.EXTRA_TYPE) as SessionTypeEnum?
            anchor = it.getSerializable(Extras.EXTRA_ANCHOR) as IMMessage?
            customization =
                it.getSerializable(Extras.EXTRA_CUSTOMIZATION) as SessionCustomization?
        }

        val container = Container(activity, sessionId, sessionType, this, true)
        if (messageListPanel == null) {
            messageListPanel =
                MessageListPanelEx(container, rootView, anchor, false, false, customization)
        } else {
            messageListPanel!!.reload(container, anchor)
        }
        if (inputPanel == null) {
            inputPanel = InputPanel(container, rootView, actionList)
            inputPanel!!.setCustomization(customization)
        } else {
            inputPanel!!.reload(container, customization)
        }
        initAitManager()
        registerObservers(true)
        if (customization != null) {
            messageListPanel!!.setChattingBackground(
                customization!!.backgroundUri,
                customization!!.backgroundColor
            )
        }
    }

    private fun initAitManager() {
        val options = NimUIKitImpl.getOptions()
        if (options.aitEnable) {
            aitManager = AitManager(
                context,
                if (options.aitTeamMember && sessionType == SessionTypeEnum.Team) sessionId else null,
                options.aitIMRobot
            )
            inputPanel!!.addAitTextWatcher(aitManager)
            aitManager!!.setTextChangeListener(inputPanel)
        }
    }

    /**
     * ************************* 消息收发 **********************************
     */
    // 是否允许发送消息
    protected fun isAllowSendMessage(message: IMMessage?): Boolean {
        customization?.let {
            return it.isAllowSendMessage(message)
        }
        return true
    }

    private fun registerObservers(register: Boolean) {
        val service = NIMClient.getService(
            MsgServiceObserve::class.java
        )
        service.observeReceiveMessage(incomingMessageObserver, register)
        // 已读回执监听
        if (NimUIKitImpl.getOptions().shouldHandleReceipt) {
            service.observeMessageReceipt(messageReceiptObserver, register)
        }
    }

    /**
     * 消息接收观察者
     */
    var incomingMessageObserver =
        Observer { messages: List<IMMessage?> -> onMessageIncoming(messages) } as Observer<List<IMMessage?>>

    private fun onMessageIncoming(messages: List<IMMessage?>) {
        if (isEmpty(messages)) {
            return
        }
        messageListPanel!!.onIncomingMessage(messages)
        // 发送已读回执
        messageListPanel!!.sendReceipt()
    }

    /**
     * 已读回执观察者
     */
    private val messageReceiptObserver: Observer<List<MessageReceipt>> =
        Observer<List<MessageReceipt>> {
            messageListPanel!!.receiveReceipt()
        }

    /**
     * ********************** implements ModuleProxy *********************
     */
    override fun sendMessage(message: IMMessage?): Boolean {
        var message = message
        if (isAllowSendMessage(message)) {
            appendTeamMemberPush(message)
            message = changeToRobotMsg(message)
            appendPushConfigAndSend(message)
        } else {
            // 替换成tip
            message = MessageBuilder.createTipMessage(message!!.sessionId, message.sessionType)
            message.content = "该消息无法发送"
            message.status = MsgStatusEnum.success
            NIMClient.getService(MsgService::class.java).saveMessageToLocal(message, false)
        }
        messageListPanel!!.onMsgSend(message)
        if (aitManager != null) {
            aitManager!!.reset()
        }
        return true
    }

    private fun appendPushConfigAndSend(message: IMMessage?) {
        appendPushConfig(message)
        val service = NIMClient.getService(MsgService::class.java)
        // send message to server and save to db
        val replyMsg = inputPanel!!.replyMessage
        if (replyMsg == null) {
            service.sendMessage(message, false).setCallback(object : RequestCallback<Void?> {
                override fun onSuccess(param: Void?) {}
                override fun onFailed(code: Int) {
                    sendFailWithBlackList(code, message)
                }

                override fun onException(exception: Throwable) {}
            })
        } else {
            service.replyMessage(message, replyMsg, false)
                .setCallback(object : RequestCallback<Void?> {
                    override fun onSuccess(param: Void?) {
                        val threadId = message!!.threadOption.threadMsgIdClient
                        messageListPanel!!.refreshMessageItem(threadId)
                    }

                    override fun onFailed(code: Int) {
                        sendFailWithBlackList(code, message)
                    }

                    override fun onException(exception: Throwable) {}
                })
        }
        inputPanel!!.resetReplyMessage()
    }

    // 被对方拉入黑名单后，发消息失败的交互处理
    private fun sendFailWithBlackList(code: Int, msg: IMMessage?) {
        if (code == ResponseCode.RES_IN_BLACK_LIST.toInt()) {
            // 如果被对方拉入黑名单，发送的消息前不显示重发红点
            msg!!.status = MsgStatusEnum.success
            NIMClient.getService(MsgService::class.java).updateIMMessageStatus(msg)
            messageListPanel!!.refreshMessageList()
            // 同时，本地插入被对方拒收的tip消息
            val tip = MessageBuilder.createTipMessage(msg.sessionId, msg.sessionType)
            tip.content = activity!!.getString(R.string.black_list_send_tip)
            tip.status = MsgStatusEnum.success
            val config = CustomMessageConfig()
            config.enableUnreadCount = false
            tip.config = config
            NIMClient.getService(MsgService::class.java).saveMessageToLocal(tip, true)
        }
    }

    private fun appendTeamMemberPush(message: IMMessage?) {
        if (aitManager == null) {
            return
        }
        if (sessionType == SessionTypeEnum.Team) {
            val pushList = aitManager!!.aitTeamMember
            if (pushList == null || pushList.isEmpty()) {
                return
            }
            val memberPushOption = MemberPushOption()
            memberPushOption.isForcePush = true
            memberPushOption.forcePushContent = message!!.content
            memberPushOption.forcePushList = pushList
            message.memberPushOption = memberPushOption
        }
    }

    private fun changeToRobotMsg(message: IMMessage?): IMMessage? {
        var message = message
        if (aitManager == null) {
            return message
        }
        if (message!!.msgType == MsgTypeEnum.robot) {
            return message
        }
        val robotAccount = aitManager!!.aitRobot
        if (TextUtils.isEmpty(robotAccount)) {
            return message
        }
        val text = message.content
        var content = aitManager!!.removeRobotAitString(text, robotAccount)
        content = if (content == "") " " else content
        message = MessageBuilder.createRobotMessage(
            message.sessionId,
            message.sessionType,
            robotAccount,
            text,
            RobotMsgType.TEXT,
            content,
            null,
            null
        )
        return message
    }

    private fun appendPushConfig(message: IMMessage?) {
        val customConfig = NimUIKitImpl.getCustomPushContentProvider() ?: return
        val content = customConfig.getPushContent(message)
        val payload = customConfig.getPushPayload(message)
        if (!TextUtils.isEmpty(content)) {
            message!!.pushContent = content
        }
        if (payload != null) {
            message!!.pushPayload = payload
        }
    }

    override fun onInputPanelExpand() {
        messageListPanel!!.scrollToBottom()
    }

    override fun shouldCollapseInputPanel() {
        inputPanel!!.collapse(false)
    }

    override val isLongClickEnabled: Boolean
        get() = !inputPanel!!.isRecording

    override fun onItemFooterClick(message: IMMessage?) {
        if (aitManager == null) {
            return
        }
    }

    override fun onReplyMessage(message: IMMessage?) {
        inputPanel!!.replyMessage = message
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (aitManager != null) {
            aitManager!!.onActivityResult(requestCode, resultCode, data)
        }
        inputPanel!!.onActivityResult(requestCode, resultCode, data)
        messageListPanel!!.onActivityResult(requestCode, resultCode, data)
    }

    // 操作面板集合
    protected val actionList: List<BaseAction>
        protected get() {
            val actions: MutableList<BaseAction> = ArrayList()
            if (customization != null && customization!!.actions != null) {
                actions.addAll(customization!!.actions)
            } else {
                actions.add(SelectImageAction())
                actions.add(TakePictureAction())
            }
            return actions
        }

    companion object {
        protected const val TAG = "MessageActivity"

        fun newInstance(account: String?, type: SessionTypeEnum?,customization: SessionCustomization?): MessageFragment =
            MessageFragment().apply {
                arguments = Bundle().apply {
                    putString(Extras.EXTRA_ACCOUNT, account)
                    putSerializable(Extras.EXTRA_TYPE, type)
                    putSerializable(Extras.EXTRA_CUSTOMIZATION, customization)
                }
            }
    }
}