package com.nim.app

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import android.text.TextUtils
import android.webkit.WebView
import androidx.multidex.MultiDex
import com.heytap.msp.push.HeytapPushManager
import com.huawei.hms.support.common.ActivityMgr
import com.netease.nim.avchatkit.AVChatKit
import com.netease.nim.avchatkit.config.AVChatOptions
import com.netease.nim.avchatkit.model.ITeamDataProvider
import com.netease.nim.avchatkit.model.IUserInfoProvider
import com.netease.nim.rtskit.RTSKit
import com.netease.nim.rtskit.api.config.RTSOptions
import com.netease.nim.uikit.api.NimUIKit
import com.netease.nim.uikit.api.UIKitOptions
import com.netease.nim.uikit.business.contact.core.query.PinYin
import com.netease.nim.uikit.business.team.helper.TeamHelper
import com.netease.nim.uikit.business.uinfo.UserInfoHelper
import com.netease.nim.uikit.common.ToastHelper
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.SDKOptions
import com.netease.nimlib.sdk.auth.LoginInfo
import com.netease.nimlib.sdk.mixpush.NIMPushClient
import com.netease.nimlib.sdk.uinfo.model.UserInfo
import com.netease.nimlib.sdk.util.NIMUtil
import com.qiyukf.unicorn.ysfkit.unicorn.api.*
import com.qiyukf.unicorn.ysfkit.unicorn.api.privatization.UnicornAddress
import com.zxn.nim.*
import com.zxn.nim.chatroom.ChatRoomSessionHelper
import com.zxn.nim.common.util.LogHelper
import com.zxn.nim.common.util.crash.AppCrashHandler
import com.zxn.nim.config.preference.Preferences
import com.zxn.nim.config.preference.UserPreferences
import com.zxn.nim.contact.ContactHelper
import com.zxn.nim.event.DemoOnlineStateContentProvider
import com.zxn.nim.mixpush.DemoMixPushMessageHandler
import com.zxn.nim.mixpush.DemoPushContentProvider
import com.zxn.nim.rts.RTSHelper
import com.zxn.nim.session.NimDemoLocationProvider
import com.zxn.nim.session.SessionHelper
import com.zxn.nim.ysf.imageloader.GlideImageLoader
import com.zxn.nim.ysf.util.YsfHelper
import com.zxn.utils.UIUtils


/**
 * Copyright(c) ${}YEAR} ZhuLi co.,Ltd All Rights Reserved.
 *
 * @className:
 * @description: TODO 类描述
 * @version: v0.0.1
 * @author: zxn < a href=" ">zhangxiaoning@17biyi.com</ a>
 * @date:
 * @updateUser: 更新者：
 * @updateDate:
 * @updateRemark: 更新说明：
 * @version: 1.0
 * */
class YinYu : Application() {


    private val sdKAppId = 1400068060

    /**
     * 测试 (灰度);
     */
    private val sdKAppIdTest = 1400373666


    override fun onCreate() {
        super.onCreate()
        initNIMClient()
        UIUtils.init(this)
    }


