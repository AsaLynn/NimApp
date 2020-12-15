package com.netease.nim.demo.main.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.netease.nim.demo.R
import com.netease.nim.demo.config.preference.Preferences
import com.netease.nim.demo.login.LoginActivity.Companion.start
import com.netease.nim.demo.login.LogoutHelper.logout
import com.netease.nim.demo.main.activity.MultiportActivity
import com.netease.nim.demo.main.fragment.SessionListFragment
import com.netease.nim.demo.main.model.MainTab
import com.netease.nim.demo.main.reminder.ReminderManager
import com.netease.nim.demo.msg.MsgActivity
import com.netease.nim.demo.session.extension.*
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.StatusCode
import com.netease.nimlib.sdk.auth.AuthServiceObserver
import com.netease.nimlib.sdk.auth.ClientType
import com.netease.nimlib.sdk.auth.OnlineClient
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.zxn.netease.nimsdk.business.recent.RecentContactsCallback
import com.zxn.netease.nimsdk.business.recent.RecentContactsFragment
import com.zxn.netease.nimsdk.common.ToastHelper.showToast
import com.zxn.netease.nimsdk.common.activity.UI
import com.zxn.netease.nimsdk.common.util.log.LogUtil
import com.zxn.netease.nimsdk.common.util.log.sdk.wrapper.NimLog
import java.util.*

class SessionListFragment : MainTabFragment() {
    private var notifyBar: View? = null
    private var notifyBarText: TextView? = null

