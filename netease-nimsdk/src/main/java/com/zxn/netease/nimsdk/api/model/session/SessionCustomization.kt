package com.zxn.netease.nimsdk.api.model.session

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment
import com.netease.nimlib.sdk.msg.model.IMMessage
import com.zxn.netease.nimsdk.business.session.actions.BaseAction
import com.zxn.netease.nimsdk.business.session.buttons.InputButton
import java.io.Serializable
import java.util.*

/**
 * 聊天界面定制化参数。 可定制：<br></br>
 * 1. 聊天背景 <br></br>
 * 2. 加号展开后的按钮和动作 <br></br>
 * 3. ActionBar右侧按钮。
 * 4.聊天列表顶部温馨提示内容
 */
open class SessionCustomization : Serializable {

    /**
     *底部输入框右侧可定制按钮。默认为空。
     */
    var bottomButtonList: ArrayList<InputButton>? = null

    /**
     * 顶部布局提示内容
     */
    var headerLayoutId: Int = 0

    /**
     * 聊天背景。优先使用uri，如果没有提供uri，使用color。如果没有color，使用默认。uri暂时支持以下格式：<br></br>
     * drawable: android.resource://包名/drawable/资源名
     * assets: file:///android_asset/{asset文件路径}
     * file: file:///文件绝对路径
     */
    var backgroundUri: String? = null

    /**
     * 聊天背景颜色的资源
     */
    var backgroundColor: Int = 0

    // UIKit
    @JvmField
    var withSticker = false

    /**
     * 加号展开后的action list。
     */
    var actions: ArrayList<BaseAction>? = null

    /**
     * ActionBar标题右侧可定制按钮。默认为空。
     */
    var buttons: ArrayList<OptionsButton>? = null

    /**
     * 如果OptionsButton的点击响应中需要startActivityForResult，可在此函数中处理结果。
     * 需要注意的是，由于加号中的Action的限制，RequestCode只能使用int的最低8位。
     *
     * @param activity    当前的聊天Activity
     * @param requestCode 请求码
     * @param resultCode  结果码
     * @param data        返回的结果数据
     */
    open fun onActivityResult(
        activity: Activity?,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
    }

    fun isAllowSendMessage(message: IMMessage?): Boolean {
        return true
    }

    // uikit内建了对贴图消息的输入和管理展示，并和emoji表情整合在了一起，但贴图消息的附件定义开发者需要根据自己的扩展
    open fun createStickerAttachment(category: String?, item: String?): MsgAttachment? {
        return null
    }

    /**
     * 获取消息的简述
     *
     * @return 消息的简述
     */
    fun getMessageDigest(message: IMMessage?): String {
        return if (message == null) "" else message.content
    }

    /**
     * ActionBar 右侧按钮，可定制icon和点击事件
     */
    abstract class OptionsButton(// 图标drawable id
        var iconId: Int = 0
    ) : Serializable {

        // 响应事件
        abstract fun onClick(context: Context, view: View?, sessionId: String?)
    }
}