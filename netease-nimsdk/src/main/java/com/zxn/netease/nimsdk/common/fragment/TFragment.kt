package com.zxn.netease.nimsdk.common.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import com.zxn.netease.nimsdk.common.activity.UI
import com.zxn.netease.nimsdk.common.util.log.LogUtil

/**
 * 基类.
 */
abstract class TFragment : Fragment() {

    var containerId = 0

    protected var isDestroyed = false
        private set

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        LogUtil.ui("fragment: " + javaClass.simpleName + " onActivityCreated()")
        isDestroyed = false
    }

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.ui("fragment: " + javaClass.simpleName + " onDestroy()")
        isDestroyed = true
    }

    protected val handler: Handler
        get() = Companion.handler

    protected fun postRunnable(runnable: Runnable) {
        postDelayed(runnable, 0)
    }

    protected fun postDelayed(runnable: Runnable, delay: Long) {
        Companion.handler.postDelayed({
            // validate
            if (!isAdded) {
                return@postDelayed
            }
            // run
            runnable.run()
        }, delay)
    }

    protected fun showKeyboard(isShow: Boolean) {
        activity?.let {
            val inputMethodManager =
                it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (isShow) {
                if (it.currentFocus == null) {
                    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
                } else {
                    inputMethodManager.showSoftInput(it.currentFocus, 0)
                }
            } else {
                it.currentFocus?.let { currentView ->
                    inputMethodManager.hideSoftInputFromWindow(
                        currentView.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS
                    )
                }
            }
        }
    }

    protected fun hideKeyboard(view: View) {
        activity?.let {
            val imm = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    protected fun <T : View?> findView(resId: Int): T {
        return view!!.findViewById<View>(resId) as T
    }

    protected fun setToolBar(toolbarId: Int, titleId: Int, logoId: Int) {
        activity?.let {
            if (it is UI) {
                it.setToolBar(toolbarId, titleId)
            }
        }

    }

    protected fun setTitle(titleId: Int) {
        activity?.let {
            if (it is UI) {
                it.setTitle(titleId)
            }
        }
    }

    companion object {
        private val handler = Handler()
    }
}