    // 同时在线的其他端的信息
    private var onlineClients: List<OnlineClient>? = null
    private var multiportBar: View? = null
    private var fragment: RecentContactsFragment? = null
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        onCurrent()
    }

    override fun onDestroy() {
        registerObservers(false)
        super.onDestroy()
    }

    override fun onInit() {
        findViews()
        registerObservers(true)
        addRecentContactsFragment()
    }

    private fun registerObservers(register: Boolean) {
        NIMClient.getService(AuthServiceObserver::class.java)
            .observeOtherClients(clientsObserver, register)
        NIMClient.getService(AuthServiceObserver::class.java)
            .observeOnlineStatus(userStatusObserver, register)
    }

    private fun findViews() {
        notifyBar = view!!.findViewById(R.id.status_notify_bar)
        notifyBarText = view!!.findViewById(R.id.status_desc_label)
        notifyBar?.visibility = View.GONE
        multiportBar = view!!.findViewById(R.id.multiport_notify_bar)
        multiportBar?.visibility = View.GONE
        multiportBar?.setOnClickListener {
            MultiportActivity.startActivity(
                activity, onlineClients
            )
        }
    }

    /**
     * 用户状态变化
     */
    var userStatusObserver = Observer<StatusCode> { code ->
        if (code.wontAutoLogin()) {
            kickOut(code)
            NimLog.i(TAG, "kick out desc: " + code.desc)
        } else {
            if (code == StatusCode.NET_BROKEN) {
                notifyBar!!.visibility = View.VISIBLE
                notifyBarText!!.setText(R.string.net_broken)
            } else if (code == StatusCode.UNLOGIN) {
                notifyBar!!.visibility = View.VISIBLE
                notifyBarText!!.setText(R.string.nim_status_unlogin)
            } else if (code == StatusCode.CONNECTING) {
                notifyBar!!.visibility = View.VISIBLE
                notifyBarText!!.setText(R.string.nim_status_connecting)
            } else if (code == StatusCode.LOGINING) {
                notifyBar!!.visibility = View.VISIBLE
                notifyBarText!!.setText(R.string.nim_status_logining)
            } else {
                notifyBar!!.visibility = View.GONE
            }
        }
    }
    var clientsObserver: Observer<List<OnlineClient>> = Observer { onlineClients ->
        this@SessionListFragment.onlineClients = onlineClients
        if (onlineClients == null || onlineClients.size == 0) {
            multiportBar!!.visibility = View.GONE
        } else {
            multiportBar!!.visibility = View.VISIBLE
            val status = multiportBar!!.findViewById<TextView>(R.id.multiport_desc_label)
            val client = onlineClients[0]
            for (temp in onlineClients) {
                Log.d(TAG, "type : " + temp.clientType + " , customTag : " + temp.customTag)
            }
            when (client.clientType) {
                ClientType.Windows, ClientType.MAC -> status.text = getString(
                    R.string.multiport_logging
                ) + getString(R.string.computer_version)
                ClientType.Web -> status.text = getString(R.string.multiport_logging) + getString(
                    R.string.web_version
                )
                ClientType.iOS, ClientType.Android -> status.text =
                    getString(R.string.multiport_logging) + getString(
                        R.string.mobile_version
                    )
                else -> multiportBar!!.visibility = View.GONE
            }
        }
    }

    private fun kickOut(code: StatusCode) {
        Preferences.saveUserToken("")
        if (code == StatusCode.PWD_ERROR) {
            LogUtil.e("Auth", "user password error")
            showToast(activity!!, R.string.login_failed)
        } else {
            LogUtil.i("Auth", "Kicked!")
        }
        if (code == StatusCode.DATA_UPGRADE) {
            onLogout(getString(R.string.kickout_encrypt_database))
        } else {
            onLogout("")
        }
    }

    // 注销
    private fun onLogout(desc: String) {
        // 清理缓存&注销监听&清除状态
        logout()
        start(activity!!, true, desc)
        activity!!.finish()
    }

    // 将最近联系人列表fragment动态集成进来。 开发者也可以使用在xml中配置的方式静态集成。
    private fun addRecentContactsFragment() {
        fragment = RecentContactsFragment()
        fragment!!.containerId = R.id.messages_fragment
        val activity = activity as UI?

        // 如果是activity从堆栈恢复，FM中已经存在恢复而来的fragment，此时会使用恢复来的，而new出来这个会被丢弃掉
        fragment = activity!!.addFragment(fragment!!) as RecentContactsFragment?
        fragment!!.setCallback(object : RecentContactsCallback {
            override fun onRecentContactsLoaded() {
                // 最近联系人列表加载完毕
            }

            override fun onUnreadCountChange(unreadCount: Int) {
                ReminderManager.getInstance().updateSessionUnreadNum(unreadCount)
            }

            override fun onItemClick(recent: RecentContact?) {
                // 回调函数，以供打开会话窗口时传入定制化参数，或者做其他动作
                when (recent!!.sessionType) {
                    SessionTypeEnum.P2P -> {
                        //startP2PSession(getActivity(), recent.contactId)
                        MsgActivity.jumpTo(activity, recent.contactId)
                    }
                    SessionTypeEnum.Team -> {
                    }
                    SessionTypeEnum.SUPER_TEAM -> showToast(getActivity()!!, "超大群开发者按需实现")
                    SessionTypeEnum.Ysf -> {
                    }
                    else -> {
                    }
                }
            }

            override fun getDigestOfAttachment(
                recentContact: RecentContact?,
                attachment: MsgAttachment?
            ): String? {
                // 设置自定义消息的摘要消息，展示在最近联系人列表的消息缩略栏上
                // 当然，你也可以自定义一些内建消息的缩略语，例如图片，语音，音视频会话等，自定义的缩略语会被优先使用。
                when (attachment) {
                    is GuessAttachment -> {
                        return attachment.value.desc
                    }
                    is StickerAttachment -> {
                        return "[贴图]"
                    }
                    is SnapChatAttachment -> {
                        return "[阅后即焚]"
                    }
                    is RedPacketAttachment -> {
                        return "[红包]"
                    }
                    is RedPacketOpenedAttachment -> {
                        return attachment.getDesc(
                            recentContact!!.sessionType,
                            recentContact.contactId
                        )
                    }
                    is MultiRetweetAttachment -> {
                        return "[聊天记录]"
                    }
                    else -> return null
                }
            }

            override fun getDigestOfTipMsg(recent: RecentContact?): String? {
                val msgId = recent!!.recentMessageId
                val uuids: MutableList<String> = ArrayList(1)
                uuids.add(msgId)
                val msgs = NIMClient.getService(
                    MsgService::class.java
                ).queryMessageListByUuidBlock(uuids)
                if (msgs != null && !msgs.isEmpty()) {
                    val msg = msgs[0]
                    val content = msg.remoteExtension
                    if (content != null && !content.isEmpty()) {
                        return content["content"] as String?
                    }
                }
                return "null"
            }
        })
    }

    companion object {
        private val TAG = SessionListFragment::class.java.simpleName
    }

    init {
        containerId = MainTab.RECENT_CONTACTS.fragmentId
    }
}