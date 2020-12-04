package com.netease.nim.demo.main.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import com.netease.nim.demo.DemoCache.getAccount
import com.netease.nim.demo.DemoCache.notificationConfig
import com.netease.nim.demo.R
import com.netease.nim.demo.config.preference.UserPreferences
import com.netease.nim.demo.contact.activity.UserProfileSettingActivity
import com.netease.nim.demo.jsbridge.JsBridgeActivity
import com.netease.nim.demo.main.activity.MainActivity.Companion.logout
import com.netease.nim.demo.main.activity.SettingsActivity
import com.netease.nim.demo.main.adapter.SettingsAdapter
import com.netease.nim.demo.main.adapter.SettingsAdapter.CheckChangeListener
import com.netease.nim.demo.main.adapter.SettingsAdapter.SwitchChangeListener
import com.netease.nim.demo.main.model.SettingTemplate
import com.netease.nim.demo.main.model.SettingType
import com.netease.nimlib.sdk.*
import com.netease.nimlib.sdk.Observer
import com.netease.nimlib.sdk.auth.AuthService
import com.netease.nimlib.sdk.misc.DirCacheFileType
import com.netease.nimlib.sdk.misc.MiscService
import com.netease.nimlib.sdk.msg.MsgService
import com.netease.nimlib.sdk.settings.SettingsService
import com.netease.nimlib.sdk.settings.SettingsServiceObserver
import com.zxn.netease.nimsdk.api.NimUIKit
import com.zxn.netease.nimsdk.api.wrapper.NimToolBarOptions
import com.zxn.netease.nimsdk.common.ToastHelper.showToast
import com.zxn.netease.nimsdk.common.activity.ToolBarOptions
import com.zxn.netease.nimsdk.common.activity.UI
import kotlinx.android.synthetic.main.settings_activity.*
import java.util.*

