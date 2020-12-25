//package com.netease.nim.demo.session.viewholder
//
//import android.graphics.Color
//import android.widget.ImageView
//import android.widget.TextView
//import com.netease.nim.demo.R
////import com.netease.nimlib.sdk.avchat.constant.AVChatRecordState
////import com.netease.nimlib.sdk.avchat.constant.AVChatType
////import com.netease.nimlib.sdk.avchat.model.AVChatAttachment
//import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum
//import com.netease.nimlib.sdk.msg.model.IMMessage
//import com.zxn.netease.nimsdk.business.session.viewholder.MsgViewHolderBase
//import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter
//import com.zxn.netease.nimsdk.common.util.sys.TimeUtil
//
//class MsgViewHolderAVChat(adapter: BaseMultiItemFetchLoadAdapter<*, *>?) : MsgViewHolderBase(
//    adapter!!
//) {
//    private var typeImage: ImageView? = null
//    private var statusLabel: TextView? = null
//    override val contentResId: Int
//        get() = R.layout.nim_message_item_avchat
//
//    override fun inflateContentView() {
//        typeImage = findViewById<ImageView>(R.id.message_item_avchat_type_img)
//        statusLabel = findViewById<TextView>(R.id.message_item_avchat_state)
//    }
//
//    override fun bindContentView() {
//        message?.let { msg ->
//            msg.attachment?.let {
//                layoutByDirection(msg)
//                refreshContent()
//            }
//        }
//    }
//
//    private fun layoutByDirection(message: IMMessage) {
//        val attachment = message.attachment as AVChatAttachment
//        if (isReceivedMessage) {
//            if (attachment.type == AVChatType.AUDIO) {
//                typeImage?.setImageResource(R.drawable.avchat_left_type_audio)
//            }
//            statusLabel!!.setTextColor(context!!.resources.getColor(R.color.color_grey_999999))
//        } else {
//            if (attachment.type == AVChatType.AUDIO) {
//                typeImage?.setImageResource(R.drawable.avchat_right_type_audio)
//            }
//            statusLabel!!.setTextColor(Color.WHITE)
//        }
//    }
//
//    private fun refreshContent() {
//        val attachment = message!!.attachment as AVChatAttachment
//        var textString: String? = ""
//        when (attachment.state) {
//            AVChatRecordState.Success -> textString = TimeUtil.secToTime(attachment.duration)
//            AVChatRecordState.Missed -> textString = context!!.getString(R.string.avchat_no_pick_up)
//            AVChatRecordState.Rejected -> {
//                val strID: Int =
//                    if (message!!.direct == MsgDirectionEnum.In) R.string.avchat_has_reject else R.string.avchat_be_rejected
//                textString = context!!.getString(strID)
//            }
//            AVChatRecordState.Canceled -> textString = context!!.getString(R.string.avchat_cancel)
//            else -> {
//            }
//        }
//        statusLabel!!.text = textString
//    }
//}