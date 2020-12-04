package com.netease.nim.demo.main.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.alibaba.fastjson.JSONException
import com.alibaba.fastjson.JSONObject
import com.netease.nim.demo.R
import com.netease.nim.demo.common.ui.viewpager.FadeInOutPageTransformer
import com.netease.nim.demo.common.ui.viewpager.PagerSlidingTabStrip
import com.netease.nim.demo.common.ui.viewpager.PagerSlidingTabStrip.OnCustomTabListener
import com.netease.nim.demo.config.preference.Preferences
import com.netease.nim.demo.contact.activity.AddFriendActivity
import com.netease.nim.demo.login.LoginActivity
import com.netease.nim.demo.login.LogoutHelper.logout
import com.netease.nim.demo.main.adapter.MainTabPagerAdapter
import com.netease.nim.demo.main.helper.CustomNotificationCache
import com.netease.nim.demo.main.helper.SystemMessageUnreadManager
import com.netease.nim.demo.main.model.MainTab
import com.netease.nim.demo.main.reminder.ReminderItem
import com.netease.nim.demo.main.reminder.ReminderManager
import com.netease.nim.demo.main.reminder.ReminderManager.UnreadNumChangedCallback
import com.netease.nim.demo.session.SessionHelper.startP2PSession
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.NimIntent
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.msg.MsgServiceObserve
import com.netease.nimlib.sdk.msg.SystemMessageObserver
import com.netease.nimlib.sdk.msg.SystemMessageService
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.CustomNotification
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.netease.nimlib.sdk.msg.model.RecentContact
import com.zxn.netease.nimsdk.api.model.main.LoginSyncDataStatusObserver
import com.zxn.netease.nimsdk.business.contact.selector.activity.ContactSelectActivity
import com.zxn.netease.nimsdk.common.ToastHelper.showToast
import com.zxn.netease.nimsdk.common.activity.UI
import com.zxn.netease.nimsdk.common.ui.dialog.DialogMaker
import com.zxn.netease.nimsdk.common.ui.drop.DropManager
import com.zxn.netease.nimsdk.common.util.log.LogUtil

/**
 * 主界面
 */
class MainActivity : UI(), OnPageChangeListener, UnreadNumChangedCallback {
    private var tabs: PagerSlidingTabStrip? = null
    private var pager: ViewPager? = null
    private var scrollState = 0
    private var adapter: MainTabPagerAdapter? = null
    private var isFirstIn = false
    private val sysMsgUnreadCountChangedObserver = Observer { unreadCount: Int? ->
        SystemMessageUnreadManager.getInstance().sysMsgUnreadCount = unreadCount!!
        ReminderManager.getInstance().updateContactUnreadNum(unreadCount) //todo:
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        setToolBar(R.id.toolbar, R.string.app_name)
        setTitle(R.string.app_name)
        isFirstIn = true
        //不保留后台活动，从厂商推送进聊天页面，会无法退出聊天页面
        if (savedInstanceState == null && parseIntent()) {
            return
        }
        init()
    }

    private fun init() {
        observerSyncDataComplete()
        findViews()
        setupPager()
        setupTabs()
        registerMsgUnreadInfoObserver(true)
        registerSystemMessageObservers(true)
        registerCustomMessageObservers(true)
        requestSystemMessageUnreadCount()
        initUnreadCover()
    }

