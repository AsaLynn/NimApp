package com.netease.nim.demo.main.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import com.netease.nim.demo.DemoCache.getAccount
import com.netease.nim.demo.DemoCache.setMainTaskLaunching
import com.netease.nim.demo.R
import com.netease.nim.demo.common.util.sys.SysInfoUtil
import com.netease.nim.demo.config.preference.Preferences
import com.netease.nim.demo.login.LoginActivity.Companion.start
import com.netease.nimlib.sdk.NimIntent
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.zxn.netease.nimsdk.api.NimUIKit
import com.zxn.netease.nimsdk.common.activity.UI
import com.zxn.netease.nimsdk.common.util.log.LogUtil
import java.util.*

class WelcomeActivity : UI() {
    private var customSplash = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        setMainTaskLaunching(true)
        if (savedInstanceState != null) {
            intent = Intent() // 从堆栈恢复，不再重复解析之前的intent
        }
        if (!firstEnter) {
            onIntent() // APP进程还在，Activity被重新调度起来
        } else {
            showSplashView() // APP进程重新起来
        }
    }

    private fun showSplashView() {
        customSplash = true
    }

    /*
     * 如果Activity在，不会走到onCreate，而是onNewIntent，这时候需要setIntent
     * 场景：点击通知栏跳转到此，会收到Intent
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (!customSplash) {
            onIntent()
        }
    }

    override fun onResume() {
        super.onResume()
        if (firstEnter) {
            firstEnter = false
            val runnable: Runnable = object : Runnable {
                override fun run() {
                    if (!NimUIKit.isInitComplete()) {
                        LogUtil.i(TAG, "wait for uikit cache!")
                        Handler().postDelayed(this, 100)
                        return
                    }
                    customSplash = false
                    if (canAutoLogin()) {
                        onIntent()
                    } else {
                        start(this@WelcomeActivity)
                        finish()
                    }
                }
            }
            if (customSplash) {
                Handler().postDelayed(runnable, 1000)
            } else {
                runnable.run()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        setMainTaskLaunching(false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.clear()
    }

    // 处理收到的Intent
    private fun onIntent() {
        LogUtil.i(TAG, "onIntent...")
        if (TextUtils.isEmpty(getAccount())) {
            // 判断当前app是否正在运行
            if (!SysInfoUtil.stackResumed(this)) {
                start(this)
            }
            finish()
        } else {
            // 已经登录过了，处理过来的请求
            val intent = intent
            if (intent != null) {
                if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
                    parseNotifyIntent(intent)
                    return
                }
            }
            if (!firstEnter && intent == null) {
                finish()
            } else {
                showMainActivity()
            }
        }
    }

    /**
     * 已经登陆过，自动登陆
     */
    private fun canAutoLogin(): Boolean {
        val account = Preferences.getUserAccount()
        val token = Preferences.getUserToken()
        Log.i(TAG, "get local sdk token =$token")
        return !TextUtils.isEmpty(account) && !TextUtils.isEmpty(token)
    }

    private fun parseNotifyIntent(intent: Intent) {
        val messages =
            intent.getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT) as ArrayList<IMMessage>?
        if (messages == null || messages.size > 1) {
            showMainActivity(null)
        } else {
            showMainActivity(Intent().putExtra(NimIntent.EXTRA_NOTIFY_CONTENT, messages[0]))
        }
    }

    private fun showMainActivity(intent: Intent? = null) {
        MainActivity.start(this@WelcomeActivity, intent)
        finish()
    }

    companion object {
        private const val TAG = "WelcomeActivity"
        private var firstEnter = true // 是否首次进入
    }
}