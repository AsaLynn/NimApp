package com.netease.nim.demo.session.viewholder

import android.app.Activity
import android.text.TextUtils
import android.text.style.ImageSpan
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.netease.nim.demo.R
import com.netease.nim.demo.session.activity.WatchMultiRetweetActivity
import com.netease.nim.demo.session.extension.MultiRetweetAttachment
import com.zxn.netease.nimsdk.business.session.emoji.EmojiManager
import com.zxn.netease.nimsdk.business.session.emoji.MoonUtil
import com.zxn.netease.nimsdk.business.session.module.list.MessageListPanelEx
import com.zxn.netease.nimsdk.business.session.viewholder.MsgViewHolderBase
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter
import java.util.*

class MsgViewHolderMultiRetweet(adapter: BaseMultiItemFetchLoadAdapter<*, *>?) :
    MsgViewHolderBase(adapter) {
    private var mTitleTV: TextView? = null
    private var mFirstMsgTV: TextView? = null
    private var mSecondMsgTV: TextView? = null
    private var mFootNoteTV: TextView? = null
    override fun getContentResId(): Int {
        return R.layout.nim_message_item_multi_retweet
    }

    override fun inflateContentView() {
        mTitleTV = findViewById(R.id.nim_message_item_tv_title)
        mFirstMsgTV = findViewById(R.id.nim_message_item_tv_msg1)
        mSecondMsgTV = findViewById(R.id.nim_message_item_tv_msg2)
        mFootNoteTV = findViewById(R.id.nim_message_item_tv_foot_note)
    }

    override fun bindContentView() {
        var sessionName = ""
        var sender1 = ""
        var msg1 = ""
        var sender2 = ""
        var msg2 = ""
        val msgAttachment = message.attachment
        if (msgAttachment != null) {
            //通过json转接的方式进行类型转换
            val attachmentJsonStr = message.attachment.toJson(false)
            var attachmentJson = JSON.parseObject(attachmentJsonStr)
            if (attachmentJson.containsKey("data")) {
                attachmentJson = attachmentJson.getJSONObject("data")
            }
            val attachment = MultiRetweetAttachment()
            attachment.fromJson(attachmentJson)
            sessionName = attachment.sessionName?:""
            sender1 = attachment.sender1?:""
            msg1 = attachment.message1?:""
            sender2 = attachment.sender2?:""
            msg2 = attachment.message2?:""
        }
        if (isReceivedMessage) {
            mTitleTV!!.setTextColor(context.resources.getColor(R.color.color_black_b3000000))
            mFirstMsgTV!!.setTextColor(context.resources.getColor(R.color.color_grey_555555))
            mSecondMsgTV!!.setTextColor(context.resources.getColor(R.color.color_grey_555555))
            mFootNoteTV!!.setTextColor(context.resources.getColor(R.color.color_grey_555555))
        } else {
            mTitleTV!!.setTextColor(context.resources.getColor(R.color.GreyWhite))
            mFirstMsgTV!!.setTextColor(context.resources.getColor(R.color.color_gray_d9d9d9))
            mSecondMsgTV!!.setTextColor(context.resources.getColor(R.color.color_gray_d9d9d9))
            mFootNoteTV!!.setTextColor(context.resources.getColor(R.color.color_gray_d9d9d9))
        }
        mTitleTV!!.text = String.format(Locale.CHINA, "%s的聊天记录", sessionName)
        val tip1 = String.format(Locale.CHINA, "%s: %s", sender1, msg1)
        MoonUtil.identifyFaceExpression(
            context,
            mFirstMsgTV,
            cutStrWithEmoji(tip1, TIP_LEN_MAX),
            ImageSpan.ALIGN_BOTTOM
        )
        val tip2 = if (TextUtils.isEmpty(sender2)) "" else String.format(
            Locale.CHINA,
            "%s: %s",
            sender2,
            msg2
        )
        if (TextUtils.isEmpty(tip2)) {
            mSecondMsgTV!!.text = ""
        } else {
            MoonUtil.identifyFaceExpression(
                context,
                mSecondMsgTV,
                cutStrWithEmoji(tip2, TIP_LEN_MAX),
                ImageSpan.ALIGN_BOTTOM
            )
        }
    }

    /**
     * 在不截断emoji的情况下截取字符串的最多前lenLimit个，如果截断，则在最后添加...
     *
     * @param str      源字符串
     * @param lenLimit 截断后的字符串的最大长度
     * @return 截断后字符串
     */
    private fun cutStrWithEmoji(str: String, lenLimit: Int): String {
        var lenLimit = lenLimit
        lenLimit = Math.max(0, lenLimit)
        if (str.length <= lenLimit) {
            return str
        }
        val matcher = EmojiManager.getPattern().matcher(str)
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            if (end > lenLimit) {
                if (start < lenLimit) {
                    lenLimit = start
                }
                break
            }
        }
        return str.substring(0, lenLimit) + "..."
    }

    override fun onItemClick() {
        if (context is WatchMultiRetweetActivity) {
            WatchMultiRetweetActivity.start(context as Activity, message)
        } else {
            WatchMultiRetweetActivity.startForResult(
                MessageListPanelEx.REQUEST_CODE_WATCH_MULTI_RETWEET,
                context as Activity,
                message
            )
        }
    }

    companion object {
        private const val TIP_LEN_MAX = 30
    }
}