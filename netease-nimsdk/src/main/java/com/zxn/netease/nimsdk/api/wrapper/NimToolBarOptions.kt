package com.zxn.netease.nimsdk.api.wrapper

import com.zxn.netease.nimsdk.R
import com.zxn.netease.nimsdk.common.activity.ToolBarOptions

/**
 */
class NimToolBarOptions : ToolBarOptions() {
    init {
//        logoId = R.drawable.nim_actionbar_nest_dark_logo;
        navigateId = R.mipmap.nim_actionbar_dark_back_icon
        isNeedNavigate = true
    }
}