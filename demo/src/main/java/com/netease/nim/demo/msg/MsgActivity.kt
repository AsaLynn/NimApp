package com.netease.nim.demo.msg

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.ImageView
import com.netease.nim.demo.R
import com.zxn.mvvm.view.BaseActivity
import com.zxn.netease.nimsdk.business.session.fragment.MessageFragment
import com.zxn.netease.nimsdk.common.ToastHelper
import kotlinx.android.synthetic.main.activity_msg.*

class MsgActivity : BaseActivity<Nothing>() {

    companion object {

        @JvmStatic
        fun jumpTo(context: Context, account: String?) {
            context.startActivity(Intent(context, MsgActivity::class.java).apply {
                account?.let {
                    putExtra("account", it)
                }
            })
        }

        private fun onJumpTo(intent: Intent): String? {
            val account = intent.getStringExtra("account")
            Log.i("MsgActivity", "onJumpTo: $account")
            return account
        }
    }

    override fun onInitView() {
        onInitTitle()
        supportFragmentManager.beginTransaction()
            .add(R.id.fl_container, MessageFragment())
            .commitAllowingStateLoss()
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