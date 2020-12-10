package com.zxn.netease.nimsdk.api.model.user

import android.content.Context
import android.os.Handler
import java.util.*

/**
 * 用户资料变动观察者管理
 */
class UserInfoObservable(context: Context) {
    private val observers: MutableList<UserInfoObserver> = ArrayList()
    private val uiHandler: Handler
    @Synchronized
    fun registerObserver(observer: UserInfoObserver?, register: Boolean) {
        if (observer == null) {
            return
        }
        if (register) {
            observers.add(observer)
        } else {
            observers.remove(observer)
        }
    }

    @Synchronized
    fun notifyUserInfoChanged(accounts: List<String>?) {
        uiHandler.post {
            for (observer in observers) {
                observer.onUserInfoChanged(accounts)
            }
        }
    }

    init {
        uiHandler = Handler(context.mainLooper)
    }
}