package com.zxn.netease.nimsdk.api.wrapper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.text.TextUtils
import com.netease.nimlib.sdk.NIMClient
import com.netease.nimlib.sdk.RequestCallbackWrapper
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.nos.NosService
import com.netease.nimlib.sdk.uinfo.UserInfoProvider
import com.netease.nimlib.sdk.uinfo.model.UserInfo
import com.zxn.netease.nimsdk.R
import com.zxn.netease.nimsdk.api.NimUIKit
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * 初始化sdk 需要的用户信息提供者，现主要用于内置通知提醒获取昵称和头像
 * 注意不要与 IUserInfoProvider 混淆，后者是 UIKit 与 demo 之间的数据共享接口
 *
 */
class NimUserInfoProvider(private val context: Context) : UserInfoProvider {
    override fun getUserInfo(account: String): UserInfo {
        return NimUIKit.getUserInfoProvider().getUserInfo(account)!!
    }

    override fun getAvatarForMessageNotifier(
        sessionType: SessionTypeEnum,
        sessionId: String
    ): Bitmap {
        /*
         * 注意：这里最好从缓存里拿，如果加载时间过长会导致通知栏延迟弹出！该函数在后台线程执行！
         */
        var bm: Bitmap? = null
        val defResId = R.drawable.nim_avatar_default
        val countDownLatch = CountDownLatch(1)
        val originUrl = arrayOfNulls<String>(1)
        if (SessionTypeEnum.P2P == sessionType) {
            val user = getUserInfo(sessionId)
            originUrl[0] = if (user != null) user.avatar else null
        }
        NIMClient.getService(NosService::class.java).getOriginUrlFromShortUrl(originUrl[0])
            .setCallback(
                object : RequestCallbackWrapper<String?>() {
                    override fun onResult(code: Int, result: String?, exception: Throwable) {
                        originUrl[0] = result
                        countDownLatch.countDown()
                    }
                })
        try {
            countDownLatch.await(200, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        if (!TextUtils.isEmpty(originUrl[0])) {
            bm = NimUIKit.getImageLoaderKit().getNotificationBitmapFromCache(originUrl[0])
        }
        if (bm == null) {
            val drawable = context.resources.getDrawable(defResId)
            if (drawable is BitmapDrawable) {
                bm = drawable.bitmap
            }
        }
        return bm!!
    }

    override fun getDisplayNameForMessageNotifier(
        account: String, sessionId: String,
        sessionType: SessionTypeEnum
    ): String? {
        var nick: String? = null
        if (sessionType == SessionTypeEnum.P2P) {
            nick = NimUIKit.getContactProvider().getAlias(account)
        }
        return if (TextUtils.isEmpty(nick)) {
            null // 返回null，交给sdk处理。如果对方有设置nick，sdk会显示nick
        } else nick!!
    }
}