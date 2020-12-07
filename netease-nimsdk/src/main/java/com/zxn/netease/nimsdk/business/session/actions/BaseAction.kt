package com.zxn.netease.nimsdk.business.session.actions

import android.app.Activity
import android.content.Intent
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.zxn.netease.nimsdk.business.session.module.Container
import java.io.Serializable
import java.lang.ref.WeakReference

/**
 * Action基类。<br></br>
 * 注意：在子类中调用startActivityForResult时，requestCode必须用makeRequestCode封装一遍，否则不能再onActivityResult中收到结果。
 * requestCode仅能使用最低8位。
 */
abstract class BaseAction
/**
 * 构造函数
 *
 * @param iconResId 图标 res id
 * @param titleId   图标标题的string res id
 */ protected constructor(val iconResId: Int, val titleId: Int) : Serializable {

    @Transient
    private var index = 0

    // Container持有activity ， 防止内存泄露
    @Transient
    private var containerRef: WeakReference<Container>? = null
    val activity: Activity?
        get() = container.activity
    val account: String?
        get() = container.account
    val sessionType: SessionTypeEnum?
        get() = container.sessionType
    var container: Container
        get() = containerRef!!.get()
            ?: throw RuntimeException("container be recycled by vm ")
        set(container) {
            containerRef = WeakReference(container)
        }

    abstract fun onClick()
    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}
    protected fun sendMessage(message: IMMessage?) {
        container.proxy.sendMessage(message)
    }

    protected fun makeRequestCode(requestCode: Int): Int {
        require(requestCode and -0x100 == 0) { "Can only use lower 8 bits for requestCode" }
        return (index + 1 shl 8) + (requestCode and 0xff)
    }

    fun setIndex(index: Int) {
        this.index = index
    }
}