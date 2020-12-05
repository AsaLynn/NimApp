package com.netease.nim.demo

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import android.text.TextUtils
import android.webkit.WebView
import androidx.multidex.MultiDex
import com.heytap.msp.push.HeytapPushManager
import com.netease.nim.demo.DemoCache.setAccount
import com.netease.nim.demo.DemoCache.setContext
import com.netease.nim.demo.common.util.crash.AppCrashHandler
import com.netease.nim.demo.config.preference.Preferences
import com.netease.nim.demo.config.preference.UserPreferences
import com.netease.nim.demo.contact.ContactHelper
import com.netease.nim.demo.event.DemoOnlineStateContentProvider
import com.netease.nim.demo.mixpush.DemoPushContentProvider
import com.netease.nim.demo.session.SessionHelper
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.auth.LoginInfo
import com.netease.nimlib.sdk.util.NIMUtil
import com.zxn.netease.nimsdk.api.NimUIKit
import com.zxn.netease.nimsdk.api.UIKitOptions
import com.zxn.netease.nimsdk.business.contact.core.query.PinYin
import com.zxn.utils.UIUtils

class NimApplication : Application() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
        MultiDex.install(this)
    }

    /**
     * 注意：每个进程都会创建自己的Application 然后调用onCreate() 方法，
     * 如果用户有自己的逻辑需要写在Application#onCreate()（还有Application的其他方法）中，一定要注意判断进程，不能把业务逻辑写在core进程，
     * 理论上，core进程的Application#onCreate()（还有Application的其他方法）只能做与im sdk 相关的工作
     */
    override fun onCreate() {
        super.onCreate()
        UIUtils.init(this)
        setContext(this)

        // 4.6.0 开始，第三方推送配置入口改为 SDKOption#mixPushConfig，旧版配置方式依旧支持。
        val sdkOptions = NimSDKOptionConfig.getSDKOptions(this)
        //sdkOptions.appKey = "40b5f5ff9dc3e53d5568bfd5531ba085";
        sdkOptions.appKey = "45c6af3c98409b18a84451215d0bdd6e"
        NIMClient.init(this, loginInfo, sdkOptions)

        // crash handler
        AppCrashHandler.getInstance(this)

        // 以下逻辑只在主进程初始化时执行
        if (NIMUtil.isMainProcess(this)) {

            //ActivityMgr.INST.init(this);
            // 初始化OPPO PUSH服务，创建默认通道
            HeytapPushManager.init(this, true)

            // init pinyin
            PinYin.init(this)
            PinYin.validate()
            // 初始化UIKit模块
            initUIKit()
            // 初始化消息提醒
            NIMClient.toggleNotification(UserPreferences.getNotificationToggle())
            //关闭撤回消息提醒
//            NIMClient.toggleRevokeMessageNotification(false);
            // 云信sdk相关业务初始化
            NIMInitManager.getInstance().init(true)
        }
        //初始化融合 SDK 中的七鱼业务关业务
        initMixSdk()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WebView.setDataDirectorySuffix(Process.myPid().toString() + "")
        }
    }

    private fun initMixSdk() {
//        UnicornImageLoader imageLoader;
//        imageLoader = new GlideImageLoader(this);
        //内部已经初始化了 Nim baseSDK
//        Unicorn.init(this, YsfHelper.readAppKey(this), mixOptions(), imageLoader);
    }

    /*private YSFOptions mixOptions() {
        YSFOptions options = new YSFOptions();
        if (options.uiCustomization == null) {
            options.uiCustomization = new UICustomization();
        }
        options.onMessageItemClickListener = (context, url) -> ToastHelper.showToast(context, url);

        options.onBotEventListener = new OnBotEventListener() {
            @Override
            public boolean onUrlClick(Context context, String url) {
                ToastHelper.showToast(context, url);
                return true;
            }
        };
        options.quickEntryListener = new QuickEntryListener() {
            @Override
            public void onClick(Context context, String shopId, QuickEntry quickEntry) {
                ToastHelper.showToast(context, shopId);
                if (quickEntry.getId() == 0) {
                }
            }
        };
        options.isPullMessageFromServer = true;
        options.isMixSDK = true;
        if (!TextUtils.isEmpty(DemoPrivatizationConfig.getYsfDaUrlLabel(this)) && !TextUtils.isEmpty(DemoPrivatizationConfig.getYsfDefalutUrlLabel(this))) {
            UnicornAddress unicornAddress = new UnicornAddress();
            unicornAddress.defaultUrl = DemoPrivatizationConfig.getYsfDefalutUrlLabel(this);
            unicornAddress.daUrl = DemoPrivatizationConfig.getYsfDaUrlLabel(this);
            options.unicornAddress = unicornAddress;
        }
        return options;
    }*/
    private val loginInfo: LoginInfo?
        private get() {
            val account = Preferences.getUserAccount()
            val token = Preferences.getUserToken()
            return if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)) {
                setAccount(account.toLowerCase())
                LoginInfo(account, token)
            } else {
                null
            }
        }

    private fun initUIKit() {
        NimUIKit.init(this, buildUIKitOptions())
        SessionHelper.init()
        ContactHelper.init()

        // 添加自定义推送文案以及选项，请开发者在各端（Android、IOS、PC、Web）消息发送时保持一致，以免出现通知不一致的情况
        NimUIKit.setCustomPushContentProvider(DemoPushContentProvider())
        NimUIKit.setOnlineStateContentProvider(DemoOnlineStateContentProvider())
    }

    private fun buildUIKitOptions(): UIKitOptions {
        val options = UIKitOptions()
        // 设置app图片/音频/日志等缓存目录
        options.appCacheDir = NimSDKOptionConfig.getAppCacheDir(this) + "/app"
        options.messageLeftBackground = R.drawable.nim_message_item_left_selector
        options.messageRightBackground = R.drawable.nim_message_item_right_selector
        return options
    }
}