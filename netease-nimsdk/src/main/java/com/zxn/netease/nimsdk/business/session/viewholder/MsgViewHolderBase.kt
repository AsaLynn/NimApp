package com.zxn.netease.nimsdk.business.session.viewholder

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.*
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.NIMSDK
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.attachment.FileAttachment
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.zxn.netease.nimsdk.R
import com.zxn.netease.nimsdk.api.NimUIKit
import com.zxn.netease.nimsdk.business.session.helper.MessageHelper
import com.zxn.netease.nimsdk.business.session.module.list.MsgAdapter
import com.zxn.netease.nimsdk.business.uinfo.UserInfoHelper
import com.zxn.netease.nimsdk.common.ui.imageview.HeadImageView
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter
import com.zxn.netease.nimsdk.common.ui.recyclerview.holder.BaseViewHolder
import com.zxn.netease.nimsdk.common.ui.recyclerview.holder.RecyclerViewHolder
import com.zxn.netease.nimsdk.common.util.log.sdk.wrapper.NimLog
import com.zxn.netease.nimsdk.common.util.sys.TimeUtil
import com.zxn.netease.nimsdk.impl.NimUIKitImpl
import java.util.*

/**
 * 会话窗口消息列表项的ViewHolder基类，负责每个消息项的外层框架，包括头像，昵称，发送/接收进度条，重发按钮等。<br></br>
 * 具体的消息展示项可继承该基类，然后完成具体消息内容展示即可。
 */
