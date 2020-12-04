package com.netease.nim.demo.contact.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.netease.nim.demo.DemoCache.getAccount
import com.netease.nim.demo.R
import com.netease.nimlib.sdk.ResponseCode
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo
import com.zxn.netease.nimsdk.api.NimUIKit
import com.zxn.netease.nimsdk.api.model.SimpleCallback
import com.zxn.netease.nimsdk.api.wrapper.NimToolBarOptions
import com.zxn.netease.nimsdk.common.ToastHelper.showToast
import com.zxn.netease.nimsdk.common.activity.ToolBarOptions
import com.zxn.netease.nimsdk.common.activity.UI
import com.zxn.netease.nimsdk.common.ui.dialog.DialogMaker
import com.zxn.netease.nimsdk.common.ui.dialog.EasyAlertDialogHelper
import com.zxn.netease.nimsdk.common.ui.widget.ClearableEditTextWithIcon

/**
 * 添加好友页面
 */
class AddFriendActivity : UI() {
    private var searchEdit: ClearableEditTextWithIcon? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_friend_activity)
        val options: ToolBarOptions = NimToolBarOptions()
        options.titleId = R.string.add_buddy
        setToolBar(R.id.toolbar, options)
        findViews()
        initActionbar()
    }

    private fun findViews() {
        searchEdit = findView(R.id.search_friend_edit)
    }

    private fun initActionbar() {
        val toolbarView = findView<TextView>(R.id.action_bar_right_clickable_textview)
        //toolbarView.setText(R.string.search)
        toolbarView.setOnClickListener { v: View? ->
            if (TextUtils.isEmpty(
                    searchEdit!!.text.toString()
                )
            ) {
                showToast(this@AddFriendActivity, "not_allow_empty")
            } else if (searchEdit!!.text.toString() == getAccount()) {
                showToast(this@AddFriendActivity, R.string.add_friend_self_tip)
            } else {
                query()
            }
        }
    }

    private fun query() {
        DialogMaker.showProgressDialog(this, null, false)
        val account = searchEdit!!.text.toString().toLowerCase()
        NimUIKit.getUserInfoProvider()
            .getUserInfoAsync(account, object : SimpleCallback<NimUserInfo?> {
                override fun onResult(success: Boolean, result: NimUserInfo?, code: Int) {
                    DialogMaker.dismissProgressDialog()
                    if (success) {
                        if (result == null) {
                            EasyAlertDialogHelper.showOneButtonDiolag(
                                this@AddFriendActivity, R.string.user_not_exsit,
                                R.string.user_tips, R.string.ok, false, null
                            )
                        } else {
                            UserProfileActivity.start(this@AddFriendActivity, account)
                        }
                    } else if (code == 408) {
                        showToast(this@AddFriendActivity, R.string.network_is_not_available)
                    } else if (code == ResponseCode.RES_EXCEPTION.toInt()) {
                        showToast(this@AddFriendActivity, "on exception")
                    } else {
                        showToast(this@AddFriendActivity, "on failed:$code")
                    }
                }
            })
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent()
            intent.setClass(context, AddFriendActivity::class.java)
            context.startActivity(intent)
        }
    }
}