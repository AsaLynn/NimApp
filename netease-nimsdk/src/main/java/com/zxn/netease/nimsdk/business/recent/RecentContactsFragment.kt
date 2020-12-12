package com.zxn.netease.nimsdk.business.recent

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.netease.nimlib.sdk.*
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.netease.nimlib.sdk.msg.model.StickTopSessionInfo
import com.zxn.netease.nimsdk.R
import com.zxn.netease.nimsdk.api.NimUIKit
import com.zxn.netease.nimsdk.api.model.contact.ContactChangedObserver
import com.zxn.netease.nimsdk.api.model.main.OnlineStateChangeObserver
import com.zxn.netease.nimsdk.api.model.user.UserInfoObserver
import com.zxn.netease.nimsdk.business.recent.adapter.RecentContactAdapter
import com.zxn.netease.nimsdk.business.uinfo.UserInfoHelper
import com.zxn.netease.nimsdk.common.ToastHelper.showToast
import com.zxn.netease.nimsdk.common.badger.Badger
import com.zxn.netease.nimsdk.common.fragment.TFragment
import com.zxn.netease.nimsdk.common.ui.dialog.CustomAlertDialog
import com.zxn.netease.nimsdk.common.ui.drop.DropCover.IDropCompletedListener
import com.zxn.netease.nimsdk.common.ui.drop.DropManager
import com.zxn.netease.nimsdk.common.ui.drop.DropManager.IDropListener
import com.zxn.netease.nimsdk.common.ui.recyclerview.listener.SimpleClickListener
import com.zxn.netease.nimsdk.impl.NimUIKitImpl
import java.util.*

/**
 * 最近联系人列表(会话列表)
 */
class RecentContactsFragment : TFragment() {
    // view
    private var recyclerView: RecyclerView? = null
    private var emptyBg: View? = null

