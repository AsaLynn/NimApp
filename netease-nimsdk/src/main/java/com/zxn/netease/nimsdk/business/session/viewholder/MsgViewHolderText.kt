package com.zxn.netease.nimsdk.business.session.viewholder

import android.graphics.Color
import android.text.method.LinkMovementMethod
import android.text.style.ImageSpan
import android.view.View
import android.widget.TextView
import com.zxn.netease.nimsdk.R
import com.zxn.netease.nimsdk.api.NimUIKit
import com.zxn.netease.nimsdk.business.session.emoji.MoonUtil
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter
import com.zxn.netease.nimsdk.common.util.sys.ScreenUtil
import com.zxn.netease.nimsdk.impl.NimUIKitImpl

/**
 *文本消息
 */
open class MsgViewHolderText(adapter: BaseMultiItemFetchLoadAdapter<*, *>) :
    MsgViewHolderBase(adapter) {

    private var bodyTextView: TextView? = null

    override val contentResId: Int = R.layout.nim_message_item_text


    override fun inflateContentView() {
        bodyTextView = findViewById(R.id.nim_message_item_text_body)
    }

    override fun bindContentView() {
        bodyTextView?.let { bodyTextView ->
            layoutDirection()
            bodyTextView.setOnClickListener { v: View? -> onItemClick() }
            MoonUtil.identifyFaceExpression(
                NimUIKit.getContext(),
                bodyTextView,
                displayText(),
                ImageSpan.ALIGN_BOTTOM
            )
            bodyTextView.movementMethod = LinkMovementMethod.getInstance()
            bodyTextView.setOnLongClickListener(longClickListener)
        }
    }

    private fun layoutDirection() {
        if (isReceivedMessage) {
            bodyTextView!!.setBackgroundResource(NimUIKitImpl.getOptions().messageLeftBackground)
            bodyTextView!!.setTextColor(Color.WHITE)
            bodyTextView!!.setPadding(
                ScreenUtil.dip2px(15f),
                ScreenUtil.dip2px(8f),
                ScreenUtil.dip2px(10f),
                ScreenUtil.dip2px(8f)
            )
        } else {
            bodyTextView!!.setBackgroundResource(NimUIKitImpl.getOptions().messageRightBackground)
            bodyTextView!!.setTextColor(Color.WHITE)
            bodyTextView!!.setPadding(
                ScreenUtil.dip2px(10f),
                ScreenUtil.dip2px(8f),
                ScreenUtil.dip2px(15f),
                ScreenUtil.dip2px(8f)
            )
        }
    }

    override fun leftBackground(): Int {
        return R.drawable.nim_message_left_white_bg
    }

    override fun rightBackground(): Int {
        return R.drawable.nim_message_right_blue_bg
    }

    protected open fun displayText(): String = if (message == null) "" else message!!.content

}