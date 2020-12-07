package com.zxn.netease.nimsdk.business.session.module

import android.app.Activity
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum

class Container {
    @JvmField
    val activity: Activity?
    @JvmField
    val account: String?
    @JvmField
    val sessionType: SessionTypeEnum?
    @JvmField
    val proxy: ModuleProxy
    @JvmField
    val proxySend: Boolean

    constructor(
        activity: Activity,
        account: String,
        sessionType: SessionTypeEnum,
        proxy: ModuleProxy
    ) {
        this.activity = activity
        this.account = account
        this.sessionType = sessionType
        this.proxy = proxy
        proxySend = false
    }

    constructor(
        activity: Activity?, account: String?, sessionType: SessionTypeEnum?, proxy: ModuleProxy,
        proxySend: Boolean
    ) {
        this.activity = activity
        this.account = account
        this.sessionType = sessionType
        this.proxy = proxy
        this.proxySend = proxySend
    }
}