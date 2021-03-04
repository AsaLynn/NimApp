package com.zxn.netease.nimsdk.business.session.module.input

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.SystemClock
import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.alibaba.fastjson.JSONObject
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.media.record.AudioRecorder
import com.netease.nimlib.sdk.media.record.IAudioRecordCallback
import com.netease.nimlib.sdk.media.record.RecordType
import com.netease.nimlib.sdk.msg.MessageBuilder
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomNotification
import com.netease.nimlib.sdk.msg.model.CustomNotificationConfig
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.zxn.netease.nimsdk.R
import com.zxn.netease.nimsdk.api.NimUIKit
import com.zxn.netease.nimsdk.api.model.session.SessionCustomization
import com.zxn.netease.nimsdk.business.ait.AitTextChangeListener
import com.zxn.netease.nimsdk.business.session.actions.BaseAction
import com.zxn.netease.nimsdk.business.session.buttons.ButtonType
import com.zxn.netease.nimsdk.business.session.emoji.EmoticonPickerView
import com.zxn.netease.nimsdk.business.session.emoji.IEmoticonSelectedListener
import com.zxn.netease.nimsdk.business.session.emoji.MoonUtil
import com.zxn.netease.nimsdk.business.session.module.Container
import com.zxn.netease.nimsdk.business.uinfo.UserInfoHelper
import com.zxn.netease.nimsdk.common.ToastHelper.showToast
import com.zxn.netease.nimsdk.common.ui.dialog.EasyAlertDialogHelper
import com.zxn.netease.nimsdk.common.ui.dialog.EasyAlertDialogHelper.OnDialogActionListener
import com.zxn.netease.nimsdk.common.util.log.LogUtil
import com.zxn.netease.nimsdk.common.util.string.StringUtil
import com.zxn.netease.nimsdk.impl.NimUIKitImpl
import com.zxn.utils.UIUtils
import java.io.File

/**
 * 底部文本编辑，语音等模块
 */
