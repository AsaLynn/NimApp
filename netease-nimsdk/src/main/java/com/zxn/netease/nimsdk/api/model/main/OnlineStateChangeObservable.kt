package com.zxn.netease.nimsdk.api.model.main

import android.content.Context
import android.os.Handler
import java.util.*

/**
 * 在线状态事件变更通知接口.
 */
class OnlineStateChangeObservable(context: Context) {
    // 在线状态变化监听
    private val onlineStateChangeObservers: MutableList<OnlineStateChangeObserver>?
    private val uiHandler: Handler
    @Synchronized
    fun registerOnlineStateChangeListeners(
        onlineStateChangeObserver: OnlineStateChangeObserver,
        register: Boolean
    ) {
        if (register) {
            onlineStateChangeObservers!!.add(onlineStateChangeObserver)
        } else {
            onlineStateChangeObservers!!.remove(onlineStateChangeObserver)
        }
    }

    @Synchronized
    fun notifyOnlineStateChange(accounts: Set<String?>?) {
        uiHandler.post {
            if (onlineStateChangeObservers != null) {
                for (listener in onlineStateChangeObservers) {
                    listener.onlineStateChange(accounts)
                }
            }
        }
    }

    init {
        onlineStateChangeObservers = LinkedList()
        uiHandler = Handler(context.mainLooper)
    }
}