package com.netease.nim.demo.msg

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.ImageView
import com.netease.nim.demo.R
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.zxn.mvvm.view.BaseActivity
import com.zxn.netease.nimsdk.api.model.session.SessionCustomization
import com.zxn.netease.nimsdk.business.session.actions.BaseAction
import com.zxn.netease.nimsdk.business.session.actions.SelectImageAction
import com.zxn.netease.nimsdk.business.session.actions.TakePictureAction
import com.zxn.netease.nimsdk.business.session.constant.Extras
import com.zxn.netease.nimsdk.business.session.fragment.MessageFragment
import com.zxn.netease.nimsdk.business.uinfo.UserInfoHelper
import com.zxn.netease.nimsdk.common.ToastHelper
import com.zxn.time.StampUtils
import com.zxn.time.TimeUnitPattern
import com.zxn.utils.UIUtils
import kotlinx.android.synthetic.main.activity_msg.*
import java.text.DecimalFormat
import java.util.*

/**
 *自定义点对点单聊消息页面.
 */
class MsgActivity : BaseActivity<Nothing>() {

    companion object {

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

        private fun onJumpTo(intent: Intent, block: (String?, SessionCustomization?) -> Unit) {
            val account = intent.getStringExtra("account")
            Log.i("MsgActivity", "onJumpTo: $account")

            block(
                account,
                intent.getSerializableExtra(Extras.EXTRA_CUSTOMIZATION) as SessionCustomization?
            )
        }
    }

    private var mCustomization: SessionCustomization = SessionCustomization().apply {

        this.backgroundColor = UIUtils.getColor(R.color.colorPrimary)

        this.actions = ArrayList<BaseAction>().apply {
            add(SelectImageAction())
            add(TakePictureAction())
        }

    }

    override fun onInitView() {
        onInitTitle()
        onJumpTo(intent) { account, customization ->
            titleView.titleText = UserInfoHelper.getUserTitleName(account, SessionTypeEnum.P2P)
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.fl_container, MessageFragment.newInstance(
                        account,
                        SessionTypeEnum.P2P,
                        customization ?: mCustomization
                    )
                ).commitAllowingStateLoss()
        }
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
                showToast("titleView")
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
}