    // data
    private var items: MutableList<RecentContact>? = null
    private var cached // 暂缓刷上列表的数据（未读数红点拖拽动画运行时用）
            : MutableMap<String, RecentContact>? = null
    private var adapter: RecentContactAdapter? = null
    private var msgLoaded = false
    private var callback: RecentContactsCallback? = null
    private var userInfoObserver: UserInfoObserver? = null
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        findViews()
        initMessageList()
        requestMessages(true)
        registerObservers(true)
        registerDropCompletedListener(true)
        registerOnlineStateChangeListener(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.nim_recent_contacts, container, false)
    }

    private fun notifyDataSetChanged() {
        adapter!!.notifyDataSetChanged()
        val empty = items!!.isEmpty() && msgLoaded
        emptyBg!!.visibility = if (empty) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        registerObservers(false)
        registerDropCompletedListener(false)
        registerOnlineStateChangeListener(false)
        DropManager.getInstance().setDropListener(null)
        super.onDestroy()
    }

    /**
     * 查找页面控件
     */
    private fun findViews() {
        recyclerView = findView<RecyclerView>(R.id.recycler_view)
        emptyBg = findView<View>(R.id.emptyBg)
    }

    /**
     * 初始化消息列表
     */
    private fun initMessageList() {
        items = ArrayList()
        cached = HashMap(3)
        // adapter
        adapter = RecentContactAdapter(recyclerView, items)
        initCallBack()
        adapter!!.callback = callback
        // recyclerView
        recyclerView!!.adapter = adapter
        recyclerView!!.layoutManager = LinearLayoutManager(activity)
        recyclerView!!.addOnItemTouchListener(touchListener)
        // drop listener
        DropManager.getInstance().setDropListener(object : IDropListener {
            override fun onDropBegin() {
                touchListener.setShouldDetectGesture(false)
            }

            override fun onDropEnd() {
                touchListener.setShouldDetectGesture(true)
            }
        })
    }

    private fun initCallBack() {
        if (callback != null) {
            return
        }
        callback = object : RecentContactsCallback {
            override fun onRecentContactsLoaded() {}
            override fun onUnreadCountChange(unreadCount: Int) {}
            override fun onItemClick(recent: RecentContact?) {
                if (recent!!.sessionType == SessionTypeEnum.P2P) {
                    NimUIKit.startP2PSession(activity, recent.contactId)
                }
            }

            override fun getDigestOfAttachment(
                recentContact: RecentContact?,
                attachment: MsgAttachment?
            ): String? {
                return null
            }

            override fun getDigestOfTipMsg(recent: RecentContact?): String? {
                return null
            }
        }
    }

    private val touchListener: SimpleClickListener<RecentContactAdapter?> =
        object : SimpleClickListener<RecentContactAdapter?>() {
            override fun onItemClick(adapter: RecentContactAdapter?, view: View, position: Int) {
                if (callback != null) {
                    adapter?.let {
                        val recent = adapter.getItem(position)
                        callback!!.onItemClick(recent)
                    }
                }
            }

            override fun onItemLongClick(
                adapter: RecentContactAdapter?,
                view: View,
                position: Int
            ) {
                adapter?.let {
                    showLongClickMenu(adapter.getItem(position), position)
                }
            }

            override fun onItemChildClick(
                adapter: RecentContactAdapter?,
                view: View,
                position: Int
            ) {
            }

            override fun onItemChildLongClick(
                adapter: RecentContactAdapter?,
                view: View,
                position: Int
            ) {
            }
        }

    var onlineStateChangeObserver: OnlineStateChangeObserver =
        object : OnlineStateChangeObserver {
            override fun onlineStateChange(account: Set<String?>?) {
                notifyDataSetChanged()
            }
        }

    private fun registerOnlineStateChangeListener(register: Boolean) {
        if (!NimUIKitImpl.enableOnlineState()) {
            return
        }
        NimUIKitImpl.getOnlineStateChangeObservable().registerOnlineStateChangeListeners(
            onlineStateChangeObserver,
            register
        )
    }

    private fun showLongClickMenu(recent: RecentContact?, position: Int) {
        val msgService = NIMClient.getService(MsgService::class.java)
        val sessionId = recent?.contactId
        val sessionType = recent?.sessionType
        val alertDialog = CustomAlertDialog(activity)
        alertDialog.setTitle(
            UserInfoHelper.getUserTitleName(
                recent!!.contactId,
                recent.sessionType
            )
        )
        var title = getString(R.string.main_msg_list_delete_chatting)
        alertDialog.addItem(title) {

            // 删除会话，删除后，消息历史被一起删除
            msgService.deleteRecentContact(recent)
            msgService.clearChattingHistory(sessionId, sessionType)
            adapter!!.remove(position)
            postRunnable { refreshMessages(true) }
        }
        title = if (msgService.isStickTopSession(sessionId, sessionType)) getString(
            R.string.main_msg_list_clear_sticky_on_top
        ) else getString(R.string.main_msg_list_sticky_on_top)
        alertDialog.addItem(title) {
            if (msgService.isStickTopSession(sessionId, sessionType)) {
                msgService.removeStickTopSession(sessionId, sessionType, "")
                    .setCallback(object : RequestCallbackWrapper<Void?>() {
                        override fun onResult(code: Int, result: Void?, exception: Throwable) {
                            if (ResponseCode.RES_SUCCESS.toInt() == code) {
                                refreshMessages(false)
                            }
                        }
                    })
            } else {
                msgService.addStickTopSession(sessionId, sessionType, "")
                    .setCallback(object : RequestCallbackWrapper<StickTopSessionInfo?>() {
                        override fun onResult(
                            code: Int,
                            result: StickTopSessionInfo?,
                            exception: Throwable
                        ) {
                            if (ResponseCode.RES_SUCCESS.toInt() == code) {
                                refreshMessages(false)
                            }
                        }
                    })
            }
        }
        val itemText = getString(R.string.delete_chat_only_server)
        alertDialog.addItem(itemText) {
            NIMClient.getService(
                MsgService::class.java
            )
                .deleteRoamingRecentContact(
                    recent.contactId,
                    recent.sessionType
                )
                .setCallback(object : RequestCallback<Void?> {
                    override fun onSuccess(param: Void?) {
                        showToast(activity!!, "delete success")
                    }

                    override fun onFailed(code: Int) {
                        showToast(
                            activity!!,
                            "delete failed, code:$code"
                        )
                    }

                    override fun onException(exception: Throwable) {}
                })
        }
        alertDialog.show()
    }

    private var loadedRecents: List<RecentContact>? = null
    private fun requestMessages(delay: Boolean) {
        if (msgLoaded) {
            return
        }
        handler.postDelayed({
            if (msgLoaded) {
                return@postDelayed
            }
            // 查询最近联系人列表数据
            NIMClient.getService(MsgService::class.java).queryRecentContacts().setCallback(
                object : RequestCallbackWrapper<List<RecentContact>?>() {
                    override fun onResult(
                        code: Int,
                        recents: List<RecentContact>?,
                        exception: Throwable?
                    ) {
                        if (code != ResponseCode.RES_SUCCESS.toInt() || recents == null) {
                            return
                        }
                        loadedRecents = recents
                        // 初次加载，更新离线的消息中是否有@我的消息
                        for (loadedRecent in loadedRecents!!) {
                            if (loadedRecent.sessionType == SessionTypeEnum.Team) {
                                //updateOfflineContactAited(loadedRecent)
                            }
                        }
                        // 此处如果是界面刚初始化，为了防止界面卡顿，可先在后台把需要显示的用户资料和群组资料在后台加载好，然后再刷新界面
                        //
                        msgLoaded = true
                        if (isAdded) {
                            onRecentContactsLoaded()
                        }
                    }
                })
        }, if (delay) 250 else 0.toLong())
    }

    private fun onRecentContactsLoaded() {
        items!!.clear()
        if (loadedRecents != null) {
            items!!.addAll(loadedRecents!!)
            loadedRecents = null
        }
        refreshMessages(true)
        if (callback != null) {
            callback!!.onRecentContactsLoaded()
        }
    }

    private fun refreshMessages(unreadChanged: Boolean) {
        sortRecentContacts(items)
        notifyDataSetChanged()
        if (unreadChanged) {
            // 方式一：累加每个最近联系人的未读（快）
            var unreadNum = 0
            for (r in items!!) {
                unreadNum += r.unreadCount
            }
            // 方式二：直接从SDK读取（相对慢）
            //int unreadNum = NIMClient.getService(MsgService.class).getTotalUnreadCount();
            if (callback != null) {
                callback!!.onUnreadCountChange(unreadNum)
            }
            Badger.updateBadgerCount(unreadNum)
        }
    }

    /**
     * **************************** 排序 ***********************************
     */
    private fun sortRecentContacts(list: List<RecentContact>?) {
        if (list!!.size == 0) {
            return
        }
        Collections.sort(list, comp)
    }

    /**
     * ********************** 收消息，处理状态变化 ************************
     */
    private fun registerObservers(register: Boolean) {
        val service = NIMClient.getService(
            MsgServiceObserve::class.java
        )
        service.observeReceiveMessage(messageReceiverObserver, register)
        service.observeRecentContact(messageObserver, register)
        service.observeMsgStatus(statusObserver, register)
        service.observeRecentContactDeleted(deleteObserver, register)
        registerStickTopObserver(register)
        NimUIKit.getContactChangedObservable().registerObserver(friendDataChangedObserver, register)
        if (register) {
            registerUserInfoObserver()
        } else {
            unregisterUserInfoObserver()
        }
    }

    private fun registerStickTopObserver(register: Boolean) {
        val msgObserver = NIMClient.getService(
            MsgServiceObserve::class.java
        )
        msgObserver.observeAddStickTopSession(stickTopSessionChangeObserve, register)
        msgObserver.observeRemoveStickTopSession(stickTopSessionChangeObserve, register)
        msgObserver.observeUpdateStickTopSession(stickTopSessionChangeObserve, register)
        msgObserver.observeSyncStickTopSession(syncStickTopSessionObserve, register)
    }

    private fun registerDropCompletedListener(register: Boolean) {
        if (register) {
            DropManager.getInstance().addDropCompletedListener(dropCompletedListener)
        } else {
            DropManager.getInstance().removeDropCompletedListener(dropCompletedListener)
        }
    }

    // 暂存消息，当RecentContact 监听回来时使用，结束后清掉
    private val cacheMessages: MutableMap<String, MutableSet<IMMessage>?> = HashMap()

    //监听在线消息中是否有@我
    private val messageReceiverObserver: Observer<List<IMMessage>> = Observer { imMessages ->
        if (imMessages != null) {
            for (imMessage in imMessages) {
                if (!TeamMemberAitHelper.isAitMessage(imMessage)) {
                    continue
                }
                var cacheMessageSet = cacheMessages[imMessage.sessionId]
                if (cacheMessageSet == null) {
                    cacheMessageSet = HashSet()
                    cacheMessages[imMessage.sessionId] = cacheMessageSet
                }
                cacheMessageSet.add(imMessage)
            }
        }
    }
    var messageObserver = Observer<List<RecentContact>> { recentContacts ->
        if (!DropManager.getInstance().isTouchable) {
            // 正在拖拽红点，缓存数据
            for (r in recentContacts) {
                cached!![r.contactId] = r
            }
            return@Observer
        }
        onRecentContactChanged(recentContacts)
    }

    private fun onRecentContactChanged(recentContacts: List<RecentContact>) {
        var index: Int
        for (r in recentContacts) {
            index = -1
            for (i in items!!.indices) {
                if (r.contactId == items!![i].contactId && r.sessionType == items!![i]
                        .sessionType
                ) {
                    index = i
                    break
                }
            }
            if (index >= 0) {
                items!!.removeAt(index)
            }
            items!!.add(r)
            if (r.sessionType == SessionTypeEnum.Team && cacheMessages[r.contactId] != null) {
                TeamMemberAitHelper.setRecentContactAited(r, cacheMessages[r.contactId])
            }
        }
        cacheMessages.clear()
        refreshMessages(true)
    }

    var dropCompletedListener = IDropCompletedListener { id, explosive ->
        if (cached != null && !cached!!.isEmpty()) {
            // 红点爆裂，已经要清除未读，不需要再刷cached
            if (explosive) {
                if (id is RecentContact) {
                    cached!!.remove(id.contactId)
                } else if (id is String && id.contentEquals("0")) {
                    cached!!.clear()
                }
            }
            // 刷cached
            if (!cached!!.isEmpty()) {
                val recentContacts: MutableList<RecentContact> = ArrayList(
                    cached!!.size
                )
                recentContacts.addAll(cached!!.values)
                cached!!.clear()
                onRecentContactChanged(recentContacts)
            }
        }
    }
    val statusObserver: Observer<IMMessage> = Observer { message ->
        if (message == null) {
            return@Observer
        }
        val sessionId = message.sessionId
        val sessionType = message.sessionType
        val index = getItemIndex(sessionId, sessionType)
        if (index >= 0 && index < items!!.size) {
            val recentContact = NIMClient.getService(MsgService::class.java)
                .queryRecentContact(sessionId, sessionType)
            items!![index] = recentContact
            refreshViewHolderByIndex(index)
        }
    }
    var deleteObserver: Observer<RecentContact> = Observer { recentContact ->
        if (recentContact != null) {
            for (item in items!!) {
                if (TextUtils.equals(item.contactId, recentContact.contactId) &&
                    item.sessionType == recentContact.sessionType
                ) {
                    items!!.remove(item)
                    refreshMessages(true)
                    break
                }
            }
        } else {
            items!!.clear()
            refreshMessages(true)
        }
    }
    private val syncStickTopSessionObserve =
        Observer { stickTopSessionInfos: List<StickTopSessionInfo?>? -> refreshMessages(false) }
    private val stickTopSessionChangeObserve =
        Observer { stickTopSessionInfo: StickTopSessionInfo? -> refreshMessages(false) }

    private fun getItemIndex(uuid: String): Int {
        for (i in items!!.indices) {
            val item = items!![i]
            if (TextUtils.equals(item.recentMessageId, uuid)) {
                return i
            }
        }
        return -1
    }

    private fun getItemIndex(sessionId: String, sessionType: SessionTypeEnum): Int {
        for (i in items!!.indices) {
            val item = items!![i]
            if (TextUtils.equals(item.contactId, sessionId) && item.sessionType == sessionType) {
                return i
            }
        }
        return -1
    }

    protected fun refreshViewHolderByIndex(index: Int) {
        activity!!.runOnUiThread { adapter!!.notifyItemChanged(index) }
    }

    fun setCallback(callback: RecentContactsCallback?) {
        this.callback = callback
    }

    private fun registerUserInfoObserver() {
        if (userInfoObserver == null) {
            userInfoObserver =
                object : UserInfoObserver {
                    override fun onUserInfoChanged(accounts: List<String>?) {
                        refreshMessages(false)
                    }
                }
        }
        NimUIKit.getUserInfoObservable().registerObserver(userInfoObserver, true)
    }

    private fun unregisterUserInfoObserver() {
        if (userInfoObserver != null) {
            NimUIKit.getUserInfoObservable().registerObserver(userInfoObserver, false)
        }
    }

    var friendDataChangedObserver: ContactChangedObserver = object : ContactChangedObserver {
        override fun onAddedOrUpdatedFriends(accounts: List<String?>?) {
            refreshMessages(false)
        }

        override fun onDeletedFriends(accounts: List<String?>?) {
            refreshMessages(false)
        }

        override fun onAddUserToBlackList(account: List<String?>?) {
            refreshMessages(false)
        }

        override fun onRemoveUserFromBlackList(account: List<String?>?) {
            refreshMessages(false)
        }
    }

    private fun updateOfflineContactAited(recentContact: RecentContact?) {
        if (recentContact == null || recentContact.sessionType != SessionTypeEnum.Team || recentContact.unreadCount <= 0) {
            return
        }
        // 锚点
        val uuid: MutableList<String> = ArrayList(1)
        uuid.add(recentContact.recentMessageId)
        val messages = NIMClient.getService(
            MsgService::class.java
        ).queryMessageListByUuidBlock(uuid)
        if (messages == null || messages.size < 1) {
            return
        }
        val anchor = messages[0]
        // 查未读消息
        NIMClient.getService(MsgService::class.java).queryMessageListEx(
            anchor, QueryDirectionEnum.QUERY_OLD,
            recentContact.unreadCount - 1, false
        )
            .setCallback(object : RequestCallbackWrapper<MutableList<IMMessage?>?>() {
                override fun onResult(
                    code: Int,
                    result: MutableList<IMMessage?>?,
                    exception: Throwable?
                ) {
                    if (code == ResponseCode.RES_SUCCESS.toInt() && result != null) {
                        //MutableList()
                        result?.add(0, anchor)
                        var messages: MutableSet<IMMessage?>? = null
                        // 过滤存在的@我的消息
                        for (msg in result) {
                            if (TeamMemberAitHelper.isAitMessage(msg)) {
                                if (messages == null) {
                                    messages = HashSet()
                                }
                                messages.add(msg)
                            }
                        }
                        // 更新并展示
                        if (messages != null) {
                            TeamMemberAitHelper.setRecentContactAited(recentContact, messages)
                            notifyDataSetChanged()
                        }
                    }
                }
            })
    }

    companion object {
        private val comp = label@ Comparator { recent1: RecentContact, recent2: RecentContact ->
            // 先比较置顶tag
            val isStickTop1 = NIMClient.getService(MsgService::class.java)
                .isStickTopSession(recent1.contactId, recent1.sessionType)
            val isStickTop2 = NIMClient.getService(MsgService::class.java)
                .isStickTopSession(recent2.contactId, recent2.sessionType)
            if (isStickTop1 xor isStickTop2) {
                if (isStickTop1) -1 else 1
            } else {
                val time = recent1.time - recent2.time
                if (time == 0L) 0 else if (time > 0) -1 else 1
            }
        }
    }
}