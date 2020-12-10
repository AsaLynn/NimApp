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
        protected get() = Companion.handler

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
        val activity = activity ?: return
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            ?: return
        if (isShow) {
            if (activity.currentFocus == null) {
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            } else {
                imm.showSoftInput(activity.currentFocus, 0)
            }
        } else {
            if (activity.currentFocus != null) {
                imm.hideSoftInputFromWindow(
                    activity.currentFocus!!.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        }
    }

    protected fun hideKeyboard(view: View) {
        val activity = activity ?: return
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            ?: return
        imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    protected fun <T : View?> findView(resId: Int): T {
        return view!!.findViewById<View>(resId) as T
    }

    protected fun setToolBar(toolbarId: Int, titleId: Int, logoId: Int) {
        if (activity != null && activity is UI) {
            (activity as UI?)!!.setToolBar(toolbarId, titleId)
        }
    }

    protected fun setTitle(titleId: Int) {
        if (activity != null && activity is UI) {
            activity!!.setTitle(titleId)
        }
    }

    companion object {
        private val handler = Handler()
    }
}