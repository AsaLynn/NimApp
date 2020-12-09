package com.netease.nim.demo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.netease.nim.demo.DemoCache.context
import com.netease.nim.demo.config.preference.UserPreferences
import com.netease.nim.demo.event.OnlineStateEventManager
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.NimStrings
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.model.BroadcastMessage
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum
import com.netease.nimlib.sdk.team.model.UpdateTeamAttachment
import com.zxn.netease.nimsdk.common.ToastHelper.showToast

/**
 *
 * 用于初始化时，注册全局的广播、云信观察者等等云信相关业务
 */
class NIMInitManager private constructor() {

    companion object {
        private const val TAG = "NIMInitManager"
        var instance = InstanceHolder.instance
    }


    private object InstanceHolder {
        var instance = NIMInitManager()
            //get() = InstanceHolder.field
    }

    fun init(register: Boolean) {
        // 注册通知消息过滤器
        registerIMMessageFilter()

        // 注册语言变化监听广播
        registerLocaleReceiver(register)

        // 注册全局云信sdk 观察者
        registerGlobalObservers(register)

        // 初始化在线状态事件
        OnlineStateEventManager.init()
    }

    private fun registerGlobalObservers(register: Boolean) {
        // 注册云信全员广播
        registerBroadcastMessages(register)
    }

    private fun registerLocaleReceiver(register: Boolean) {
        if (register) {
            updateLocale()
            val filter = IntentFilter(Intent.ACTION_LOCALE_CHANGED)
            context!!.registerReceiver(localeReceiver, filter)
        } else {
            context!!.unregisterReceiver(localeReceiver)
        }
    }

    private val localeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_LOCALE_CHANGED) {
                updateLocale()
            }
        }
    }

    private fun updateLocale() {
        val context = context
        val strings = NimStrings()
        strings.status_bar_multi_messages_incoming =
            context!!.getString(R.string.nim_status_bar_multi_messages_incoming)
        strings.status_bar_image_message = context.getString(R.string.nim_status_bar_image_message)
        strings.status_bar_audio_message = context.getString(R.string.nim_status_bar_audio_message)
        strings.status_bar_custom_message =
            context.getString(R.string.nim_status_bar_custom_message)
        strings.status_bar_file_message = context.getString(R.string.nim_status_bar_file_message)
        strings.status_bar_location_message =
            context.getString(R.string.nim_status_bar_location_message)
        strings.status_bar_notification_message =
            context.getString(R.string.nim_status_bar_notification_message)
        strings.status_bar_ticker_text = context.getString(R.string.nim_status_bar_ticker_text)
        strings.status_bar_unsupported_message =
            context.getString(R.string.nim_status_bar_unsupported_message)
        strings.status_bar_video_message = context.getString(R.string.nim_status_bar_video_message)
        strings.status_bar_hidden_message_content =
            context.getString(R.string.nim_status_bar_hidden_msg_content)
        NIMClient.updateStrings(strings)
    }

    /**
     * 通知消息过滤器（如果过滤则该消息不存储不上报）
     */
    private fun registerIMMessageFilter() {
        NIMClient.getService(MsgService::class.java).registerIMMessageFilter { message: IMMessage ->
            if (UserPreferences.getMsgIgnore() && message.attachment != null) {
                if (message.attachment is UpdateTeamAttachment) {
                    val attachment = message.attachment as UpdateTeamAttachment
                    for ((key) in attachment.updatedFields) {
                        if (key == TeamFieldEnum.ICON) {
                            return@registerIMMessageFilter true
                        }
                    }
                }
            }
            false
        }
    }

    /**
     * 注册云信全服广播接收器
     *
     * @param register
     */
    private fun registerBroadcastMessages(register: Boolean) {
        NIMClient.getService(MsgServiceObserve::class.java).observeBroadcastMessage(
            Observer { broadcastMessage: BroadcastMessage ->
                showToast(
                    context!!, "收到全员广播 ：" + broadcastMessage.content
                )
            } as Observer<BroadcastMessage>, register)
    }


}