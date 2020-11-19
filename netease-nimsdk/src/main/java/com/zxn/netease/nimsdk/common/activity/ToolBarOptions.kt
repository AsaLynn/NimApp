package com.zxn.netease.nimsdk.common.activity

/**
 *
 */
open class ToolBarOptions {
    /**
     * toolbar的title资源id
     */
    @JvmField
    var titleId = 0

    /**
     * toolbar的title
     */
    @JvmField
    var titleString: String? = null

    /**
     * toolbar的logo资源id
     */
    @JvmField
    var logoId = 0

    /**
     * toolbar的返回按钮资源id
     */
    @JvmField
    var navigateId = 0

    /**
     * toolbar的返回按钮
     */
    @JvmField
    var isNeedNavigate = false
}