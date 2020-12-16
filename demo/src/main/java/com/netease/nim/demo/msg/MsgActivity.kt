package com.netease.nim.demo.msg

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.netease.nim.demo.R
import com.netease.nim.demo.session.action.GuessAction
import com.netease.nim.demo.session.action.SelectImageAction
import com.netease.nim.demo.session.action.TakePictureAction
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallback
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.zxn.mvvm.view.BaseActivity
import com.zxn.netease.nimsdk.api.model.session.SessionCustomization
import com.zxn.netease.nimsdk.business.session.actions.BaseAction
import com.zxn.netease.nimsdk.business.session.buttons.ButtonType
import com.zxn.netease.nimsdk.business.session.buttons.InputButton
import com.zxn.netease.nimsdk.business.session.constant.Extras
import com.zxn.netease.nimsdk.business.session.fragment.MessageFragment
import com.zxn.netease.nimsdk.business.session.module.input.InputPanel
import com.zxn.netease.nimsdk.business.uinfo.UserInfoHelper
import com.zxn.netease.nimsdk.common.ToastHelper
import com.zxn.time.StampUtils
import com.zxn.time.TimeUnitPattern
import com.zxn.utils.UIUtils
import kotlinx.android.synthetic.main.activity_msg.*
import java.text.DecimalFormat

/**
 *自定义点对点单聊消息页面.
 */
class MsgActivity : BaseActivity<Nothing>(), RequestCallback<Void?> {

    companion object {
        private const val TAG = "MsgActivity"
        var mCpValue = 0
        val mCustomization: SessionCustomization = SessionCustomization().apply {

            this.backgroundColor = UIUtils.getColor(R.color.colorPrimary)

            this.headerLayoutId = R.layout.msg_notice_header

            this.actions = ArrayList<BaseAction>().apply {
                add(SelectImageAction())
                add(TakePictureAction())
            }

            bottomButtonList =
                ArrayList<InputButton>().apply {
                    add(object :
                        InputButton(R.drawable.nim_message_button_bottom_gift_selector) {

                        override var buttonType: Int = 2

                        override fun onClick(
                            view: View?,
                            inputPanel: InputPanel,
                            sessionId: String?
                        ) {
                            Log.i("TAG", "sessionId: $sessionId")
                            //点击礼物发送

                        }
                    })
                    add(object :
                        InputButton(R.drawable.nim_message_button_bottom_emoji_selector) {

                        override var buttonType: Int = 1

                        override fun onClick(
                            view: View?,
                            inputPanel: InputPanel,
                            sessionId: String?
                        ) {
                            Log.i("TAG", "sessionId: $sessionId")
                            //点击表情包
                            inputPanel.toggleEmojiLayout()
                        }
                    })
                    add(object :
                        InputButton(R.drawable.nim_message_button_bottom_audio_selector) {

                        override var buttonType: Int = ButtonType.AUDIO

                        override fun onClick(
                            view: View?,
                            inputPanel: InputPanel,
                            sessionId: String?
                        ) {
                            Log.i("TAG", "sessionId: $sessionId")
                            if (mCpValue < 100) {
                                UIUtils.toast("mCpValue( $mCpValue )")
                                return
                            }
                            //点击录音按钮
                            inputPanel.switchToAudio()
                        }
                    })
                }

            this.actions = ArrayList<BaseAction>().apply {
                add(object : SelectImageAction() {
                    override fun onClick() {
                        Log.i("TAG", "onClick: mCpValue( $mCpValue )")
                        if (mCpValue < 100) {
                            UIUtils.toast("mCpValue( $mCpValue )")
                            return
                        }
                        super.onClick()
                    }
                })
                add(object : TakePictureAction() {
                    override fun onClick() {
                        if (mCpValue < 0) {
                            UIUtils.toast("mCpValue( $mCpValue )")
                            return
                        }
                        super.onClick()
                    }
                })
                add(GuessAction())
            }
        }

        @JvmStatic
        fun jumpTo(
            context: Context,
            account: String?,
            customization: SessionCustomization? = null
        ) {
            context.startActivity(Intent(context, MsgActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                account?.let {
                    putExtra("account", it)
                    putExtra(Extras.EXTRA_CUSTOMIZATION, customization)
                }
            })
        }

        @JvmStatic
        private fun onJumpTo(intent: Intent, block: (String?, SessionCustomization?) -> Unit) {
            val account = intent.getStringExtra("account")
            Log.i("MsgActivity", "onJumpTo: $account")

            block(
                account,
                intent.getSerializableExtra(Extras.EXTRA_CUSTOMIZATION) as SessionCustomization?
            )
        }
    }

    private lateinit var mMessageFragment: MessageFragment

    override fun onInitView() {

        mCpValue = 90

        onInitTitle()

        onJumpTo(intent) { account, customization ->
            titleView.titleText = UserInfoHelper.getUserTitleName(account, SessionTypeEnum.P2P)
            mMessageFragment = MessageFragment.newInstance(
                account,
                SessionTypeEnum.P2P,
                customization ?: mCustomization
            ).apply {

                this.mOnMsgPassedListener = object : MessageFragment.OnMsgPassedListener {

                    override fun onMsgPassed(msgList: List<IMMessage>) {
                        for (msg in msgList) {
                            if (msg.msgType == MsgTypeEnum.text) {
                                showToast(msg.content)
                            }
                        }
                    }
                }

                this.sendCallback = object : RequestCallback<Void?> {
                    override fun onSuccess(param: Void?) {
                        Log.i(TAG, "onSuccess: $param")
                    }

                    override fun onFailed(code: Int) {
                        Log.i(TAG, "onFailed: $code")
                    }

                    override fun onException(exception: Throwable?) {
                        Log.i(TAG, "onException: ${exception?.message}")
                    }
                }
            }
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.fl_container, mMessageFragment
                ).commitAllowingStateLoss()
        }




        mMessageFragment.sendCallback = this
    }


    override fun registerEventBus(isRegister: Boolean) {

    }

    private fun onInitTitle() {
        val startTime = 1607068800000
        val howLong = StampUtils.howLong(startTime, TimeUnitPattern.DAY, DecimalFormat("0.000000"))
        val text = """相处${howLong}天  在线"""
        val spannableString = SpannableString(text)
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#6A6A86")),
            0,
            (text.length - 2),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#B0B0BD")),
            (text.length - 2),
            text.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        tvOnlineTime.text = spannableString
        titleView.addRightView(ImageView(mContext).apply {
            setOnClickListener {
                showToast("删除聊天记录成功!")
                onJumpTo(intent) { account, _ ->
                    account?.let {
                        clearChattingHistory(it)
                    }
                }
            }
            setImageResource(R.mipmap.more_icon)
        })
    }

    override val layoutResId: Int = R.layout.activity_msg

    override fun createObserver() {

    }

    override fun showToast(msg: String) {
        ToastHelper.showToast(mContext, msg)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (::mMessageFragment.isInitialized) {
            mMessageFragment.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * 清除与指定用户的所有消息记录，且不在数据库记录此次操作
     */
    private fun clearChattingHistory(account: String) {
        NIMClient.getService(MsgService::class.java)
            .clearChattingHistory(account, SessionTypeEnum.P2P)
        mMessageFragment.msgReload()
    }

    override fun onSuccess(param: Void?) {
        Log.i(TAG, "onSuccess: $param")
    }

    override fun onFailed(code: Int) {
        Log.i(TAG, "onFailed: $code")
    }

    override fun onException(exception: Throwable?) {
        Log.i(TAG, "onException: ${exception?.message}")
    }

}