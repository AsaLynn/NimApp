package com.netease.nim.demo.session.viewholder

import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.netease.nim.demo.DemoCache.getAccount
import com.netease.nim.demo.R
import com.netease.nim.demo.session.extension.RedPacketOpenedAttachment
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo
import com.zxn.netease.nimsdk.api.NimUIKit
import com.zxn.netease.nimsdk.business.session.viewholder.MsgViewHolderBase
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter

class MsgViewHolderOpenRedPacket(adapter: BaseMultiItemFetchLoadAdapter<*, *>?) :
    MsgViewHolderBase(adapter) {
    private var packetMessageText: TextView? = null
    private var attachment: RedPacketOpenedAttachment? = null
    private var linearLayout: LinearLayout? = null
    private val userInfo: NimUserInfo
    override fun getContentResId(): Int {
        return R.layout.red_packet_open_item
    }

    override fun inflateContentView() {
        linearLayout = findViewById(R.id.packet_ll)
        packetMessageText = findViewById(R.id.packet_message)
    }

    override fun bindContentView() {
        attachment = message.attachment as RedPacketOpenedAttachment
        if (attachment == null || !validAttachment(attachment!!) || !belongToMe(attachment!!)) {
            setLayoutParams(0, 0, linearLayout)
            return
        }
        if (userInfo.account == attachment!!.openAccount) {
            openedRp(userInfo.account == attachment!!.sendAccount)
        } else if (userInfo.account == attachment!!.sendAccount) {
            othersOpenedRp()
        }
    }

    override fun shouldDisplayReceipt(): Boolean {
        return false
    }

    private fun openedRp(myself: Boolean) {
        val content: String
        content = if (myself) {
            if (attachment!!.isRpGetDone) {
                // 最后一个红包
                "你领取了自己的红包，你的红包已被领完"
            } else {
                // 不是最后一个红包
                "你领取了自己的红包"
            }
        } else {
            // 拆别人的红包
            val targetName = attachment!!.getSendNickName(message.sessionType, message.sessionId)
            "你领取了" + targetName + "的红包"
        }
        setSpannableText(content, content.length - 2, content.length)
    }

    private fun othersOpenedRp() {
        val content: String
        if (attachment!!.isRpGetDone) {
            // 最后一个红包
            content = attachment!!.getOpenNickName(
                message.sessionType,
                message.sessionId
            ) + "领取了你的红包，你的红包已被领完"
            setSpannableText(content, content.length - 11, content.length - 9)
        } else {
            // 不是最后一个红包
            content =
                attachment!!.getOpenNickName(message.sessionType, message.sessionId) + "领取了你的红包"
            setSpannableText(content, content.length - 2, content.length)
        }
    }

    private fun validAttachment(attachment: RedPacketOpenedAttachment): Boolean {
        return !TextUtils.isEmpty(attachment.openAccount) && !TextUtils.isEmpty(attachment.sendAccount)
    }

    // 我发的红包或者是我打开的红包
    private fun belongToMe(attachment: RedPacketOpenedAttachment): Boolean {
        return attachment.belongTo(userInfo.account)
    }

    private fun setSpannableText(content: String, start: Int, end: Int) {
        val tSS = SpannableString(content)
        val clickableSpan = RpDetailClickableSpan()
        tSS.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        packetMessageText!!.movementMethod = LinkMovementMethod.getInstance()
        packetMessageText!!.text = tSS
    }

    private inner class RpDetailClickableSpan : ClickableSpan() {
        override fun onClick(v: View) {}
        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = context.resources.getColor(R.color.color_238fea)
            ds.isUnderlineText = false
        }
    }

    /**
     * ------------------------------显示样式-------------------------
     */
    override fun isMiddleItem(): Boolean {
        return true
    }

    override fun isShowBubble(): Boolean {
        return false
    }

    override fun isShowHeadImage(): Boolean {
        return false
    }

    override fun onItemLongClick(): Boolean {
        return true
    }

    override fun onItemClick() {}

    init {
        userInfo = NimUIKit.getUserInfoProvider().getUserInfo(getAccount()) as NimUserInfo
    }
}