    private fun parseIntent(): Boolean {
        val intent = intent
        if (intent.hasExtra(EXTRA_APP_QUIT)) {
            intent.removeExtra(EXTRA_APP_QUIT)
            onLogout()
            return true
        }
        if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
            val message = intent.getSerializableExtra(
                NimIntent.EXTRA_NOTIFY_CONTENT
            ) as IMMessage?
            intent.removeExtra(NimIntent.EXTRA_NOTIFY_CONTENT)
            when (message!!.sessionType) {
                SessionTypeEnum.P2P -> startP2PSession(this, message.sessionId)
                SessionTypeEnum.Team -> {
                }
            }
            return true
        }
        return false
    }

    private fun observerSyncDataComplete() {
        val syncCompleted = LoginSyncDataStatusObserver.getInstance()
            .observeSyncDataCompletedEvent(
                Observer { v: Void? ->
                    DialogMaker
                        .dismissProgressDialog()
                })
        //如果数据没有同步完成，弹个进度Dialog
        if (!syncCompleted) {
            DialogMaker.showProgressDialog(this@MainActivity, getString(R.string.prepare_data))
                .setCanceledOnTouchOutside(false)
        }
    }

    private fun findViews() {
        tabs = findView(R.id.tabs)
        pager = findView(R.id.main_tab_pager)
    }

    private fun setupPager() {
        adapter = MainTabPagerAdapter(supportFragmentManager, this, pager)
        pager!!.offscreenPageLimit = adapter!!.cacheCount
        pager!!.setPageTransformer(true, FadeInOutPageTransformer())
        pager!!.adapter = adapter
        pager!!.setOnPageChangeListener(this)
    }

    private fun setupTabs() {
        tabs!!.setOnCustomTabListener(object : OnCustomTabListener() {
            override fun getTabLayoutResId(position: Int): Int {
                return R.layout.tab_layout_main
            }

            override fun screenAdaptation(): Boolean {
                return true
            }
        })
        tabs!!.setViewPager(pager)
        tabs!!.setOnTabClickListener(adapter)
        tabs!!.setOnTabDoubleTapListener(adapter)
    }

    /**
     * 注册未读消息数量观察者
     */
    private fun registerMsgUnreadInfoObserver(register: Boolean) {
        if (register) {
            ReminderManager.getInstance().registerUnreadNumChangedCallback(this)
        } else {
            ReminderManager.getInstance().unregisterUnreadNumChangedCallback(this)
        }
    }

    /**
     * 注册/注销系统消息未读数变化
     */
    private fun registerSystemMessageObservers(register: Boolean) {
        NIMClient.getService(SystemMessageObserver::class.java).observeUnreadCountChange(
            sysMsgUnreadCountChangedObserver, register
        )
    }

    // sample
    var customNotificationObserver = Observer { notification: CustomNotification ->
        // 处理自定义通知消息
        LogUtil.i(
            "demo", "receive custom notification: " + notification.content + " from :" +
                    notification.sessionId + "/" + notification.sessionType +
                    "unread=" + if (notification.config == null) "" else notification.config.enableUnreadCount.toString() + " " + "push=" +
                    notification.config.enablePush + " nick=" +
                    notification.config.enablePushNick
        )
        try {
            val obj = JSONObject.parseObject(notification.content)
            if (obj != null && obj.getIntValue("id") == 2) {
                // 加入缓存中
                CustomNotificationCache.getInstance().addCustomNotification(notification)
                // Toast
                val content = obj.getString("content")
                val tip = String.format("自定义消息[%s]：%s", notification.fromAccount, content)
                showToast(this@MainActivity, tip)
            }
        } catch (e: JSONException) {
            LogUtil.e("demo", e.message)
        }
    } as Observer<CustomNotification>

    private fun registerCustomMessageObservers(register: Boolean) {
        NIMClient.getService(MsgServiceObserve::class.java).observeCustomNotification(
            customNotificationObserver, register
        )
    }

    /**
     * 查询系统消息未读数
     */
    private fun requestSystemMessageUnreadCount() {
        val unread = NIMClient.getService(SystemMessageService::class.java)
            .querySystemMessageUnreadCountBlock()
        SystemMessageUnreadManager.getInstance().sysMsgUnreadCount = unread
        ReminderManager.getInstance().updateContactUnreadNum(unread)
    }

    //初始化未读红点动画
    private fun initUnreadCover() {
        DropManager.getInstance()
            .init(this, findView(R.id.unread_cover)) { id: Any?, explosive: Boolean ->
                if (id == null || !explosive) {
                    return@init
                }
                if (id is RecentContact) {
                    val r = id
                    NIMClient.getService(MsgService::class.java).clearUnreadCount(
                        r.contactId,
                        r.sessionType
                    )
                    return@init
                }
                if (id is String) {
                    if (id.contentEquals("0")) {
                        NIMClient.getService(MsgService::class.java).clearAllUnreadCount()
                    } else if (id.contentEquals("1")) {
                        NIMClient.getService(SystemMessageService::class.java)
                            .resetSystemMessageUnreadCount()
                    }
                }
            }
    }

    private fun onLogout() {
        Preferences.saveUserToken("")
        // 清理缓存&注销监听
        logout()
        // 启动登录
        LoginActivity.start(this)
        finish()
    }

    private fun selectPage() {
        if (scrollState == ViewPager.SCROLL_STATE_IDLE) {
            adapter!!.onPageSelected(pager!!.currentItem)
        }
    }

    /**
     * 设置最近联系人的消息为已读
     *
     *
     * account, 聊天对象帐号，或者以下两个值：
     * [MsgService.MSG_CHATTING_ACCOUNT_ALL] 目前没有与任何人对话，但能看到消息提醒（比如在消息列表界面），不需要在状态栏做消息通知
     * [MsgService.MSG_CHATTING_ACCOUNT_NONE] 目前没有与任何人对话，需要状态栏消息通知
     */
    private fun enableMsgNotification(enable: Boolean) {
        /*boolean msg = (pager.getCurrentItem() != MainTab.RECENT_CONTACTS.tabIndex);
        if (enable | msg) {
            NIMClient.getService(MsgService.class).setChattingAccount(
                    MsgService.MSG_CHATTING_ACCOUNT_NONE, SessionTypeEnum.None);
        } else {
            NIMClient.getService(MsgService.class).setChattingAccount(
                    MsgService.MSG_CHATTING_ACCOUNT_ALL, SessionTypeEnum.None);
        }*/
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_activity_menu, menu)
        super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.about -> startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            R.id.add_buddy -> AddFriendActivity.start(this@MainActivity)
            R.id.search_btn -> GlobalSearchActivity.start(this@MainActivity)
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        parseIntent()
    }

    public override fun onResume() {
        super.onResume()
        // 第一次 ， 三方通知唤起进会话页面之类的，不会走初始化过程
        val temp = isFirstIn
        isFirstIn = false
        if (pager == null && temp) {
            return
        }
        //如果不是第一次进 ， eg: 其他页面back
        if (pager == null) {
            init()
        }
        enableMsgNotification(false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    public override fun onPause() {
        super.onPause()
        if (pager == null) {
            return
        }
        enableMsgNotification(true)
    }

    public override fun onDestroy() {
        registerMsgUnreadInfoObserver(false)
        registerSystemMessageObservers(false)
        registerCustomMessageObservers(false)
        DropManager.getInstance().destroy()
        super.onDestroy()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            return
        }
        if (requestCode == REQUEST_CODE_NORMAL) {
            val selected = data!!.getStringArrayListExtra(
                ContactSelectActivity.RESULT_DATA
            )
            showToast(this@MainActivity, "请选择至少一个联系人！")
        } else if (requestCode == REQUEST_CODE_ADVANCED) {
            val selected = data!!.getStringArrayListExtra(
                ContactSelectActivity.RESULT_DATA
            )
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        tabs!!.onPageScrolled(position, positionOffset, positionOffsetPixels)
        adapter!!.onPageScrolled(position)
    }

    override fun onPageSelected(position: Int) {
        tabs!!.onPageSelected(position)
        selectPage()
        enableMsgNotification(false)
    }

    override fun onPageScrollStateChanged(state: Int) {
        tabs!!.onPageScrollStateChanged(state)
        scrollState = state
        selectPage()
    }

    //未读消息数量观察者实现
    override fun onUnreadNumChanged(item: ReminderItem) {
        val tab = MainTab.fromReminderId(item.id)
        if (tab != null) {
            tabs!!.updateTab(tab.tabIndex, item)
        }
    }

    override fun displayHomeAsUpEnabled(): Boolean {
        return false
    }

    companion object {
        private const val EXTRA_APP_QUIT = "APP_QUIT"
        private const val REQUEST_CODE_NORMAL = 1
        private const val REQUEST_CODE_ADVANCED = 2
        @JvmOverloads
        fun start(context: Context, extras: Intent? = null) {
            val intent = Intent()
            intent.setClass(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            if (extras != null) {
                intent.putExtras(extras)
            }
            context.startActivity(intent)
        }

        // 注销
        @JvmStatic
        fun logout(context: Context, quit: Boolean) {
            val extra = Intent()
            extra.putExtra(EXTRA_APP_QUIT, quit)
            start(context, extra)
        }
    }
}