package com.zxn.netease.nimsdk.common.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * 修复ViewPagerbug
 *
 *
 * https://stackoverflow.com/questions/16459196/java-lang-illegalargumentexception-pointerindex-out-of-range-exception-dispat
 */
class FixBugsViewPager : ViewPager {
    constructor(context: Context?) : super(context!!)
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    )

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        try {
            return super.onInterceptTouchEvent(ev)
        } catch (ex: IllegalArgumentException) {
            ex.printStackTrace()
        }
        return false
    }
}