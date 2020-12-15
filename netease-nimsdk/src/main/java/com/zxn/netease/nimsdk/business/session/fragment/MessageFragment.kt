package com.zxn.netease.nimsdk.business.session.fragment

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
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
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.MessageReceipt
import com.zxn.netease.nimsdk.R
import com.zxn.netease.nimsdk.api.model.session.SessionCustomization
import com.zxn.netease.nimsdk.business.ait.AitManager
import com.zxn.netease.nimsdk.business.session.actions.BaseAction
import com.zxn.netease.nimsdk.business.session.actions.ImageAction
import com.zxn.netease.nimsdk.business.session.constant.Extras
import com.zxn.netease.nimsdk.business.session.module.Container
import com.zxn.netease.nimsdk.business.session.module.ModuleProxy
import com.zxn.netease.nimsdk.business.session.module.input.InputPanel
import com.zxn.netease.nimsdk.business.session.module.list.MessageListPanelEx
import com.zxn.netease.nimsdk.common.fragment.TFragment
import com.zxn.netease.nimsdk.impl.NimUIKitImpl
import com.zxn.utils.UIUtils
import java.io.Serializable

/**
 * 聊天界面基类
 */
open class MessageFragment : TFragment(), ModuleProxy {

    /**
     * 发送结果回调
     */
    var sendCallback: RequestCallback<Void?>? = null


    /**
     * 消息收发监听
     */
    var mOnMsgPassedListener: OnMsgPassedListener? = null

    private var mContainer: Container? = null

    /**
     *
     */
    var anchor: IMMessage? = null

    private var rootView: View? = null

    /**
     * 自定义扩展功能.
     */
    private var customization: SessionCustomization? = null

    /**
     * p2p对方Account或者群id
     */
    protected var sessionId: String? = null

    /**
     * 会话类型
     */
    protected var sessionType: SessionTypeEnum? = null

    /**
     * 输入模块
     */
    private var inputPanel: InputPanel? = null

    /**
     * 消息展示模块
     */
    protected var messageListPanel: MessageListPanelEx? = null

    /**
     *文本变化监听.
     */
    private var aitManager: AitManager? = null

