package com.zxn.netease.nimsdk.common

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast

object ToastHelper {
    private var sToast: Toast? = null

    @JvmStatic
    fun showToast(context: Context, text: String) {
        showToastInner(context, text, Toast.LENGTH_SHORT)
    }

    @JvmStatic
    fun showToast(context: Context, stringId: Int) {
        showToastInner(context, context.getString(stringId), Toast.LENGTH_SHORT)
    }

    @JvmStatic
    fun showToastLong(context: Context, text: String) {
        showToastInner(context, text, Toast.LENGTH_LONG)
    }

    @JvmStatic
    fun showToastLong(context: Context, stringId: Int) {
        showToastInner(context, context.getString(stringId), Toast.LENGTH_LONG)
    }

    private fun showToastInner(context: Context, text: String, duration: Int) {
        ensureToast(context)
        sToast!!.setText(text)
        sToast!!.duration = duration
        sToast!!.show()
    }

    @SuppressLint("ShowToast")
    private fun ensureToast(context: Context) {
        if (sToast != null) {
            return
        }
        synchronized(ToastHelper::class.java) {
            if (sToast != null) {
                return
            }
            sToast = Toast.makeText(context.applicationContext, " ", Toast.LENGTH_SHORT)
        }
    }
}