open class InputPanel @JvmOverloads constructor(
    protected var container: Container,
    protected var view: View?,
    private var actions: MutableList<BaseAction>,
    isTextAudioSwitchShow: Boolean = true,
    private var customization: SessionCustomization? = null
) : IEmoticonSelectedListener, IAudioRecordCallback, AitTextChangeListener {

    // TODO: 2021/3/2 校验输入框中的消息回调
    var sendClickListener: ((String) -> Boolean)? = null

    private var uiHandler: Handler

    private var actionPanelBottomLayout // 更多布局
            : View? = null

    private var messageActivityBottomLayout: LinearLayout? = null

    /**
     * 文本消息编辑框
     */
    protected var messageEditText
            : EditText? = null

    /**
     * 录音按钮
     */
    private var audioRecordBtn
            : Button? = null

    /**
     * 录音动画布局
     */
    private var audioAnimLayout
            : View? = null

    /**
     * 切换文本，语音按钮布局
     */
    private var textAudioSwitchLayout
            : FrameLayout? = null

    /**
     * 文本消息选择按钮
     */
    private var switchToTextButtonInInputBar: View? = null

    /**
     * 语音消息选择按钮
     */
    private var switchToAudioButtonInInputBar
            : View? = null

    /***
     * 更多消息发送选择按钮
     */
    private var moreFuntionButtonInInputBar
            : View? = null

    /**
     * 发送消息按钮
     */
    private var sendMessageButtonInInputBar
            : View? = null

    /**
     * emoji选择按钮
     */
    private var emojiButtonInInputBar
            : View? = null

    /**
     *
     */
    private var messageInputBar: View? = null

    /**
     * 被回复消息信息
     */
    protected var replyInfoTv
            : TextView? = null

    protected var replyLayout: View? = null

    /**
     * 取消回复消息的按钮
     */
    protected var cancelReplyImg
            : ImageView? = null

    /**
     *  贴图表情控件
     */
    private var emoticonPickerView
            : EmoticonPickerView? = null

    // 语音
    protected var audioMessageHelper: AudioRecorder? = null
    private var time: Chronometer? = null
    private var timerTip: TextView? = null
    private var timerTipContainer: LinearLayout? = null
    private var started = false
    private var cancelled = false
    private var touched = false // 是否按着
    private var isKeyboardShowed = true // 是否显示键盘

    // state
    private var actionPanelBottomLayoutHasSetup = false
    private var isTextAudioSwitchShow = true

    // data
    private var typingTime: Long = 0
    private var isRobotSession = false
    private var aitTextWatcher: TextWatcher? = null

    @JvmField
    var replyMessage: IMMessage? = null

    fun onPause() {
        // 停止录音
        if (audioMessageHelper != null) {
            onEndAudioRecord(true)
        }
    }

    fun onDestroy() {
        // release
        if (audioMessageHelper != null) {
            audioMessageHelper!!.destroyAudioRecorder()
        }
    }

    fun collapse(immediately: Boolean): Boolean {
        val respond = (emoticonPickerView != null && emoticonPickerView!!.visibility == View.VISIBLE
                || actionPanelBottomLayout != null && actionPanelBottomLayout!!.visibility == View.VISIBLE)
        hideAllInputLayout(immediately)
        return respond
    }

    fun addAitTextWatcher(watcher: TextWatcher?) {
        aitTextWatcher = watcher
    }

    /**
     * 初始化.
     */
    private fun init() {
        initViews()
        initInputBarListener()
        initTextEdit()
        initAudioRecordButton()
        restoreText(false)
        for (i in actions.indices) {
            actions[i].setIndex(i)
            actions[i].container = container
        }
    }

    fun getReplyMessage(): IMMessage? {
        return replyMessage
    }

    fun setReplyMessage(replyMessage: IMMessage?) {
        this.replyMessage = replyMessage
        refreshReplyMsgLayout()
    }

    fun resetReplyMessage() {
        setReplyMessage(null)
    }

    fun setCustomization(customization: SessionCustomization?) {
        this.customization = customization
        if (customization != null) {
            emoticonPickerView!!.setWithSticker(customization.withSticker)
        }
    }

    fun reload(container: Container, customization: SessionCustomization?) {
        this.container = container
        setCustomization(customization)
    }

    private fun initViews() {
        // input bar

        view?.let {

            messageActivityBottomLayout = it.findViewById(R.id.messageActivityBottomLayout)
            messageInputBar = it.findViewById(R.id.textMessageLayout)
            switchToTextButtonInInputBar = it.findViewById(R.id.buttonTextMessage)
            switchToAudioButtonInInputBar = it.findViewById(R.id.buttonAudioMessage)
            moreFuntionButtonInInputBar = it.findViewById(R.id.buttonMoreFuntionInText)

            emojiButtonInInputBar = it.findViewById(R.id.emoji_button)

            sendMessageButtonInInputBar = it.findViewById(R.id.buttonSendMessage)
            messageEditText = it.findViewById(R.id.editTextMessage)
            replyInfoTv = it.findViewById(R.id.tvReplyInfo)
            replyLayout = it.findViewById(R.id.layout_reply)
            cancelReplyImg = it.findViewById(R.id.imgCancelReply)

            // 语音
            audioRecordBtn = it.findViewById(R.id.audioRecord)
            audioAnimLayout = it.findViewById(R.id.layoutPlayAudio)
            time = it.findViewById(R.id.timer)
            timerTip = it.findViewById(R.id.timer_tip)
            timerTipContainer = it.findViewById(R.id.timer_tip_container)

            // 表情
            emoticonPickerView = it.findViewById(R.id.emoticon_picker_view)

            // 显示录音按钮
            switchToTextButtonInInputBar!!.visibility = View.GONE
            switchToAudioButtonInInputBar!!.visibility = View.VISIBLE

            // 文本录音按钮切换布局
            textAudioSwitchLayout = it.findViewById(R.id.flSwitchLayout)
            if (isTextAudioSwitchShow) {
                textAudioSwitchLayout!!.visibility = View.VISIBLE
            } else {
                textAudioSwitchLayout?.visibility = View.GONE
            }

            switchToTextButtonInInputBar!!.setOnClickListener(clickListener)
            switchToAudioButtonInInputBar!!.setOnClickListener(clickListener)


            val mInputBtnContainer: LinearLayout = it.findViewById(R.id.llInputBtnContainer)
            customization?.let { sessionCustomization ->
                mInputBtnContainer.removeAllViews()
                sessionCustomization.bottomButtonList?.let { buttonList ->
                    for (button in buttonList) {
                        when (button.buttonType) {
                            ButtonType.AUDIO -> {
                                //音频点击切换操作:
                                //textAudioSwitchLayout?.removeAllViews()
                                //textAudioSwitchLayout?.removeView(switchToAudioButtonInInputBar)
                                if (button.backIconId != 0) {
                                    switchToAudioButtonInInputBar?.setBackgroundResource(button.backIconId)
                                    switchToAudioButtonInInputBar?.setOnClickListener { v ->
                                        button.onClick(
                                            v,
                                            this@InputPanel,
                                            container.account
                                        )
                                    }
                                }
                            }
                            else -> {
                                val layoutParams = LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                                layoutParams.leftMargin =
                                    UIUtils.getDimensionPixelSize(R.dimen.dp_15)
                                mInputBtnContainer.addView(
                                    ImageView(container.activity).apply {
                                        if (button.backIconId != 0) {
                                            setBackgroundResource(button.backIconId)
                                            setOnClickListener { buttonView ->
                                                when (button.buttonType) {
                                                    //1 -> hideEmojiLayout()
                                                    2 -> hideEmojiLayout()
                                                }
                                                hideActionPanelLayout()
                                                button.onClick(
                                                    buttonView,
                                                    this@InputPanel,
                                                    container.account
                                                )
                                            }
                                        }
                                    }, layoutParams
                                )
                            }
                        }
                    }
                }
            }


        }
    }

    private fun initInputBarListener() {

        emojiButtonInInputBar?.setOnClickListener(clickListener)
        sendMessageButtonInInputBar!!.setOnClickListener(clickListener)
        moreFuntionButtonInInputBar!!.setOnClickListener(clickListener)
        cancelReplyImg!!.setOnClickListener(clickListener)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initTextEdit() {
        messageEditText!!.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        messageEditText!!.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                switchToTextLayout(true)
            }
            false
        }
        messageEditText!!.onFocusChangeListener =
            OnFocusChangeListener { v: View?, hasFocus: Boolean ->
                messageEditText!!.hint = ""
                checkSendButtonEnable(messageEditText)
            }
        messageEditText!!.addTextChangedListener(object : TextWatcher {
            private var start = 0
            private var count = 0
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                this.start = start
                this.count = count
                if (aitTextWatcher != null) {
                    aitTextWatcher!!.onTextChanged(s, start, before, count)
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                if (aitTextWatcher != null) {
                    aitTextWatcher!!.beforeTextChanged(s, start, count, after)
                }
            }

            override fun afterTextChanged(s: Editable) {
                checkSendButtonEnable(messageEditText)
                MoonUtil.replaceEmoticons(container.activity, s, start, count)
                var editEnd = messageEditText!!.selectionEnd
                messageEditText!!.removeTextChangedListener(this)
                while (StringUtil.counterChars(s.toString()) > NimUIKitImpl.getOptions().maxInputTextLength && editEnd > 0) {
                    s.delete(editEnd - 1, editEnd)
                    editEnd--
                }
                messageEditText!!.setSelection(editEnd)
                messageEditText!!.addTextChangedListener(this)
                if (aitTextWatcher != null) {
                    aitTextWatcher!!.afterTextChanged(s)
                }
                sendTypingCommand()
            }
        })
    }

    /**
     * 发送“正在输入”通知
     */
    private fun sendTypingCommand() {
        if (container.account == NimUIKit.getAccount()) {
            return
        }
        if (container.sessionType == SessionTypeEnum.Team || container.sessionType == SessionTypeEnum.ChatRoom) {
            return
        }
        if (System.currentTimeMillis() - typingTime > 5000L) {
            typingTime = System.currentTimeMillis()
            val command = CustomNotification()
            command.sessionId = container.account
            command.sessionType = container.sessionType
            val config = CustomNotificationConfig()
            config.enablePush = false
            config.enableUnreadCount = false
            command.config = config
            val json = JSONObject()
            json["id"] = "1"
            command.content = json.toString()
            NIMClient.getService(MsgService::class.java).sendCustomNotification(command)
        }
    }

    /**
     * ************************* 键盘布局切换 *******************************
     */
    /**
     * 底部可输入部分的点击
     */
    private val clickListener = View.OnClickListener { v ->
        when {
            v === switchToTextButtonInInputBar -> {
                switchToTextLayout(true) // 显示文本发送的布局
            }
            v === sendMessageButtonInInputBar -> {
                onTextMessageSendButtonPressed()
            }
            v === switchToAudioButtonInInputBar -> {
                switchToAudio()
            }
            v === moreFuntionButtonInInputBar -> {
                toggleActionPanelLayout()
            }
            v === emojiButtonInInputBar -> {
                toggleEmojiLayout()
            }
            v === cancelReplyImg -> {
                cancelReply()
            }
        }
    }

    /**
     * 切换可以录音状态
     */
    fun switchToAudio() {
        switchToAudioLayout()
        view?.let {
            requestRecordAudioPermission(it.context)
        }
    }

    private fun requestRecordAudioPermission(context: Context) {
        XXPermissions.with(context)
            .permission(Permission.RECORD_AUDIO)
            .permission(*Permission.Group.STORAGE)
            .request(object : OnPermissionCallback {

                override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                    if (all) {
                        toast("获取录音权限成功")
                    } else {
                        toast("获取录音权限失败")
                    }
                }

                override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                    if (never) {
                        toast("被拒绝授权，请手动授予录音")
                        // 如果是被永久拒绝就跳转到应用权限系统设置页面
                        XXPermissions.startPermissionActivity(context, permissions)
                    } else {
                        toast("获取录音权限失败")
                    }
                }
            })
    }

    /**
     * 点击edittext，切换键盘和更多布局
     */
    private fun switchToTextLayout(needShowInput: Boolean) {
        hideEmojiLayout()
        hideActionPanelLayout()
        audioRecordBtn!!.visibility = View.GONE
        messageEditText!!.visibility = View.VISIBLE
        switchToTextButtonInInputBar!!.visibility = View.GONE
        switchToAudioButtonInInputBar!!.visibility = View.VISIBLE
        messageInputBar!!.visibility = View.VISIBLE
        if (needShowInput) {
            uiHandler.postDelayed(showTextRunnable, SHOW_LAYOUT_DELAY.toLong())
        } else {
            hideInputMethod()
        }
    }

    /**
     * 点击发送按钮对输入框文本进行校验,true代表检验通过可发送消息,false代表检验不通过进行消息拦截并处理.
     * 默认值是true.
     */
    private var isCheckedPass = true

    // 发送文本消息
    private fun onTextMessageSendButtonPressed() {
        val text = messageEditText!!.text.toString()
        sendClickListener?.let {
            isCheckedPass = it.invoke(text)
        }
        if (isCheckedPass) {
            val textMessage = createTextMessage(text)
            if (container.proxy.sendMessage(textMessage)) {
                restoreText(true)
            }
        }
    }

    private fun createTextMessage(text: String?): IMMessage {
        return MessageBuilder.createTextMessage(container.account, container.sessionType, text)
    }

    // 切换成音频，收起键盘，按钮切换成键盘
    private fun switchToAudioLayout() {
        messageEditText!!.visibility = View.GONE
        audioRecordBtn!!.visibility = View.VISIBLE
        hideInputMethod()
        hideEmojiLayout()
        hideActionPanelLayout()
        switchToAudioButtonInInputBar!!.visibility = View.GONE
        switchToTextButtonInInputBar!!.visibility = View.VISIBLE
    }

    /**
     * // 点击“+”号按钮，切换更多布局和键盘
     */
    private fun toggleActionPanelLayout() {
        if (actionPanelBottomLayout == null || actionPanelBottomLayout!!.visibility == View.GONE) {
            showActionPanelLayout()
        } else {
            hideActionPanelLayout()
        }
    }

    /**
     * 点击表情，切换到表情布局
     */
    fun toggleEmojiLayout() {
        emoticonPickerView?.let {
            if (it.visibility == View.GONE) {
                showEmojiLayout()
            } else {
                hideEmojiLayout()
            }
        }
    }

    private fun cancelReply() {
        resetReplyMessage()
    }

    /**
     * 隐藏表情布局
     */
    private fun hideEmojiLayout() {
        uiHandler.removeCallbacks(showEmojiRunnable)
        if (emoticonPickerView != null) {
            emoticonPickerView!!.visibility = View.GONE
        }
    }

    /**
     * 隐藏更多布局
     */
    private fun hideActionPanelLayout() {
        uiHandler.removeCallbacks(showMoreFuncRunnable)
        if (actionPanelBottomLayout != null) {
            actionPanelBottomLayout!!.visibility = View.GONE
        }
    }

    /**
     * // 隐藏键盘布局
     */
    private fun hideInputMethod() {
        isKeyboardShowed = false
        uiHandler.removeCallbacks(showTextRunnable)
        val imm =
            container.activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(messageEditText!!.windowToken, 0)
        messageEditText!!.clearFocus()
    }

    /**
     * // 隐藏语音布局
     */
    private fun hideAudioLayout() {
        audioRecordBtn!!.visibility = View.GONE
        messageEditText!!.visibility = View.VISIBLE
        switchToTextButtonInInputBar!!.visibility = View.VISIBLE
        switchToAudioButtonInInputBar!!.visibility = View.GONE
    }

    /**
     * // 显示表情布局
     */
    private fun showEmojiLayout() {
        hideInputMethod()
        hideActionPanelLayout()
        hideAudioLayout()
        messageEditText!!.requestFocus()
        uiHandler.postDelayed(showEmojiRunnable, 200)
        emoticonPickerView!!.visibility = View.VISIBLE
        emoticonPickerView!!.show(this)
        container.proxy.onInputPanelExpand()
    }

    /**
     * // 初始化更多布局
     */
    private fun addActionPanelLayout() {
        if (actionPanelBottomLayout == null) {
            View.inflate(
                container.activity,
                R.layout.nim_message_activity_actions_layout,
                messageActivityBottomLayout
            )
            view?.let {
                actionPanelBottomLayout = view?.findViewById(R.id.actionsLayout)
                actionPanelBottomLayoutHasSetup = false
            }
        }
        initActionPanelLayout()
    }

    /**
     * // 显示键盘布局
     */
    private fun showInputMethod(editTextMessage: EditText?) {
        editTextMessage!!.requestFocus()
        //如果已经显示,则继续操作时不需要把光标定位到最后
        if (!isKeyboardShowed) {
            editTextMessage.setSelection(editTextMessage.text.length)
            isKeyboardShowed = true
        }
        val imm =
            container.activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editTextMessage, 0)
        container.proxy.onInputPanelExpand()
    }

    /**
     * // 显示更多布局
     */
    private fun showActionPanelLayout() {
        addActionPanelLayout()
        hideEmojiLayout()
        hideInputMethod()
        uiHandler.postDelayed(showMoreFuncRunnable, SHOW_LAYOUT_DELAY.toLong())
        container.proxy.onInputPanelExpand()
    }

    /**
     * // 显示回复消息信息
     */
    private fun refreshReplyMsgLayout() {
        if (replyMessage == null) {
            replyLayout!!.visibility = View.GONE
            return
        }
        uiHandler.postDelayed(showTextRunnable, SHOW_LAYOUT_DELAY.toLong())
        val fromDisplayName = UserInfoHelper.getUserDisplayNameInSession(
            replyMessage!!.fromAccount, replyMessage!!.sessionType, replyMessage!!.sessionId
        )
        val content = customization!!.getMessageDigest(replyMessage)
        val text = String.format(
            container.activity!!.getString(R.string.reply_with_message),
            fromDisplayName,
            content
        )
        replyInfoTv!!.text = text
        replyLayout!!.visibility = View.VISIBLE
    }

    // 初始化具体more layout中的项目
    private fun initActionPanelLayout() {
        if (actionPanelBottomLayoutHasSetup) {
            return
        }
        ActionsPanel.init(view, actions)
        actionPanelBottomLayoutHasSetup = true
    }

    private val showEmojiRunnable = Runnable { emoticonPickerView!!.visibility = View.VISIBLE }
    private val showMoreFuncRunnable =
        Runnable { actionPanelBottomLayout!!.visibility = View.VISIBLE }
    private val showTextRunnable = Runnable { showInputMethod(messageEditText) }

    /**
     * 清空输入框.
     */
    fun restoreText(clearText: Boolean) {
        if (clearText) {
            messageEditText!!.setText("")
        }
        checkSendButtonEnable(messageEditText)
    }

    /**
     * 显示发送或更多
     *
     * @param editText
     */
    private fun checkSendButtonEnable(editText: EditText?) {
        if (isRobotSession) {
            return
        }
        val textMessage = editText!!.text.toString()
        if (!TextUtils.isEmpty(StringUtil.removeBlanks(textMessage)) && editText.hasFocus()) {
            moreFuntionButtonInInputBar!!.visibility = View.GONE
            sendMessageButtonInInputBar!!.visibility = View.VISIBLE
        } else {
            sendMessageButtonInInputBar!!.visibility = View.GONE
            moreFuntionButtonInInputBar!!.visibility = View.VISIBLE
        }
    }

    /**
     * *************** IEmojiSelectedListener ***************
     */
    override fun onEmojiSelected(key: String?) {
        val mEditable = messageEditText!!.text
        if (key == "/DEL") {
            messageEditText!!.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
        } else {
            var start = messageEditText!!.selectionStart
            var end = messageEditText!!.selectionEnd
            start = if (start < 0) 0 else start
            end = if (start < 0) 0 else end
            mEditable.replace(start, end, key)
        }
    }

    private var hideAllInputLayoutRunnable: Runnable? = null
    override fun onStickerSelected(category: String?, item: String?) {
        Log.i("InputPanel", "onStickerSelected, category =$category, sticker =$item")
        if (customization != null) {
            val attachment = customization!!.createStickerAttachment(category, item)
            val stickerMessage = MessageBuilder.createCustomMessage(
                container.account,
                container.sessionType,
                "贴图消息",
                attachment
            )
            container.proxy.sendMessage(stickerMessage)
        }
    }

    override fun onTextAdd(content: String, start: Int, length: Int) {
        if (messageEditText!!.visibility != View.VISIBLE ||
            emoticonPickerView != null && emoticonPickerView!!.visibility == View.VISIBLE
        ) {
            switchToTextLayout(true)
        } else {
            uiHandler.postDelayed(showTextRunnable, SHOW_LAYOUT_DELAY.toLong())
        }
        messageEditText!!.editableText.insert(start, content)
    }

    override fun onTextDelete(start: Int, length: Int) {
        if (messageEditText!!.visibility != View.VISIBLE) {
            switchToTextLayout(true)
        } else {
            uiHandler.postDelayed(showTextRunnable, SHOW_LAYOUT_DELAY.toLong())
        }
        val end = start + length - 1
        messageEditText!!.editableText.replace(start, end, "")
    }

    val editSelectionStart: Int
        get() = messageEditText!!.selectionStart

    /**
     * 隐藏所有输入布局
     */
    private fun hideAllInputLayout(immediately: Boolean) {
        if (hideAllInputLayoutRunnable == null) {
            hideAllInputLayoutRunnable = Runnable {
                hideInputMethod()
                hideActionPanelLayout()
                hideEmojiLayout()
            }
        }
        val delay = if (immediately) 0 else ViewConfiguration.getDoubleTapTimeout().toLong()
        uiHandler.postDelayed(hideAllInputLayoutRunnable!!, delay)
    }

    /**
     * ****************************** 语音 ***********************************
     */
    @SuppressLint("ClickableViewAccessibility")
    private fun initAudioRecordButton() {
        audioRecordBtn!!.setOnTouchListener { v: View, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                touched = true
                initAudioRecord()
                onStartAudioRecord()
            } else if (event.action == MotionEvent.ACTION_CANCEL
                || event.action == MotionEvent.ACTION_UP
            ) {
                touched = false
                onEndAudioRecord(isCancelled(v, event))
            } else if (event.action == MotionEvent.ACTION_MOVE) {
                touched = true
                cancelAudioRecord(isCancelled(v, event))
            }
            false
        }
    }

    private fun toast(text: String) {
        UIUtils.toast(text)
    }

    /**
     * 初始化AudioRecord
     */
    private fun initAudioRecord() {
        if (audioMessageHelper == null) {
            val options = NimUIKitImpl.getOptions()
            audioMessageHelper = AudioRecorder(
                container.activity,
                options.audioRecordType,
                options.audioRecordMaxTime,
                this
            )
        }
    }

    /**
     * 开始语音录制
     */
    private fun onStartAudioRecord() {
        container.activity!!.window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        audioMessageHelper!!.startRecord()
        cancelled = false
    }

    /**
     * 结束语音录制
     *
     * @param cancel
     */
    private fun onEndAudioRecord(cancel: Boolean) {
        started = false
        container.activity!!.window.setFlags(0, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        audioMessageHelper!!.completeRecord(cancel)
        audioRecordBtn!!.setText(R.string.record_audio)
        audioRecordBtn!!.setBackgroundResource(R.drawable.nim_message_input_edittext_box)
        stopAudioRecordAnim()
    }

    /**
     * 取消语音录制
     *
     * @param cancel
     */
    private fun cancelAudioRecord(cancel: Boolean) {
        // reject
        if (!started) {
            return
        }
        // no change
        if (cancelled == cancel) {
            return
        }
        cancelled = cancel
        updateTimerTip(cancel)
    }

    /**
     * 正在进行语音录制和取消语音录制，界面展示
     *
     * @param cancel
     */
    private fun updateTimerTip(cancel: Boolean) {
        if (cancel) {
            timerTip!!.setText(R.string.recording_cancel_tip)
            timerTipContainer!!.setBackgroundResource(R.drawable.nim_cancel_record_red_bg)
        } else {
            timerTip!!.setText(R.string.recording_cancel)
            timerTipContainer!!.setBackgroundResource(0)
        }
    }

    /**
     * 开始语音录制动画
     */
    private fun playAudioRecordAnim() {
        audioAnimLayout!!.visibility = View.VISIBLE
        time!!.base = SystemClock.elapsedRealtime()
        time!!.start()
    }

    /**
     * 结束语音录制动画
     */
    private fun stopAudioRecordAnim() {
        audioAnimLayout!!.visibility = View.GONE
        time!!.stop()
        time!!.base = SystemClock.elapsedRealtime()
    }

    // 录音状态回调
    override fun onRecordReady() {}

    override fun onRecordStart(audioFile: File, recordType: RecordType) {
        started = true
        if (!touched) {
            return
        }
        audioRecordBtn!!.setText(R.string.record_audio_end)
        audioRecordBtn!!.setBackgroundResource(R.drawable.nim_message_input_edittext_box_pressed)
        updateTimerTip(false) // 初始化语音动画状态
        playAudioRecordAnim()
    }

    override fun onRecordSuccess(audioFile: File, audioLength: Long, recordType: RecordType) {
        val audioMessage = MessageBuilder.createAudioMessage(
            container.account,
            container.sessionType,
            audioFile,
            audioLength
        )
        container.proxy.sendMessage(audioMessage)
    }

    override fun onRecordFail() {
        if (started) {
            showToast(container.activity!!, R.string.recording_error)
        }
    }

    override fun onRecordCancel() {}

    override fun onRecordReachedMaxTime(maxTime: Int) {
        stopAudioRecordAnim()
        EasyAlertDialogHelper.createOkCancelDiolag(container.activity,
            "",
            container.activity!!.getString(
                R.string.recording_max_time
            ),
            false,
            object : OnDialogActionListener {
                override fun doCancelAction() {}
                override fun doOkAction() {
                    audioMessageHelper!!.handleEndRecord(true, maxTime)
                }
            }).show()
    }

    val isRecording: Boolean
        get() = audioMessageHelper != null && audioMessageHelper!!.isRecording

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        var index = requestCode shl 16 shr 24
        if (index != 0) {
            index--
            if (index < 0 || index >= actions.size) {
                LogUtil.d(TAG, "request code out of actions' range")
                return
            }
            val action = actions[index]
            action.onActivityResult(requestCode and 0xff, resultCode, data)
        }
    }

    companion object {
        private const val TAG = "MsgSendLayout"
        private const val SHOW_LAYOUT_DELAY = 200

        // 上滑取消录音判断
        private fun isCancelled(view: View, event: MotionEvent): Boolean {
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            return event.rawX < location[0] || event.rawX > location[0] + view.width || event.rawY < location[1] - 40
        }
    }

    init {
        this.uiHandler = Handler()
        this.isTextAudioSwitchShow = isTextAudioSwitchShow
        init()
    }
}