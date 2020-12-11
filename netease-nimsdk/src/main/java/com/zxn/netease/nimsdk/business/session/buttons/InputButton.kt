package com.zxn.netease.nimsdk.business.session.buttons

import android.view.View
import com.zxn.netease.nimsdk.business.session.module.input.InputPanel
import java.io.Serializable

/**
 * Copyright(c) ${}YEAR} ZhuLi co.,Ltd All Rights Reserved.
 *
 * @className: InputButton$
 * @description: TODO 类描述
 * @version: v0.0.1
 * @author: zxn < a href=" ">zhangxiaoning@17biyi.com</ a>
 * @date: 2020/12/11$ 18:17$
 * @updateUser: 更新者：
 * @updateDate: 2020/12/11$ 18:17$
 * @updateRemark: 更新说明：
 * @version: 1.0
 * */
/**
 *底部输入框右侧的按钮.ButtonType
 */
abstract class InputButton(var backIconId: Int = 0) : Serializable {

    /**
     * 点击的按钮的事件类型
     * 0:+号按钮扩展,1:表情按钮,2:礼物按钮.3:录音切换按钮,
     */
    abstract var buttonType: Int

    /**
     * 按钮点击事件
     * view:被点击的按钮
     * inputPanel:输入框
     * sessionId:回话id.
     */
    abstract fun onClick(view: View?, inputPanel: InputPanel, sessionId: String?)
}