class SettingsActivity : UI(), SwitchChangeListener, CheckChangeListener {
//    var listView: ListView? = null
    var adapter: SettingsAdapter? = null
    private val items: MutableList<SettingTemplate> = ArrayList()
    private var noDisturbTime: String? = null
    private var disturbItem: SettingTemplate? = null
    private var clearIndexItem: SettingTemplate? = null
    private var clearSDKDirCacheItem: SettingTemplate? = null
    private var notificationItem: SettingTemplate? = null
    private var pushShowNoDetailItem: SettingTemplate? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        val options: ToolBarOptions = NimToolBarOptions()
        options.titleId = R.string.settings
        setToolBar(R.id.toolbar, options)
        initData()
        initUI()
        registerObservers(true)
    }

    override fun onResume() {
        super.onResume()
        adapter!!.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        registerObservers(false)
    }

    private fun registerObservers(register: Boolean) {
        NIMClient.getService(SettingsServiceObserver::class.java).observeMultiportPushConfigNotify(
            pushConfigObserver, register
        )
    }

    var pushConfigObserver = Observer { aBoolean: Boolean ->
        showToast(
            this@SettingsActivity, "收到multiport push config：$aBoolean"
        )
    } as Observer<Boolean>

    private fun initData() {
        noDisturbTime = if (UserPreferences.getStatusConfig() == null ||
            !UserPreferences.getStatusConfig().downTimeToggle
        ) {
            getString(R.string.setting_close)
        } else {
            String.format(
                "%s到%s",
                UserPreferences.getStatusConfig().downTimeBegin,
                UserPreferences.getStatusConfig().downTimeEnd
            )
        }
        sDKDirCacheSize
    }

    private fun initUI() {
        initItems()

        val footer = LayoutInflater.from(this).inflate(R.layout.settings_logout_footer, null)
        listView.addFooterView(footer)
        initAdapter()
        listView.setOnItemClickListener(AdapterView.OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            val item = items[position]
            onListItemClick(item)
        })
        val logoutBtn = footer.findViewById<View>(R.id.settings_button_logout)
        logoutBtn.setOnClickListener { v: View? -> logout() }
    }

    private fun initAdapter() {
        adapter = SettingsAdapter(this, this, this, items)
        listView!!.adapter = adapter
    }

    private fun initItems() {
        items.clear()
        items.add(SettingTemplate(TAG_HEAD, SettingType.TYPE_HEAD))
        notificationItem = SettingTemplate(
            TAG_NOTICE, getString(R.string.msg_notice),
            SettingType.TYPE_TOGGLE,
            UserPreferences.getNotificationToggle()
        )
        items.add(notificationItem!!)
        items.add(SettingTemplate.addLine())
        pushShowNoDetailItem = SettingTemplate(
            TAG_PUSH_SHOW_NO_DETAIL,
            getString(R.string.push_no_detail),
            SettingType.TYPE_TOGGLE,
            isShowPushNoDetail
        )
        items.add(pushShowNoDetailItem!!)
        val foldStyle = notificationFoldStyle
        val selectedId: Int
        selectedId = when (foldStyle) {
            NotificationFoldStyle.EXPAND -> R.id.rb_expand
            NotificationFoldStyle.CONTACT -> R.id.rb_contact
            NotificationFoldStyle.ALL -> R.id.rb_fold
            else -> R.id.rb_fold
        }
        items.add(
            SettingTemplate(
                TAG_NOTIFICATION_FOLD_STYLE,
                getString(R.string.notification_fold_style),
                SettingType.TYPE_THREE_CHOOSE_ONE,
                null,
                selectedId
            )
        )
        items.add(
            SettingTemplate(
                TAG_RING, getString(R.string.ring), SettingType.TYPE_TOGGLE,
                UserPreferences.getRingToggle()
            )
        )
        items.add(
            SettingTemplate(
                TAG_VIBRATE, getString(R.string.vibrate),
                SettingType.TYPE_TOGGLE, UserPreferences.getVibrateToggle()
            )
        )
        items.add(
            SettingTemplate(
                TAG_LED, getString(R.string.led), SettingType.TYPE_TOGGLE,
                UserPreferences.getLedToggle()
            )
        )
        items.add(SettingTemplate.addLine())
        items.add(
            SettingTemplate(
                TAG_NOTICE_CONTENT, getString(R.string.notice_content),
                SettingType.TYPE_TOGGLE,
                UserPreferences.getNoticeContentToggle()
            )
        )
        items.add(SettingTemplate.addLine())
        disturbItem = SettingTemplate(
            TAG_NO_DISTURBE, getString(R.string.no_disturb),
            noDisturbTime
        )
        items.add(disturbItem!!)
        items.add(SettingTemplate.addLine())
        items.add(
            SettingTemplate(
                TAG_MULTIPORT_PUSH, getString(R.string.multiport_push),
                SettingType.TYPE_TOGGLE,
                !NIMClient.getService(SettingsService::class.java)
                    .isMultiportPushOpen
            )
        )
        items.add(SettingTemplate.makeSeperator())
        items.add(
            SettingTemplate(
                TAG_SPEAKER, getString(R.string.msg_speaker),
                SettingType.TYPE_TOGGLE, NimUIKit.isEarPhoneModeEnable()
            )
        )
        items.add(SettingTemplate.makeSeperator())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            items.add(SettingTemplate.addLine())
            items.add(SettingTemplate(TAG_NRTC_NET_DETECT, "音视频通话网络探测"))
            items.add(SettingTemplate.addLine())
            items.add(SettingTemplate(AVCHAT_QUERY, "音视频通话记录"))
            items.add(SettingTemplate.makeSeperator())
        }
        items.add(
            SettingTemplate(
                TAG_MSG_IGNORE, "过滤通知", SettingType.TYPE_TOGGLE,
                UserPreferences.getMsgIgnore()
            )
        )
        items.add(SettingTemplate.makeSeperator())
        items.add(SettingTemplate(TAG_CLEAR, getString(R.string.about_clear_msg_history)))
        items.add(SettingTemplate.addLine())
        clearIndexItem = SettingTemplate(
            TAG_CLEAR_INDEX, getString(R.string.clear_index),
            "$indexCacheSize M"
        )
        items.add(clearIndexItem!!)
        items.add(SettingTemplate.addLine())
        clearSDKDirCacheItem = SettingTemplate(
            TAG_CLEAR_SDK_CACHE,
            getString(R.string.clear_sdk_cache), 0.toString() + " M"
        )
        items.add(clearSDKDirCacheItem!!)
        items.add(SettingTemplate.makeSeperator())
        items.add(SettingTemplate(TAG_CUSTOM_NOTIFY, getString(R.string.custom_notification)))
        items.add(SettingTemplate.addLine())
        items.add(SettingTemplate(TAG_JS_BRIDGE, getString(R.string.js_bridge_demonstration)))
        items.add(SettingTemplate.makeSeperator())
        items.add(
            SettingTemplate(
                TAG_PRIVATE_CONFIG,
                getString(R.string.setting_private_config)
            )
        )
        items.add(SettingTemplate.makeSeperator())
        items.add(SettingTemplate(TAG_MSG_MIGRATION, getString(R.string.local_db_migration)))
        items.add(SettingTemplate.makeSeperator())
        items.add(SettingTemplate(TAG_ABOUT, getString(R.string.setting_about)))
        items.add(SettingTemplate.makeSeperator())
        items.add(
            SettingTemplate(
                TAG_DELETE_FRIEND_ALIAS,
                getString(R.string.delete_friend_is_delete_alias),
                SettingType.TYPE_TOGGLE,
                UserPreferences.isDeleteFriendAndDeleteAlias()
            )
        )
    }

    private val notificationFoldStyle: NotificationFoldStyle
        private get() {
            val config = UserPreferences.getStatusConfig()
            var foldStyle = config.notificationFoldStyle
            if (foldStyle == null) {
                foldStyle = NotificationFoldStyle.ALL
                config.notificationFoldStyle = NotificationFoldStyle.ALL
                UserPreferences.setStatusConfig(config)
            }
            return foldStyle
        }

    private fun onListItemClick(item: SettingTemplate?) {
        if (item == null) {
            return
        }
        when (item.id) {
            TAG_HEAD -> UserProfileSettingActivity.start(this, getAccount())
            TAG_NO_DISTURBE -> startNoDisturb()
            TAG_CUSTOM_NOTIFY -> CustomNotificationActivity.start(this@SettingsActivity)
            TAG_CLEAR -> {
                NIMClient.getService(MsgService::class.java).clearMsgDatabase(true)
                showToast(this@SettingsActivity, R.string.clear_msg_history_success)
            }
            TAG_CLEAR_INDEX -> clearIndex()
            TAG_CLEAR_SDK_CACHE -> clearSDKDirCache()
            TAG_NRTC_NET_DETECT -> netDetectForNrtc()
            TAG_JS_BRIDGE -> startActivity(
                Intent(
                    this@SettingsActivity,
                    JsBridgeActivity::class.java
                )
            )
            TAG_JRMFWAllET -> {
            }
            TAG_PRIVATE_CONFIG -> startActivity(
                Intent(
                    this,
                    PrivatizationConfigActivity::class.java
                )
            )
            TAG_MSG_MIGRATION -> startActivity(Intent(this, MsgMigrationActivity::class.java))
            else -> {
            }
        }
    }

    private val sDKDirCacheSize: Unit
        private get() {
            val types: MutableList<DirCacheFileType> = ArrayList()
            types.add(DirCacheFileType.AUDIO)
            types.add(DirCacheFileType.THUMB)
            types.add(DirCacheFileType.IMAGE)
            types.add(DirCacheFileType.VIDEO)
            types.add(DirCacheFileType.OTHER)
            NIMClient.getService(MiscService::class.java)
                .getSizeOfDirCache(types, 0, 0)
                .setCallback(object : RequestCallbackWrapper<Long>() {
                        override fun onResult(code: Int, result: Long, exception: Throwable?) {
                            clearSDKDirCacheItem!!.detail =
                                String.format("%.2f M", result / (1024.0f * 1024.0f))
                            adapter!!.notifyDataSetChanged()
                        }
                    })
        }

    private fun clearSDKDirCache() {
        val types: MutableList<DirCacheFileType> = ArrayList()
        types.add(DirCacheFileType.AUDIO)
        types.add(DirCacheFileType.THUMB)
        types.add(DirCacheFileType.IMAGE)
        types.add(DirCacheFileType.VIDEO)
        types.add(DirCacheFileType.OTHER)
        NIMClient.getService(MiscService::class.java).clearDirCache(types, 0, 0).setCallback(
            object : RequestCallbackWrapper<Void?>() {
                override fun onResult(code: Int, result: Void?, exception: Throwable) {
                    clearSDKDirCacheItem!!.detail = "0.00 M"
                    adapter!!.notifyDataSetChanged()
                }
            })
    }

    private fun netDetectForNrtc() {}
    private val isShowPushNoDetail: Boolean
        private get() {
            val localConfig = UserPreferences.getStatusConfig()
            return localConfig.hideContent
        }

    private fun updateShowPushNoDetail(showNoDetail: Boolean) {}

    /**
     * 注销
     */
    private fun logout() {
        logout(this@SettingsActivity, false)
        finish()
        NIMClient.getService(AuthService::class.java).logout()
    }

    override fun onCheckChange(item: SettingTemplate, checkedId: Int) {
        when (item.id) {
            TAG_NOTIFICATION_FOLD_STYLE -> updateNotificationFoldStyle(checkedId)
            else -> {
            }
        }
    }

    /**
     * 更新通知栏合并方式
     *
     * @param checkedId 选中项的ID
     */
    private fun updateNotificationFoldStyle(checkedId: Int) {
        val foldStyle: NotificationFoldStyle
        foldStyle = when (checkedId) {
            R.id.rb_contact -> NotificationFoldStyle.CONTACT
            R.id.rb_expand -> NotificationFoldStyle.EXPAND
            R.id.rb_fold -> NotificationFoldStyle.ALL
            else -> NotificationFoldStyle.ALL
        }
        val config = UserPreferences.getStatusConfig()
        config.notificationFoldStyle = foldStyle
        UserPreferences.setStatusConfig(config)
        NIMClient.updateStatusBarNotificationConfig(config)
    }

    override fun onSwitchChange(item: SettingTemplate, checkState: Boolean) {
        when (item.id) {
            TAG_NOTICE -> setMessageNotify(checkState)
            TAG_SPEAKER -> NimUIKit.setEarPhoneModeEnable(checkState)
            TAG_MSG_IGNORE -> UserPreferences.setMsgIgnore(checkState)
            TAG_RING -> {
                UserPreferences.setRingToggle(checkState)
                val config = UserPreferences.getStatusConfig()
                config.ring = checkState
                UserPreferences.setStatusConfig(config)
                NIMClient.updateStatusBarNotificationConfig(config)
            }
            TAG_VIBRATE -> {
                UserPreferences.setVibrateToggle(checkState)
                val config = UserPreferences.getStatusConfig()
                config.vibrate = checkState
                UserPreferences.setStatusConfig(config)
                NIMClient.updateStatusBarNotificationConfig(config)
            }
            TAG_LED -> {
                UserPreferences.setLedToggle(checkState)
                val config = UserPreferences.getStatusConfig()
                val demoConfig = notificationConfig
                if (checkState && demoConfig != null) {
                    config.ledARGB = demoConfig.ledARGB
                    config.ledOnMs = demoConfig.ledOnMs
                    config.ledOffMs = demoConfig.ledOffMs
                } else {
                    config.ledARGB = -1
                    config.ledOnMs = -1
                    config.ledOffMs = -1
                }
                UserPreferences.setStatusConfig(config)
                NIMClient.updateStatusBarNotificationConfig(config)
            }
            TAG_NOTICE_CONTENT -> {
                UserPreferences.setNoticeContentToggle(checkState)
                val config2 = UserPreferences.getStatusConfig()
                config2.titleOnlyShowAppName = checkState
                UserPreferences.setStatusConfig(config2)
                NIMClient.updateStatusBarNotificationConfig(config2)
            }
            TAG_MULTIPORT_PUSH -> updateMultiportPushConfig(!checkState)
            TAG_PUSH_SHOW_NO_DETAIL -> updateShowPushNoDetail(checkState)
            TAG_DELETE_FRIEND_ALIAS -> updateDeleteFriendAndAlias(checkState)
            else -> {
            }
        }
        item.setChecked(checkState)
    }

    private fun updateDeleteFriendAndAlias(checkState: Boolean) {
        UserPreferences.setDeleteFriendAndDeleteAlias(checkState)
    }

    private fun setMessageNotify(checkState: Boolean) {}
    private fun setToggleNotification(checkState: Boolean) {
        try {
            setNotificationToggle(checkState)
            NIMClient.toggleNotification(checkState)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setNotificationToggle(on: Boolean) {
        UserPreferences.setNotificationToggle(on)
    }

    private fun startNoDisturb() {}
    private val indexCacheSize: String
        private get() = ""

    private fun clearIndex() {
        clearIndexItem!!.detail = "0.00 M"
        adapter!!.notifyDataSetChanged()
    }

    private fun updateMultiportPushConfig(checkState: Boolean) {
        NIMClient.getService(SettingsService::class.java).updateMultiportPushConfig(checkState)
            .setCallback(object : RequestCallback<Void?> {
                override fun onSuccess(param: Void?) {
                    showToast(this@SettingsActivity, "设置成功")
                }

                override fun onFailed(code: Int) {
                    showToast(this@SettingsActivity, "设置失败,code:$code")
                    adapter!!.notifyDataSetChanged()
                }

                override fun onException(exception: Throwable) {}
            })
    }

    /**
     * 设置免打扰时间
     *
     * @param data
     */
    private fun setNoDisturbTime(data: Intent) {}

    companion object {
        private const val TAG_HEAD = 1
        private const val TAG_NOTICE = 2
        private const val TAG_NO_DISTURBE = 3
        private const val TAG_CLEAR = 4
        private const val TAG_CUSTOM_NOTIFY = 5
        private const val TAG_ABOUT = 6
        private const val TAG_SPEAKER = 7
        private const val TAG_NRTC_SETTINGS = 8
        private const val TAG_NRTC_NET_DETECT = 9
        private const val TAG_MSG_IGNORE = 10
        private const val TAG_RING = 11
        private const val TAG_LED = 12
        private const val TAG_NOTICE_CONTENT = 13 // 通知栏提醒配置
        private const val TAG_CLEAR_INDEX = 18 // 清空全文检索缓存
        private const val TAG_MULTIPORT_PUSH = 19 // 桌面端登录，是否推送
        private const val TAG_JS_BRIDGE = 20 // js bridge
        private const val TAG_NOTIFICATION_FOLD_STYLE = 21 // 通知栏展开方式: 完全展开、全部折叠、按会话折叠
        private const val TAG_JRMFWAllET = 22 // 我的钱包
        private const val TAG_CLEAR_SDK_CACHE = 23 // 清除 sdk 文件缓存
        private const val TAG_PUSH_SHOW_NO_DETAIL = 24 // 推送消息不展示详情
        private const val TAG_VIBRATE = 25 // 推送消息不展示详情
        private const val TAG_PRIVATE_CONFIG = 26 // 私有化开关
        private const val TAG_MSG_MIGRATION = 27 // 本地消息迁移
        private const val TAG_DELETE_FRIEND_ALIAS = 28 // 本地消息迁移
        private const val AVCHAT_QUERY = 29 // 音视频通话记录查询
    }
}