abstract class MsgViewHolderBase(override var adapter: BaseMultiItemFetchLoadAdapter<*, *>) :
    RecyclerViewHolder<BaseMultiItemFetchLoadAdapter<*, *>, BaseViewHolder, IMMessage?>(adapter) {

    //    private ViewGroup llNoticeContainer;
    // basic
    protected var view: View? = null
    protected var context: Context? = null
    protected var layoutPosition = 0

    // data
    protected var message: IMMessage? = null

    // view
    protected lateinit var alertButton: View
    protected var timeTextView: TextView? = null
    protected open var progressBar: ProgressBar? = null
    protected var nameTextView: TextView? = null
    protected var contentContainer: FrameLayout? = null
    protected var contentContainerWithReplyTip //包含回复提示的内容部分
            : LinearLayout? = null
    protected var replyTipAboveMsg //消息列表中，显示在消息体上方的回复提示
            : TextView? = null
    protected var nameContainer: LinearLayout? = null
    protected var readReceiptTextView: TextView? = null
    protected var ackMsgTextView: TextView? = null
    protected var replyTipTextView //回复消息时，显示在回复框上方的回复提示
            : TextView? = null
    protected var pinTipImg: ImageView? = null
    private var avatarLeft: HeadImageView? = null
    private var avatarRight: HeadImageView? = null

    /**
     * 合并转发用多选框
     */
    private var multiCheckBox: CheckBox? = null
    var nameIconView: ImageView? = null

    // contentContainerView的默认长按事件。如果子类需要不同的处理，可覆盖onItemLongClick方法
    // 但如果某些子控件会拦截触摸消息，导致contentContainer收不到长按事件，子控件也可在inflate时重新设置
    protected var longClickListener: OnLongClickListener? = null

    /// -- 以下接口可由子类覆盖或实现
    // 返回具体消息类型内容展示区域的layout res id
    abstract val contentResId: Int

    // 在该接口中根据layout对各控件成员变量赋值
    abstract fun inflateContentView()

    // 在该接口操作BaseViewHolder中的数据，进行事件绑定，可选
    protected fun bindHolder(holder: BaseViewHolder?) {}

    // 将消息数据项与内容的view进行绑定
    abstract fun bindContentView()

    // 内容区域点击事件响应处理。
    open fun onItemClick() {}

    // 内容区域长按事件响应处理。该接口的优先级比adapter中有长按事件的处理监听高，当该接口返回为true时，adapter的长按事件监听不会被调用到。
    protected open fun onItemLongClick(): Boolean {
        return false
    }

    // 当是接收到的消息时，内容区域背景的drawable id
    protected open fun leftBackground(): Int {
        return NimUIKitImpl.getOptions().messageLeftBackground
    }

    // 当是发送出去的消息时，内容区域背景的drawable id
    protected open fun rightBackground(): Int {
        return NimUIKitImpl.getOptions().messageRightBackground
    }

    // 返回该消息是不是居中显示
    protected open val isMiddleItem: Boolean
        protected get() = false

    //为Thread消息的设置回复提示语
    @SuppressLint("StringFormatMatches")
    protected fun setBeRepliedTip() {
        var count = 0
        if (message!!.isThread) {
            count = NIMClient.getService(MsgService::class.java)
                .queryReplyCountInThreadTalkBlock(message)
        }
        if (count <= 0) {
            replyTipTextView!!.visibility = View.GONE
            return
        }
        replyTipTextView!!.text = String.format(
            context!!.resources.getString(R.string.reply_with_amount),
            count
        )
        replyTipTextView!!.visibility = View.VISIBLE
    }

    protected fun setReplyTip() {
        if (message!!.isThread) {
            replyTipAboveMsg!!.visibility = View.GONE
            return
        }
        replyTipAboveMsg!!.text = replyTip
        replyTipAboveMsg!!.visibility = View.VISIBLE
    }

    //thread消息没有回复对象
    protected val replyTip: String
        protected get() {
            //thread消息没有回复对象
            if (message!!.isThread) {
                return ""
            }
            val threadOption = message!!.threadOption
            val replyFrom = threadOption.replyMsgFromAccount
            if (TextUtils.isEmpty(replyFrom)) {
                NimLog.w("MsgViewHolderBase", "no reply message found, uuid=" + message!!.uuid)
                return ""
            }
            val fromDisplayName = UserInfoHelper.getUserDisplayNameInSession(
                replyFrom,
                message!!.sessionType,
                message!!.sessionId
            )
            val replyUuid = threadOption.replyMsgIdClient
            val content = getMessageBrief(replyUuid, "...")
            return String.format(
                context!!.getString(R.string.reply_with_message),
                fromDisplayName,
                content
            )
        }

    protected fun getMessageBrief(uuid: String?, defaultValue: String): String {
        if (TextUtils.isEmpty(uuid)) {
            return defaultValue
        }
        val uuidList: MutableList<String?> = ArrayList(1)
        uuidList.add(uuid)
        val msgList = NIMClient.getService(
            MsgService::class.java
        ).queryMessageListByUuidBlock(uuidList)
        if (msgList == null || msgList.isEmpty()) {
            return defaultValue
        }
        val msg = msgList[0]
        val sessionCustomization =
            if (SessionTypeEnum.P2P == msg.sessionType) NimUIKit.getCommonP2PSessionCustomization() else NimUIKit.getCommonTeamSessionCustomization()
        return sessionCustomization.getMessageDigest(msg)
    }

    // 是否显示头像，默认为显示
    protected open val isShowHeadImage: Boolean
        protected get() = true

    // 是否显示气泡背景，默认为显示
    protected open val isShowBubble: Boolean
        protected get() = true

    // 是否显示已读，默认为显示
    protected open fun shouldDisplayReceipt(): Boolean {
        return true
    }

    /// -- 以下接口可由子类调用
    protected val msgAdapter: MsgAdapter
        get() = adapter as MsgAdapter

    protected fun shouldDisplayNick(): Boolean {
        return message!!.sessionType == SessionTypeEnum.Team && isReceivedMessage && !isMiddleItem
    }

    /**
     * 下载附件/缩略图
     */
    protected fun downloadAttachment(callback: RequestCallback<Void?>?) {
        if (message!!.attachment != null && message!!.attachment is FileAttachment) NIMClient.getService(
            MsgService::class.java
        ).downloadAttachment(message, true).setCallback(callback)
    }

    // 设置FrameLayout子控件的gravity参数
    protected fun setGravity(view: View, gravity: Int) {
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = gravity
        view.layoutParams = params
    }

    // 设置控件的长宽
    protected fun setLayoutParams(width: Int, height: Int, vararg views: View) {
        for (view in views) {
            val maskParams = view.layoutParams
            maskParams.width = width
            maskParams.height = height
            view.layoutParams = maskParams
        }
    }

    // 根据layout id查找对应的控件
    protected fun <T : View?> findViewById(id: Int): T {
        return view!!.findViewById<View>(id) as T
    }

    // 判断消息方向，是否是接收到的消息
    protected val isReceivedMessage: Boolean
        protected get() = message!!.direct == MsgDirectionEnum.In

    /// -- 以下是基类实现代码
    override fun convert(
        holder: BaseViewHolder,
        data: IMMessage?,
        position: Int,
        isScrolling: Boolean
    ) {
        view = holder.convertView
        context = holder.context!!
        message = data
        layoutPosition = holder.layoutPosition
        inflate()
        refresh()
        /*if (position == 0) {
            View noticeView = holder.getNoticeView();
            if (llNoticeContainer.getChildCount() == 0) {
                if (noticeView != null) {
                    llNoticeContainer.addView(noticeView);
                }
            }else {
                llNoticeContainer.setVisibility(View.VISIBLE);
            }
        } else {
            llNoticeContainer.setVisibility(View.GONE);
        }*/
        //tvNotice.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        bindHolder(holder)
    }

    fun initParameter(itemView: View?, context: Context?, data: IMMessage?, position: Int) {
        view = itemView
        this.context = context!!
        message = data!!
        layoutPosition = position
        timeTextView = TextView(context)
        avatarLeft = HeadImageView(context)
        avatarRight = HeadImageView(context)
        multiCheckBox = CheckBox(context)
        alertButton = View(context)
        progressBar = ProgressBar(context)
        nameTextView = TextView(context)
        contentContainer = FrameLayout(context!!)
        contentContainerWithReplyTip = LinearLayout(context)
        replyTipAboveMsg = TextView(context)
        nameIconView = ImageView(context)
        nameContainer = LinearLayout(context)
        readReceiptTextView = TextView(context)
        ackMsgTextView = TextView(context)
    }

    protected fun inflate() {
        //llNoticeContainer = findViewById(R.id.llNoticeContainer);
        timeTextView = findViewById<TextView>(R.id.message_item_time)
        avatarLeft = findViewById<HeadImageView>(R.id.message_item_portrait_left)
        avatarRight = findViewById<HeadImageView>(R.id.message_item_portrait_right)
        multiCheckBox = findViewById<CheckBox>(R.id.message_item_multi_check_box)
        alertButton = findViewById<View>(R.id.message_item_alert)
        progressBar = findViewById<ProgressBar>(R.id.message_item_progress)
        nameTextView = findViewById<TextView>(R.id.message_item_nickname)
        contentContainer = findViewById<FrameLayout>(R.id.message_item_content)
        contentContainerWithReplyTip =
            findViewById<LinearLayout>(R.id.message_item_container_with_reply_tip)
        replyTipAboveMsg = findViewById<TextView>(R.id.tv_reply_tip_above_msg)
        nameIconView = findViewById<ImageView>(R.id.message_item_name_icon)
        nameContainer = findViewById<LinearLayout>(R.id.message_item_name_layout)
        readReceiptTextView = findViewById<TextView>(R.id.textViewAlreadyRead)
        ackMsgTextView = findViewById<TextView>(R.id.team_ack_msg)
        pinTipImg = findViewById<ImageView>(R.id.message_item_pin)
        replyTipTextView = findViewById<TextView>(R.id.message_item_reply)

        // 这里只要inflate出来后加入一次即可
        if (contentContainer!!.childCount == 0) {
            View.inflate(view!!.context, contentResId, contentContainer)
        }
        inflateContentView()
    }

    protected fun refresh() {
        //如果是avchat类消息，先根据附件的from字段重置消息的方向和发送者ID
        MessageHelper.adjustAVChatMsgDirect(message)
        setHeadImageView()
        setNameTextView()
        setTimeTextView()
        setStatus()
        setOnClickListener()
        setLongClickListener()
        setContent()
        setExtension()
        setReadReceipt()
        setAckMsg()
        setMultiCheckBox()
        bindContentView()
    }

    fun refreshCurrentItem() {
        if (message != null) {
            refresh()
        }
    }

    /**
     * 设置时间显示
     */
    private fun setTimeTextView() {
        if (msgAdapter.needShowTime(message)) {
            timeTextView!!.visibility = View.VISIBLE
        } else {
            timeTextView!!.visibility = View.GONE
            return
        }
        val text = TimeUtil.getTimeShowString(message!!.time, false)
        timeTextView!!.text = text
    }

    /**
     * 设置消息发送状态
     */
    private fun setStatus() {
        val status = message!!.status
        when (status) {
            MsgStatusEnum.fail -> {
                progressBar!!.visibility = View.GONE
                alertButton!!.visibility = View.VISIBLE
            }
            MsgStatusEnum.sending -> {
                progressBar!!.visibility = View.VISIBLE
                alertButton!!.visibility = View.GONE
            }
            else -> {
                progressBar!!.visibility = View.GONE
                alertButton!!.visibility = View.GONE
            }
        }
    }

    private fun setHeadImageView() {
        val show = if (isReceivedMessage) avatarLeft else avatarRight
        val hide = if (isReceivedMessage) avatarRight else avatarLeft
        hide!!.visibility = View.GONE
        if (!isShowHeadImage) {
            show!!.visibility = View.GONE
            return
        }
        if (isMiddleItem) {
            show!!.visibility = View.GONE
        } else {
            show!!.visibility = View.VISIBLE
            show.loadBuddyAvatar(message)
        }
    }

    private fun setOnClickListener() {
        //消息是否处于可被选择状态，true: 点击只能改变被选择状态; false: 点击可执行消息的点击事件
        val inNormalMode = message!!.isChecked == null
        multiCheckBox!!.setOnClickListener { v: View? ->
            msgAdapter.eventListener!!.onCheckStateChanged(
                layoutPosition,
                multiCheckBox!!.isChecked
            )
        }
        if (!inNormalMode) {
            alertButton!!.isClickable = false
            contentContainer!!.isClickable = false
            avatarLeft!!.isClickable = false
            avatarRight!!.isClickable = false
            ackMsgTextView!!.isClickable = false
            return
        }
        // 重发/重收按钮响应事件
        if (msgAdapter.eventListener != null) {
            alertButton!!.setOnClickListener { msgAdapter.eventListener!!.onFailedBtnClick(message) }
        }

        // 内容区域点击事件响应， 相当于点击了整项
        contentContainer!!.setOnClickListener { v: View? -> onItemClick() }


        // 头像点击事件响应
        if (NimUIKitImpl.getSessionListener() != null) {
            val portraitListener = View.OnClickListener { v: View? ->
                NimUIKitImpl.getSessionListener().onAvatarClicked(
                    context!!, message!!
                )
            }
            avatarLeft!!.setOnClickListener(portraitListener)
            avatarRight!!.setOnClickListener(portraitListener)
        }
        // 已读回执响应事件
        if (NimUIKitImpl.getSessionListener() != null) {
            ackMsgTextView!!.setOnClickListener { v: View? ->
                NimUIKitImpl.getSessionListener().onAckMsgClicked(
                    context!!, message!!
                )
            }
        }
    }

    /**
     * item长按事件监听
     */
    private fun setLongClickListener() {
        longClickListener = OnLongClickListener { v: View? ->
            // 优先派发给自己处理，
            if (!onItemLongClick()) {
                if (msgAdapter.eventListener != null) {
                    msgAdapter.eventListener!!.onViewHolderLongClick(
                        contentContainer,
                        view,
                        message
                    )
                    true
                }
            }
            false
        }
        // 消息长按事件响应处理
        contentContainer!!.setOnLongClickListener(longClickListener)

        // 头像长按事件响应处理
        if (NimUIKitImpl.getSessionListener() != null) {
            val longClickListener = OnLongClickListener {
                NimUIKitImpl.getSessionListener().onAvatarLongClicked(context!!, message!!)
                true
            }
            avatarLeft!!.setOnLongClickListener(longClickListener)
            avatarRight!!.setOnLongClickListener(longClickListener)
        }
    }

    private fun setNameTextView() {
        if (!shouldDisplayNick()) {
            nameTextView!!.visibility = View.GONE
            return
        }
        nameTextView!!.visibility = View.VISIBLE
        nameTextView!!.text = nameText
    }

    protected val nameText: String
        protected get() = ""

    private fun setContent() {
        if (!isShowBubble && !isMiddleItem) {
            return
        }
        val bodyContainer = view!!.findViewById<LinearLayout>(R.id.message_item_body)

        // 调整container的位置
        val index = if (isReceivedMessage) 0 else 4
        if (bodyContainer.getChildAt(index) !== contentContainerWithReplyTip) {
            bodyContainer.removeView(contentContainerWithReplyTip)
            bodyContainer.addView(contentContainerWithReplyTip, index)
        }
        if (isMiddleItem) {
            setGravity(bodyContainer, Gravity.CENTER)
        } else {
            if (isReceivedMessage) {
                setGravity(bodyContainer, Gravity.LEFT)
                //contentContainerWithReplyTip.setBackgroundResource(leftBackground());
                replyTipAboveMsg!!.setTextColor(Color.BLACK)
            } else {
                setGravity(bodyContainer, Gravity.RIGHT)
                //contentContainerWithReplyTip.setBackgroundResource(rightBackground());
                replyTipAboveMsg!!.setTextColor(Color.WHITE)
            }
        }
    }

    private fun setExtension() {
        if (!isShowBubble && !isMiddleItem) {
            return
        }
        val extensionContainer = view!!.findViewById<LinearLayout>(R.id.message_item_extension)

        // 调整扩展功能提示的位置
        val index = if (isReceivedMessage) 0 else 1
        if (extensionContainer.getChildAt(index) !== pinTipImg) {
            extensionContainer.removeView(pinTipImg)
            extensionContainer.addView(pinTipImg, index)
        }
        if (isMiddleItem) {
            return
        }
        setGravity(extensionContainer, if (isReceivedMessage) Gravity.LEFT else Gravity.RIGHT)
        setBeRepliedTip()
        setReplyTip()
    }

    private fun setReadReceipt() {
        if (shouldDisplayReceipt() && !TextUtils.isEmpty(msgAdapter.uuid) && message!!.uuid == msgAdapter.uuid) {
            readReceiptTextView!!.visibility = View.VISIBLE
        } else {
            readReceiptTextView!!.visibility = View.GONE
        }
    }

    private fun setAckMsg() {
        if (message!!.sessionType == SessionTypeEnum.Team && message!!.needMsgAck()) {
            if (isReceivedMessage) {
                // 收到的需要已读回执的消息，需要给个反馈
                ackMsgTextView!!.visibility = View.GONE
                NIMSDK.getTeamService().sendTeamMessageReceipt(message)
            } else {
                // 自己发的需要已读回执的消息，显示未读人数
                ackMsgTextView!!.visibility = View.VISIBLE
                if (message!!.teamMsgAckCount == 0 && message!!.teamMsgUnAckCount == 0) {
                    ackMsgTextView!!.text = "还未查看"
                } else {
                    ackMsgTextView!!.text = message!!.teamMsgUnAckCount.toString() + "人未读"
                }
            }
        } else {
            ackMsgTextView!!.visibility = View.GONE
        }
    }

    private fun setMultiCheckBox() {
        val selectState = message!!.isChecked
        multiCheckBox!!.visibility =
            if (selectState == null) View.GONE else View.VISIBLE
        if (java.lang.Boolean.TRUE == selectState) {
            multiCheckBox!!.isChecked = true
        } else if (java.lang.Boolean.FALSE == selectState) {
            multiCheckBox!!.isChecked = false
        }
    }
}