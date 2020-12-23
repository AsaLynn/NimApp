package com.netease.nim.demo

import android.content.Context
import com.netease.nim.avchatkit.AVChatKit
import com.netease.nimlib.sdk.StatusBarNotificationConfig
import com.zxn.netease.nimsdk.api.NimUIKit

object DemoCache {
    @JvmStatic
    var context: Context? = null
        private set

    private var account: String? = null

    @JvmStatic
    var notificationConfig: StatusBarNotificationConfig? = null
    fun clear() {
        account = null
    }

    @JvmStatic
    fun getAccount(): String? {
        return account
    }

    private var mainTaskLaunching = false
    @JvmStatic
    fun setAccount(account: String?) {
        DemoCache.account = account
        NimUIKit.setAccount(account)
    }

    @JvmStatic
    fun setContext(context: Context) {
        DemoCache.context = context.applicationContext
        AVChatKit.setContext(context)
    }

    @JvmStatic
    fun setMainTaskLaunching(mainTaskLaunching: Boolean) {
        DemoCache.mainTaskLaunching = mainTaskLaunching
    }
}