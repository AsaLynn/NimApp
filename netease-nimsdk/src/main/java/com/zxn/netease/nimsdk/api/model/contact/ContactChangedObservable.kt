package com.zxn.netease.nimsdk.api.model.contact

import android.content.Context
import android.os.Handler
import java.util.*

/**
 * 好友关系变动观察者管理
 */
class ContactChangedObservable(context: Context) {
    private val observers: MutableList<ContactChangedObserver> = ArrayList()
    private val uiHandler: Handler

    @Synchronized
    fun registerObserver(observer: ContactChangedObserver?, register: Boolean) {
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
    fun notifyAddedOrUpdated(accounts: List<String?>?) {
        uiHandler.post {
            for (observer in observers) {
                observer.onAddedOrUpdatedFriends(accounts)
            }
        }
    }

    @Synchronized
    fun notifyDelete(accounts: List<String?>?) {
        uiHandler.post {
            for (observer in observers) {
                observer.onDeletedFriends(accounts)
            }
        }
    }

    @Synchronized
    fun notifyAddToBlackList(accounts: List<String?>?) {
        uiHandler.post {
            for (observer in observers) {
                observer.onAddUserToBlackList(accounts)
            }
        }
    }

    @Synchronized
    fun notifyRemoveFromBlackList(accounts: List<String?>?) {
        uiHandler.post {
            for (observer in observers) {
                observer.onRemoveUserFromBlackList(accounts)
            }
        }
    }

    init {
        uiHandler = Handler(context.mainLooper)
    }
}