    /**
     * 17610269785,xb123456
     * SDK初始化（启动后台服务，若已经存在用户登录信息， SDK 将进行自动登录）。
     * 不能对初始化语句添加进程判断逻辑。
     */
    private fun initNIMClient() {
        DemoCache.setContext(this)

        //NIMClientHelper.instance().init(this)
        val sdkOptions: SDKOptions = NimSDKOptionConfig.getSDKOptions(this)
        NIMClient.init(this, getLoginInfo(), sdkOptions);

        AppCrashHandler.getInstance(this)

        // 以下逻辑只在主进程初始化时执行
        if (NIMUtil.isMainProcess(this)) {
            ActivityMgr.INST.init(this)
            // 初始化OPPO PUSH服务，创建默认通道
            // 初始化OPPO PUSH服务，创建默认通道
            HeytapPushManager.init(this, true)
            // 注册自定义推送消息处理，这个是可选项
            // 注册自定义推送消息处理，这个是可选项
            NIMPushClient.registerMixPushMessageHandler(DemoMixPushMessageHandler())
            PinYin.init(this)
            PinYin.validate()

            // 初始化UIKit模块
            initUIKit()

            // 初始化消息提醒
            NIMClient.toggleNotification(UserPreferences.getNotificationToggle())

            // 云信sdk相关业务初始化
            NIMInitManager.getInstance().init(true)

            // 初始化音视频模块
            initAVChatKit()

            // 初始化rts模块
            initRTSKit()

            //初始化融合 SDK 中的七鱼业务关业务
            initMixSdk()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WebView.setDataDirectorySuffix(Process.myPid().toString() + "")
            }

        }
    }


    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        MultiDex.install(this)
    }

    private fun initUIKit() {
        // 初始化
        NimUIKit.init(this, buildUIKitOptions())

        // 设置地理位置提供者。如果需要发送地理位置消息，该参数必须提供。如果不需要，可以忽略。
        NimUIKit.setLocationProvider(NimDemoLocationProvider())

        // IM 会话窗口的定制初始化。
        SessionHelper.init()

        // 聊天室聊天窗口的定制初始化。
        ChatRoomSessionHelper.init()

        // 通讯录列表定制初始化
        ContactHelper.init()

        // 添加自定义推送文案以及选项，请开发者在各端（Android、IOS、PC、Web）消息发送时保持一致，以免出现通知不一致的情况
        NimUIKit.setCustomPushContentProvider(DemoPushContentProvider())
        NimUIKit.setOnlineStateContentProvider(DemoOnlineStateContentProvider())


    }

    private fun initMixSdk() {
        val imageLoader: UnicornImageLoader
        imageLoader = GlideImageLoader(this)
        //内部已经初始化了 Nim baseSDK
        Unicorn.init(this, YsfHelper.readAppKey(this), mixOptions(), imageLoader)
    }

    private fun mixOptions(): YSFOptions? {
        val options = YSFOptions()
        if (options.uiCustomization == null) {
            options.uiCustomization = UICustomization()
        }
        options.onMessageItemClickListener =
            OnMessageItemClickListener { context: Context?, url: String? ->
                ToastHelper.showToast(
                    context,
                    url
                )
            }
        options.onBotEventListener = object : OnBotEventListener() {
            override fun onUrlClick(context: Context, url: String): Boolean {
                ToastHelper.showToast(context, url)
                return true
            }
        }
        options.quickEntryListener = object : QuickEntryListener() {
            override fun onClick(context: Context, shopId: String, quickEntry: QuickEntry) {
                ToastHelper.showToast(context, shopId)
                if (quickEntry.id == 0L) {
                }
            }
        }
        options.isPullMessageFromServer = true
        options.isMixSDK = true
        if (!TextUtils.isEmpty(DemoPrivatizationConfig.getYsfDaUrlLabel(this)) && !TextUtils.isEmpty(
                DemoPrivatizationConfig.getYsfDefalutUrlLabel(this)
            )
        ) {
            val unicornAddress = UnicornAddress()
            unicornAddress.defaultUrl = DemoPrivatizationConfig.getYsfDefalutUrlLabel(this)
            unicornAddress.daUrl = DemoPrivatizationConfig.getYsfDaUrlLabel(this)
            options.unicornAddress = unicornAddress
        }
        return options
    }

    private fun buildUIKitOptions(): UIKitOptions? {
        val options = UIKitOptions()
        // 设置app图片/音频/日志等缓存目录
        options.appCacheDir = NimSDKOptionConfig.getAppCacheDir(this) + "/app"
        return options
    }

    private fun initAVChatKit() {
        val avChatOptions: AVChatOptions = object : AVChatOptions() {
            override fun logout(context: Context) {
                NimMainActivity.logout(context, true)
            }
        }
        avChatOptions.entranceActivity = WelcomeActivity::class.java
        avChatOptions.notificationIconRes = R.drawable.ic_stat_notify_msg
        com.netease.nim.avchatkit.ActivityMgr.INST.init(this)
        AVChatKit.init(avChatOptions)

        // 初始化日志系统
        LogHelper.init()
        // 设置用户相关资料提供者
        AVChatKit.setUserInfoProvider(object : IUserInfoProvider() {
            override fun getUserInfo(account: String): UserInfo {
                return NimUIKit.getUserInfoProvider().getUserInfo(account)
            }

            override fun getUserDisplayName(account: String): String {
                return UserInfoHelper.getUserDisplayName(account)
            }
        })
        // 设置群组数据提供者
        AVChatKit.setTeamDataProvider(object : ITeamDataProvider() {
            override fun getDisplayNameWithoutMe(teamId: String, account: String): String {
                return TeamHelper.getDisplayNameWithoutMe(teamId, account)
            }

            override fun getTeamMemberDisplayName(teamId: String, account: String): String {
                return TeamHelper.getTeamMemberDisplayName(teamId, account)
            }
        })
    }


    private fun initRTSKit() {
        val rtsOptions: RTSOptions = object : RTSOptions() {
            override fun logout(context: Context) {
                NimMainActivity.logout(context, true)
            }
        }
        RTSKit.init(rtsOptions)
        RTSHelper.init()
    }

    private fun getLoginInfo(): LoginInfo? {
        val account: String? = Preferences.getUserAccount()
        val token: String? = Preferences.getUserToken()
        return if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
            DemoCache.setAccount(account!!.toLowerCase())
            LoginInfo(account, token)
        } else {
            null
        }
    }

}