    /**
     * 操作+号的面板集合
     */
    private val actionList: MutableList<BaseAction>
        get() {
            val actions: MutableList<BaseAction> =
                mutableListOf(ImageAction())
            customization?.let {
                it.actions?.let { actionList ->
                    actions.clear()
                    actions.addAll(actionList)
                }
            }
            return actions
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.nim_message_fragment, container, false)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        UIUtils.init(context)
        onArguments(arguments) { account, sessionType, anchor, customization ->
            this.sessionId = account
            this.sessionType = sessionType
            this.anchor = anchor
            this.customization = customization

            mContainer = Container(activity, account, sessionType, this, true)

            msgReload()

            initAitManager()

            registerObservers(true)

            customization?.let {
                messageListPanel?.setChattingBackground(
                    it.backgroundUri,
                    it.backgroundColor
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        messageListPanel?.onResume()
        NIMClient.getService(MsgService::class.java).setChattingAccount(sessionId, sessionType)
        activity?.volumeControlStream = AudioManager.STREAM_VOICE_CALL // 默认使用听筒播放
    }

    override fun onPause() {
        super.onPause()
        NIMClient.getService(MsgService::class.java)
            .setChattingAccount(MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None)
        inputPanel?.onPause()
        messageListPanel?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        messageListPanel?.onDestroy()
        registerObservers(false)
        inputPanel?.onDestroy()
        if (aitManager != null) {
            aitManager!!.reset()
        }
    }

    fun onBackPressed(): Boolean {
        return inputPanel?.collapse(true) ?: false
    }

    /**
     * 刷新消息列表UI.
     */
    fun refreshMessageList() {
        messageListPanel?.refreshMessageList()
    }


    /**
     * 重新加载消息列表
     */
    fun msgReload() {
        mContainer?.let {
            if (messageListPanel == null) {
                messageListPanel =
                    MessageListPanelEx(it, rootView, anchor, false, false, customization)
            } else {
                messageListPanel?.reload(it, anchor)
            }

            if (inputPanel == null) {
                inputPanel = InputPanel(it, rootView, actionList, true, customization)
            } else {
                inputPanel?.reload(it, customization)
            }
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
            inputPanel?.addAitTextWatcher(aitManager)
            aitManager?.setTextChangeListener(inputPanel)
        }
    }

    /**
     * ************************* 消息收发 **********************************
     */
    /**
     * 是否允许发送消息
     */
    private fun isAllowSendMessage(message: IMMessage?): Boolean {
        customization?.let {
            return it.isAllowSendMessage(message)
        }
        return true
    }

    private fun registerObservers(register: Boolean) {
        val mMsgServiceObserve = NIMClient.getService(MsgServiceObserve::class.java)
        mMsgServiceObserve.observeReceiveMessage(mMsgComeInObserver, register)
        // 已读回执监听
        if (NimUIKitImpl.getOptions().shouldHandleReceipt) {
            mMsgServiceObserve.observeMessageReceipt(messageReceiptObserver, register)
        }
    }

    /**
     *  消息接收观察者
     */
    private var mMsgComeInObserver = Observer<List<IMMessage>> {
        if (it.isNotEmpty()) {
            messageListPanel?.onIncomingMessage(it)
            // 发送已读回执
            messageListPanel?.sendReceipt()
            mOnMsgPassedListener?.onMsgPassed(it)
        }
    }

    /**
     * 已读回执观察者
     */
    private val messageReceiptObserver: Observer<List<MessageReceipt>> =
        Observer<List<MessageReceipt>> {
            messageListPanel?.receiveReceipt()
        }

    /**
     * ********************** implements ModuleProxy *********************
     */

    /**
     * 发送IM消息.
     */
    override fun sendMessage(msg: IMMessage?): Boolean {
        msg?.let {
            if (isAllowSendMessage(msg)) {
                appendPushConfigAndSend(msg)
            } else {
                // 替换成tip
                NIMClient.getService(MsgService::class.java)
                    .saveMessageToLocal(
                        MessageBuilder.createTipMessage(
                            msg.sessionId,
                            msg.sessionType
                        ).apply {
                            this.content = "该消息无法发送"
                            this.status = MsgStatusEnum.success
                        }, false
                    )
            }
            messageListPanel?.onMsgSend(msg)
            aitManager?.reset()
            mOnMsgPassedListener?.onMsgPassed(mutableListOf(msg))
        }
        return true
    }

    private fun appendPushConfigAndSend(message: IMMessage?) {
        message?.let {
            appendPushConfig(message)
            val service = NIMClient.getService(MsgService::class.java)
            // send message to server and save to db
            val replyMsg = inputPanel?.replyMessage
            if (replyMsg == null) {
                service.sendMessage(message, false)
                    .setCallback(object : RequestCallback<Void?> {
                        override fun onSuccess(param: Void?) {
                            Log.i(TAG, "onSuccess: $param")
                            sendCallback?.onSuccess(param)
                        }

                        override fun onFailed(code: Int) {
                            Log.i(TAG, "onFailed: $code")
                            if (code == ResponseCode.RES_IN_BLACK_LIST.toInt()) {
                                sendFailWithBlackList(code, message)
                            } else {
                                it.status = MsgStatusEnum.fail
                                NIMClient.getService(MsgService::class.java)
                                    .updateIMMessageStatus(it)
                            }
                            sendCallback?.onFailed(code)
                        }

                        override fun onException(exception: Throwable) {
                            Log.i(TAG, "onException: ${exception.message}")
                            sendCallback?.onException(exception)
                        }
                    })
            } else {
                service.replyMessage(message, replyMsg, false)
                    .setCallback(object : RequestCallback<Void?> {
                        override fun onSuccess(param: Void?) {
                            Log.i(TAG, "replyMessage,onSuccess: ")
                            val threadId = message.threadOption.threadMsgIdClient
                            messageListPanel?.refreshMessageItem(threadId)
                        }

                        override fun onFailed(code: Int) {
                            Log.i(TAG, "replyMessage,onFailed: $code")
                            sendFailWithBlackList(code, message)
                        }

                        override fun onException(exception: Throwable) {
                            Log.i(TAG, "replyMessage,onFailed: ${exception.message}")
                        }
                    })
            }
            inputPanel?.resetReplyMessage()
        }
    }

    /**
     * 被对方拉入黑名单后，发消息失败的交互处理
     */
    private fun sendFailWithBlackList(code: Int, msg: IMMessage?) {
        msg?.let {
            if (code == ResponseCode.RES_IN_BLACK_LIST.toInt()) {
                // 如果被对方拉入黑名单，发送的消息前不显示重发红点
                it.status = MsgStatusEnum.success
                NIMClient.getService(MsgService::class.java).updateIMMessageStatus(it)
                messageListPanel?.refreshMessageList()
                // 同时，本地插入被对方拒收的tip消息
                val tip = MessageBuilder.createTipMessage(it.sessionId, it.sessionType)
                tip.content = activity?.getString(R.string.black_list_send_tip)
                tip.status = MsgStatusEnum.success
                val config = CustomMessageConfig()
                config.enableUnreadCount = false
                tip.config = config
                NIMClient.getService(MsgService::class.java).saveMessageToLocal(tip, true)
            }
        }
    }


    private fun appendPushConfig(message: IMMessage?) {
        val customConfig = NimUIKitImpl.getCustomPushContentProvider() ?: return
        val content = customConfig.getPushContent(message)
        val payload = customConfig.getPushPayload(message)
        if (!TextUtils.isEmpty(content)) {
            message?.pushContent = content
        }
        if (payload != null) {
            message?.pushPayload = payload
        }
    }

    override fun onInputPanelExpand() {
        messageListPanel?.scrollToBottom()
    }

    override fun shouldCollapseInputPanel() {
        inputPanel?.collapse(false)
    }

    override val isLongClickEnabled: Boolean =
        if (inputPanel == null) false else !(inputPanel!!.isRecording)

    override fun onItemFooterClick(message: IMMessage?) {
        if (aitManager == null) {
            return
        }
    }

    override fun onReplyMessage(message: IMMessage?) {
        inputPanel?.replyMessage = message
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (aitManager != null) {
            aitManager!!.onActivityResult(requestCode, resultCode, data)
        }
        inputPanel?.onActivityResult(requestCode, resultCode, data)
        messageListPanel?.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * 消息进出监听.
     */
    interface OnMsgPassedListener : Serializable {
        fun onMsgPassed(msgList: List<IMMessage>)


//        fun onFailed(code: Int)
    }

    companion object {

        protected const val TAG = "MessageFragment"

        /**
         * account:账号
         * type:回话类型
         * customization:自定义属性.
         */
        @JvmStatic
        fun newInstance(
            account: String?,
            type: SessionTypeEnum?,
            customization: SessionCustomization?
        ): MessageFragment =
            MessageFragment().apply {
                arguments = Bundle().apply {
                    putString(Extras.EXTRA_ACCOUNT, account)
                    putSerializable(Extras.EXTRA_TYPE, type)
                    putSerializable(Extras.EXTRA_CUSTOMIZATION, customization)
                }
            }

        /**
         * 解析数据
         */
        @JvmStatic
        fun onArguments(
            arguments: Bundle?,
            block: (String?, SessionTypeEnum?, IMMessage?, SessionCustomization?) -> Unit
        ) {
            arguments?.let {
                block(
                    it.getString(Extras.EXTRA_ACCOUNT),
                    it.getSerializable(Extras.EXTRA_TYPE) as SessionTypeEnum?,
                    it.getSerializable(Extras.EXTRA_ANCHOR) as IMMessage?,
                    it.getSerializable(Extras.EXTRA_CUSTOMIZATION) as SessionCustomization?
                )
            }
        }
    }

}

/*private fun appendTeamMemberPush(message: IMMessage?) {
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
    }*/

/*private fun changeToRobotMsg(message: IMMessage?): IMMessage? {
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
}*/