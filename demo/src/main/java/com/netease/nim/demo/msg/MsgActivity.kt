package com.netease.nim.demo.msg

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.ImageView
import com.netease.nim.demo.R
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.zxn.mvvm.view.BaseActivity
import com.zxn.netease.nimsdk.api.model.session.SessionCustomization
import com.zxn.netease.nimsdk.business.session.constant.Extras
import com.zxn.netease.nimsdk.business.session.fragment.MessageFragment
import com.zxn.netease.nimsdk.business.uinfo.UserInfoHelper
import com.zxn.netease.nimsdk.common.ToastHelper
import kotlinx.android.synthetic.main.activity_msg.*

/**
 *自定义点对点单聊消息页面
 */
class MsgActivity : BaseActivity<Nothing>() {

    companion object {

        @JvmStatic
        fun jumpTo(context: Context, account: String?, customization: SessionCustomization) {
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
                intent.getSerializableExtra(Extras.EXTRA_CUSTOMIZATION) as SessionCustomization
            )
        }
    }

    override fun onInitView() {
        onInitTitle()
        onJumpTo(intent) { account, customization ->
            titleView.titleText = UserInfoHelper.getUserTitleName(account, SessionTypeEnum.P2P)
            supportFragmentManager.beginTransaction()
                .add(R.id.fl_container, MessageFragment.newInstance(account, SessionTypeEnum.P2P,customization))
                .commitAllowingStateLoss()
        }

        /*onJumpTo(intent)?.let {
            supportFragmentManager.beginTransaction()
                .add(R.id.fl_container, MessageFragment.newInstance(it, SessionTypeEnum.P2P))
                .commitAllowingStateLoss()

        }*/

    }

    override fun registerEventBus(isRegister: Boolean) {

    }

    private fun onInitTitle() {

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